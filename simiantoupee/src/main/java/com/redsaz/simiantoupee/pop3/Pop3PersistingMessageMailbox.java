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
import com.redsaz.simiantoupee.api.model.BasicMessage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.mail.MessagingException;
import org.apache.james.protocols.pop3.mailbox.Mailbox;
import org.apache.james.protocols.pop3.mailbox.MessageMetaData;

/**
 * Connects Apache James (which uses {@link Mailbox} for POP3 data) to the app.
 *
 * @author Redsaz <redsaz@gmail.com>
 */
public class Pop3PersistingMessageMailbox implements Mailbox {

    private final MessagesService msgSrv;

    public Pop3PersistingMessageMailbox(MessagesService messagesService) {
        msgSrv = messagesService;
    }

    @Override
    public InputStream getMessageBody(String uid) throws IOException {
        try {
            return msgSrv.getMessage(uid).getInputStream();
        } catch (MessagingException ex) {
            throw new AppServerException("Can't get message " + uid + ". Reason: " + uid, ex);
        }
    }

    @Override
    public InputStream getMessageHeaders(String uid) throws IOException {
        try {
            StringBuilder sb = new StringBuilder();
            Enumeration lines = msgSrv.getMessage(uid).getAllHeaderLines();
            while (lines.hasMoreElements()) {
                String line = (String) lines.nextElement();
                sb.append(line).append("\n");
            }
            return new ByteArrayInputStream(sb.toString().getBytes());
        } catch (MessagingException ex) {
            throw new AppServerException("Can't get message " + uid + ". Reason: " + uid, ex);
        }
    }

    @Override
    public InputStream getMessage(String uid) throws IOException {
        return msgSrv.getMessageStream(uid);
    }

    @Override
    public List<MessageMetaData> getMessages() throws IOException {
        List<BasicMessage> basicMsgs = msgSrv.getBasicMessages();
        List<MessageMetaData> metas = new ArrayList<>(basicMsgs.size());
        for (BasicMessage basicMsg : basicMsgs) {
            MessageMetaData mmd = new MessageMetaData(basicMsg.getId(), basicMsg.getSize());
            metas.add(mmd);
        }
        return metas;
    }

    @Override
    public void remove(String... uids) throws IOException {
        // ha. Yeah, we'll "Delete" it.
    }

    @Override
    public String getIdentifier() throws IOException {
        return "1";
    }

    @Override
    public void close() throws IOException {
        // do nothing.
    }

}
