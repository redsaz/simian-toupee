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
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

/**
 * Persists the incoming messages with {@link MessagesService}
 *
 * @author Redsaz <redsaz@gmail.com>
 */
public class PersistingMessageHandlerFactory implements MessageHandlerFactory {

    private static final Logger LOG = LoggerFactory.getLogger(PersistingMessageHandlerFactory.class);

    private final MessagesService msgSrv;

    public PersistingMessageHandlerFactory(MessagesService messagesService) {
        msgSrv = messagesService;
    }

    @Override
    public MessageHandler create(MessageContext mc) {
        return new MessageHandler() {
            @Override
            public void from(String from) throws RejectException {
                LOG.debug("From: {}", from);
            }

            @Override
            public void recipient(String recipient) throws RejectException {
                LOG.debug("Recipient: {}", recipient);
            }

            @Override
            public void data(InputStream data) throws RejectException, TooMuchDataException, IOException {
                msgSrv.create(data);
            }

            @Override
            public void done() {
                LOG.debug("Done.");
            }
        };
    }

}
