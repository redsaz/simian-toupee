/*
 * Copyright 2015 Redsaz <redsaz@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redsaz.embeddedrest;

import io.undertow.Undertow;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;

/**
 * Entrypoint for the application which will either extract the contents and run
 * it, or if already extracted, run it.
 *
 * @author Redsaz <redsaz@gmail.com>
 */
public class EmbeddedRest {

    private static final String CONTEXT_PATH = "/";
    private static final boolean IS_POSIX = FileSystems.getDefault().supportedFileAttributeViews().contains("posix");

    public static void main(String[] args) throws MalformedURLException, IOException {
        if (!explodeIfWar(args)) {
            startServer(args);
        }
    }

    /**
     * Extracts the contents of the archive if running from said archive and if
     * not already extracted.
     *
     * @return true if extracted, false if no extraction occurred.
     * @throws IOException if some part of the extraction failed.
     */
    private static boolean explodeIfWar(String[] args) throws IOException {
        String warFile = getWarFile();
        if (warFile != null) {
            File destDir = getDestDir();
            explodeWar(warFile, destDir);
            startProcess(destDir, args);
            return true;
        }
        return false;
    }

    /**
     * Get the archive file from which this instance is running.
     *
     * @return The archive file, if running from an archive. If not, then return
     * null.
     */
    private static String getWarFile() {
        String classRes = EmbeddedRest.class.getResource("EmbeddedRest.class").toString();
        String prefix = "jar:file:";
        if (classRes.startsWith(prefix)) {
            int lastPos = classRes.indexOf("!");
            return classRes.substring(prefix.length(), lastPos);
        }
        return null;
    }

    /**
     * Extract the archive to a directory.
     *
     * @param warFile The archive file to extract.
     * @param destDir Extract the archive to this directory.
     * @throws IOException when one or more files were not extracted.
     */
    private static void explodeWar(String warFile, File destDir) throws IOException {
        // Look at http://www.devx.com/tips/Tip/22124
        System.out.println("Exploding " + warFile + " to " + destDir);
        JarFile jar = new JarFile(warFile);
        Enumeration enumEntries = jar.entries();
        Path destPath = destDir.toPath().toAbsolutePath();
        while (enumEntries.hasMoreElements()) {
            JarEntry file = (JarEntry) enumEntries.nextElement();
            Path p = destPath.resolve(file.getName());
            if (file.isDirectory()) {
                // if it is a directory, create it
                Files.createDirectories(p);
                continue;
            }
            Files.createDirectories(p.getParent());
            BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(p));
            InputStream is = new BufferedInputStream(jar.getInputStream(file)); // get the input stream
            while (is.available() > 0) {  // write contents of 'is' to 'fos'
                bos.write(is.read());
            }
            bos.close();
            is.close();
            if (p.toString().endsWith(".sh") && IS_POSIX) {
                Set<PosixFilePermission> perms = Files.getPosixFilePermissions(p);
                perms.add(PosixFilePermission.OWNER_EXECUTE);
                Files.setPosixFilePermissions(p, perms);
            }
        }
        System.out.println("Finished exploding.");
    }

    /**
     * The archive should be extracted to this directory.
     *
     * @return the destination directory.
     */
    private static File getDestDir() {
        File homeDir = new File(System.getProperty("user.home"), ".embeddedrest/war");
        if (!homeDir.exists() && !homeDir.mkdirs()) {
            throw new RuntimeException("Path \"" + homeDir + "\" was not able to be created.");
        }
        return homeDir;
    }

    /**
     * Starts server that is at the base path.
     *
     * @param basePath The location of the application.
     */
    private static void startProcess(File basePath, String[] args) throws IOException {
        List<String> passedArgs = new ArrayList<String>();
        passedArgs.add("java");
        passedArgs.add("com.redsaz.embeddedrest.EmbeddedRest");
        passedArgs.addAll(Arrays.asList(args));
        new ProcessBuilder()
                .inheritIO()
                .command(passedArgs.toArray(args))
                .directory(basePath)
                .start();
    }

    /**
     * Starts the server.
     *
     * @param args Command-line arguments.
     * @throws MalformedURLException.
     */
    private static void startServer(String[] args) throws MalformedURLException {
        for (Entry<Object, Object> prop : System.getProperties().entrySet()) {
            System.out.println(prop.getKey() + ": " + prop.getValue());
        }
        File asdf = new File("WEB-INF");
        File asdf2 = new File("WEB-INF/classes");
        File asdf3 = new File("WEB-INF/lib");
        URL[] urls = new URL[]{asdf.toURL(), asdf2.toURL(), asdf3.toURL()};
        URLClassLoader ucl = new URLClassLoader(urls, EmbeddedRest.class.getClassLoader(), null);
        // Mad props to https://github.com/vdevigere/undertow-cdi-jaxrs/wiki/CDI-%28Weld%29,-JAX-RS-%28Resteasy%29-on-Undertow
        // for showing how weld can be used by an embedded undertow server.
        System.out.println("Starting embedded server...");
        UndertowJaxrsServer jaxrsServer = new UndertowJaxrsServer();
        ResteasyDeployment restDeployment = new ResteasyDeployment();
        restDeployment.setApplicationClass("com.redsaz.embeddedrest.view.EmbeddedRestApplication");
        restDeployment.setInjectorFactoryClass("org.jboss.resteasy.cdi.CdiInjectorFactory");
        DeploymentInfo deployment = jaxrsServer.undertowDeployment(restDeployment);
        deployment.setClassLoader(ucl)
                .setContextPath(CONTEXT_PATH)
                .setDeploymentName("embeddedrest.war")
                .addListeners(Servlets.listener(org.jboss.weld.environment.servlet.Listener.class));
        jaxrsServer = jaxrsServer.deploy(deployment);
        Undertow.Builder undertow = Undertow.builder()
                .addHttpListener(8080, "localhost");
        jaxrsServer.start(undertow);

        System.out.println("...embedded server started.");
    }
}
