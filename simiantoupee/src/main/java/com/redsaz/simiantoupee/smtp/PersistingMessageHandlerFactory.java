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
import com.redsaz.simiantoupee.api.exceptions.AppServerException;
import com.redsaz.simiantoupee.api.model.MessageAddress;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
            private String sender;

            @Override
            public void from(String from) throws RejectException {
                sender = from;
            }

            @Override
            public void recipient(String recipient) throws RejectException {
                LOG.debug("Recipient: {}", recipient);
            }

            @Override
            public void data(InputStream data) throws RejectException, TooMuchDataException, IOException {
                MessageAddress senderAddr = getOrCreateAddress(sender);
                msgSrv.create(senderAddr, data);
            }

            @Override
            public void done() {
                LOG.debug("Done.");
            }

            private MessageAddress getOrCreateAddress(String fullAddress) {
                String[] addressAndName = getAddressParts(fullAddress);
                String email = addressAndName[0];
                String name = addressAndName[1];
                // First, attempt to get the email address.
                MessageAddress addr = msgSrv.getAddress(email);
                if (addr == null) {
                    // If it doesn't exist, it needs created.
                    try {
                        addr = msgSrv.createAddress(email, name);
                    } catch (AppServerException ex) {
                        // If a different thread/instance/etc created the address
                        // at the same time we did, and we failed, then retrieve the
                        // successfully created one.
                        addr = msgSrv.getAddress(email);
                    }
                    if (addr == null) {
                        // If we didn't create a sender record after all that, something
                        // is wrong.
                        throw new AppServerException("Failed to create/retrieve sender record for " + email);
                    }
                }
                return addr;
            }

            /**
             * Extracts the address and name of a single email address entry.
             * Examples are: {@code "Example Name" <example-email@example.com>}
             * or {@code <example-email@example.com>} or
             * {@code example-email@example.com}. The resulting String array
             * will have the address in [0] and the name in [1] or null if no
             * name was provided.
             *
             * @param addressEntry The single address entry
             * @return the address in [0], and the name in [1].
             */
            private String[] getAddressParts(String addressEntry) {
                String[] addressAndName = new String[]{null, null};
                Pattern addressPattern = Pattern.compile("(?:\"([^\"]*)\"\\s*<([^>]+)>)|(?:<([^>]+)>)|(?:([^>]+))");
                Matcher matcher = addressPattern.matcher(addressEntry);
                if (matcher.find()) {
                    addressAndName[1] = matcher.group(1);
                    addressAndName[0] = matcher.group(2);
                    if (addressAndName[0] == null) {
                        addressAndName[0] = matcher.group(3);
                        if (addressAndName[0] == null) {
                            addressAndName[0] = matcher.group(4);
                        }
                    }
                }

                return addressAndName;
            }

        };
    }

}
