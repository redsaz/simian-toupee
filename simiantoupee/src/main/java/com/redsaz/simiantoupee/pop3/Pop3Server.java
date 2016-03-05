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
package com.redsaz.simiantoupee.pop3;

import com.redsaz.simiantoupee.api.MessagesService;
import com.redsaz.simiantoupee.api.exceptions.AppServerException;
import java.io.Closeable;
import java.net.InetSocketAddress;
import org.apache.james.protocols.api.logger.ProtocolLoggerAdapter;
import org.apache.james.protocols.netty.NettyServer;
import org.apache.james.protocols.pop3.POP3Configuration;
import org.apache.james.protocols.pop3.POP3Protocol;
import org.apache.james.protocols.pop3.POP3ProtocolHandlerChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows control of the POP3 server.
 *
 * @author Redsaz <redsaz@gmail.com>
 */
public class Pop3Server implements Closeable {

    private final NettyServer pop3Server;

    /**
     * Constructs and starts a POP3 server.
     * <p>
     * Any port works, but any that end with "110" would be a good choice, since
     * 110 is the usual non-secure POP3 port.
     *
     * @param messagesService Provides the email messages
     * @param port the port the server runs on
     */
    public Pop3Server(MessagesService messagesService, int port) {
        try {
            POP3Configuration config = new POP3Configuration();
            config.setGreeting("Welcome to simiantoupee POP3 server!");
            config.setSoftwareName("simiantoupee");
            config.setHelloName("Hello!");

            Logger slf4jLog = LoggerFactory.getLogger(Pop3Server.class);
            org.apache.james.protocols.api.logger.Logger log = new ProtocolLoggerAdapter(slf4jLog);

            Pop3AuthenticationHandler pop3AuthHandler = new Pop3AuthenticationHandler(messagesService);

            POP3ProtocolHandlerChain chain = new POP3ProtocolHandlerChain(pop3AuthHandler);

            InetSocketAddress socket = new InetSocketAddress(port);
            POP3Protocol pop3Prot = new POP3Protocol(chain, config, log);
            pop3Server = new NettyServer(pop3Prot);
            pop3Server.setListenAddresses(socket);
            pop3Server.setTimeout(180);
            pop3Server.setMaxConcurrentConnections(1000);
            pop3Server.bind();
        } catch (Exception ex) {
            throw new AppServerException("Could not start POP3 server. Reason: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void close() {
        pop3Server.unbind();
    }

}
