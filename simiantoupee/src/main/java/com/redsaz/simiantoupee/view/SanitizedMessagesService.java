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
package com.redsaz.simiantoupee.view;

import com.redsaz.simiantoupee.store.HsqlMessagesService;
import com.redsaz.simiantoupee.api.model.BasicMessage;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import com.redsaz.simiantoupee.api.MessagesService;
import java.io.InputStream;
import javax.mail.internet.MimeMessage;

/**
 * Does not directly store messages, but is responsible for ensuring that the
 * messages sent to and retrieved from the store are correctly formatted, sized,
 * and without malicious/errorific content.
 *
 * @author Redsaz <redsaz@gmail.com>
 */
@Default
@ApplicationScoped
public class SanitizedMessagesService implements MessagesService {

    private final MessagesService srv;

    public SanitizedMessagesService() {
        srv = new HsqlMessagesService();
    }

    @Override
    public BasicMessage getBasicMessage(String id) {
        return srv.getBasicMessage(id);
    }

    @Override
    public void deleteMessage(String id) {
        srv.deleteMessage(id);
    }

    @Override
    public List<BasicMessage> getBasicMessages() {
        return srv.getBasicMessages();
    }

    @Override
    public MimeMessage getMessage(String id) {
        return srv.getMessage(id);
    }

    @Override
    public InputStream getMessageStream(String id) {
        return srv.getMessageStream(id);
    }

    @Override
    public String create(InputStream messageStream) {
        if (messageStream == null) {
            return null;
        }
        return srv.create(messageStream);
    }

}
