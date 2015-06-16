package com.redsaz.embeddedrest;

import io.undertow.Undertow;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import java.util.Collections;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;

@ApplicationPath("/")
public class EmbeddedRest extends Application {

    private static final String CONTEXT_PATH = "/";

    public static void main(String[] args) {
        // Mad props to https://github.com/vdevigere/undertow-cdi-jaxrs/wiki/CDI-%28Weld%29,-JAX-RS-%28Resteasy%29-on-Undertow
        // for showing how weld can be used by an embedded undertow server.
        System.out.println("Starting embedded server...");
        UndertowJaxrsServer jaxrsServer = new UndertowJaxrsServer();
        ResteasyDeployment restDeployment = new ResteasyDeployment();
        restDeployment.setApplicationClass(EmbeddedRest.class.getName());
        restDeployment.setInjectorFactoryClass("org.jboss.resteasy.cdi.CdiInjectorFactory");
        DeploymentInfo deployment = jaxrsServer.undertowDeployment(restDeployment);
        deployment.setClassLoader(EmbeddedRest.class.getClassLoader())
                .setContextPath(CONTEXT_PATH)
                .setDeploymentName("embeddedrest.war")
                .addListeners(Servlets.listener(org.jboss.weld.environment.servlet.Listener.class));
        jaxrsServer = jaxrsServer.deploy(deployment);
        Undertow.Builder undertow = Undertow.builder()
                .addHttpListener(8081, "localhost");
        jaxrsServer.start(undertow);

        System.out.println("...embedded server started.");
    }

    @Override
    public Set<Class<?>> getClasses() {
        Class<?> clazz = HelloService.class;
        return (Set<Class<?>>) Collections.singleton(clazz);
    }

}
