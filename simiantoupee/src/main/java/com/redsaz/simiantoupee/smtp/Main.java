/*
 * Copyright 2016 Redsaz <redsaz@gmail.com>.
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
package com.redsaz.simiantoupee.smtp;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.subethamail.smtp.AuthenticationHandler;
import org.subethamail.smtp.AuthenticationHandlerFactory;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.server.SMTPServer;

/**
 *
 * @author Redsaz <redsaz@gmail.com>
 */
public class Main {

    // PLAIN and LOGIN use base64 encoding.
    // List retrieved from https://en.wikipedia.org/wiki/SMTP_Authentication
    private static final List<String> AUTH_MECHS = Arrays.asList("LOGIN");
    //private static final List<String> AUTH_MECHS = Arrays.asList("PLAIN", "LOGIN", "GSSAPI", "DIGEST-MD5", "MD5", "CRAM-MD5");

    public static void main(String[] args) throws InterruptedException, UnsupportedEncodingException {
        // SimpleMessageListenerAdapter might be used instead.
        SMTPServer smtpServer = new SMTPServer(new MessageHandlerFactory() {
            @Override
            public MessageHandler create(MessageContext mc) {
                return new MessageHandler() {
                    @Override
                    public void from(String from) throws RejectException {
                        System.out.println("From: " + from);
                    }

                    @Override
                    public void recipient(String recipient) throws RejectException {
                        System.out.println("Recipient: " + recipient);
                    }

                    @Override
                    public void data(InputStream data) throws RejectException, TooMuchDataException, IOException {

                        byte[] b = new byte[32768];
                        StringWriter sw = new StringWriter();
                        int read = data.read(b);
                        while (-1 < read) {
                            for (int i = 0; i < read; ++i) {
                                sw.write(b[i]);
                            }
                            read = data.read(b);
                        }
                        System.out.println(sw);
                        sw.close();
                        data.close();
                    }

                    @Override
                    public void done() {
                        System.out.println("Done.");
                    }
                };
            }
        }, new AuthenticationHandlerFactory() {

            @Override
            public List<String> getAuthenticationMechanisms() {
                return AUTH_MECHS;
            }

            @Override
            public AuthenticationHandler create() {
                return new AuthenticationHandler() {
                    private String mech;
                    private String user;
                    private String pass;

                    private String base64DecodedString(String base64Encoded) {
                        try {
                            byte[] b = javax.xml.bind.DatatypeConverter.parseBase64Binary(base64Encoded);
                            return new String(b);
                        } catch (RuntimeException ex) {
                            throw new RejectException();
                        }
                    }

                    @Override
                    public String auth(String clientInput) throws RejectException {
                        if (clientInput == null || clientInput.length() > 684) {
                            throw new RejectException();
                        }
                        if (mech == null) {
                            mech = clientInput;
                            return "334 VXNlcm5hbWU6"; // Base64 for "Username:"
                        } else if (user == null) {
                            user = base64DecodedString(clientInput);
                            return "334 UGFzc3dvcmQ6"; // Base64 for "Password:"
                        }
                        pass = base64DecodedString(clientInput);
                        System.out.println("Mech: " + mech);
                        System.out.println("User: " + user);
                        System.out.println("Pass: " + pass);

                        return null;
                    }

                    @Override
                    public Object getIdentity() {
                        System.out.println("getIdentity called");
                        return null;
                    }
                };
            }

        });

        smtpServer.setConnectionTimeout(300);
        smtpServer.setHostName("localhost");
        smtpServer.setPort(45678);
        smtpServer.setSoftwareName("simiantoupee");
        smtpServer.setRequireTLS(false);
        smtpServer.start();
        asdf();
        smtpServer.stop();
    }

    private static void asdf() throws UnsupportedEncodingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.smtp.host", "localhost");
        props.put("mail.smtp.port", "45678");
        Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("example", "SECRET");
            }
        });

        String msgBody = "Message body!";

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("example@example.com", "Example User"));
            msg.addRecipient(Message.RecipientType.TO,
                    new InternetAddress("example@example.com", "Mr. User"));
            msg.setSubject("Whoops");
            msg.setText(msgBody);
            Transport.send(msg);

        } catch (AddressException e) {
            // ...
        } catch (MessagingException e) {
            // ...
        }
    }
}
