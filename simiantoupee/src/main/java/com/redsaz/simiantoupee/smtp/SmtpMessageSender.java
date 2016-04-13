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

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Can be used to test out an SMTP server quickly.
 *
 * @author Redsaz <redsaz@gmail.com>
 */
public class SmtpMessageSender {

    // PLAIN and LOGIN use base64 encoding.
    // List retrieved from https://en.wikipedia.org/wiki/SMTP_Authentication
    private static final List<String> AUTH_MECHS = Arrays.asList("LOGIN");
    //private static final List<String> AUTH_MECHS = Arrays.asList("PLAIN", "LOGIN", "GSSAPI", "DIGEST-MD5", "MD5", "CRAM-MD5");

    public static void main(String[] args) throws InterruptedException, UnsupportedEncodingException {
        sendMessage();
    }

    private static void sendMessage() throws UnsupportedEncodingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.smtp.host", "localhost");
        props.put("mail.smtp.port", "40025");
        Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("example", "SECRET");
            }
        });

        String msgBody = "Message body! " + System.currentTimeMillis();

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("examplesender@example.com", "Example User"));
            msg.addRecipient(Message.RecipientType.TO,
                    new InternetAddress("examplerecipient@example.com", "Mr. User"));
            msg.setSubject("Whoops");
            msg.setText(msgBody);
            Transport.send(msg);

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
