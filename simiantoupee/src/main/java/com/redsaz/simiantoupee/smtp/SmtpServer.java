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

import com.redsaz.simiantoupee.api.MessagesService;
import java.io.Closeable;
import org.subethamail.smtp.server.SMTPServer;

/**
 * Allows control of the fake SMTP server.
 *
 * @author Redsaz <redsaz@gmail.com>
 */
public class SmtpServer implements Closeable {

    private final SMTPServer smtpServer;

    public SmtpServer(MessagesService messagesService, int port, String hostname) {
        // SimpleMessageListenerAdapter might be used instead.
        smtpServer = new SMTPServer(new PersistingMessageHandlerFactory(messagesService),
                new AgileAuthenticationHandlerFactory());

        smtpServer.setConnectionTimeout(300);
        smtpServer.setHostName(hostname);
        smtpServer.setPort(port);
        smtpServer.setSoftwareName("simiantoupee");
        smtpServer.setRequireTLS(false);
        smtpServer.start();
    }

    @Override
    public void close() {
        smtpServer.stop();
    }

}
