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

import com.github.slugify.Slugify;
import com.redsaz.simiantoupee.store.HsqlMessagesService;
import com.redsaz.simiantoupee.api.exceptions.AppClientException;
import com.redsaz.simiantoupee.api.exceptions.AppServerException;
import com.redsaz.simiantoupee.api.model.Message;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import com.redsaz.simiantoupee.api.MessagesService;

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

    private static final Slugify SLG = initSlug();
    private static final int SHORTENED_MAX = 60;
    private static final int SHORTENED_MIN = 12;

    private final MessagesService srv;

    public SanitizedMessagesService() {
        srv = new HsqlMessagesService();
    }

    @Override
    public List<Message> getMessages() {
        return sanitizeAll(srv.getMessages());
    }

    @Override
    public Message getMessage(long id) {
        return sanitize(srv.getMessage(id));
    }

    @Override
    public List<Message> createAll(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }
        return srv.createAll(sanitizeAll(messages));
    }

    @Override
    public List<Message> updateAll(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }
        return srv.updateAll(sanitizeAll(messages));
    }

    @Override
    public void deleteMessage(long id) {
        srv.deleteMessage(id);
    }

    /**
     * Sanitizes a group of messages according to the
     * {@link #sanitize(com.redsaz.simiantoupee.api.model.Message)} method.
     *
     * @param messages The messages to sanitize
     * @return A List of new message instances with sanitized data.
     */
    private static List<Message> sanitizeAll(List<Message> messages) {
        List<Message> sanitizeds = new ArrayList<>(messages.size());
        for (Message message : messages) {
            sanitizeds.add(sanitize(message));
        }
        return sanitizeds;
    }

    /**
     * A message must have at least a uri, a title, and/or a body. If none of
     * them are present then message cannot be sanitized. The ID will remain
     * unchanged.
     *
     * @param message The message to sanitize
     * @return A new message instance with sanitized data.
     */
    private static Message sanitize(Message message) {
        String uriName = message.getUriName();
        if (uriName == null || uriName.isEmpty()) {
            uriName = message.getTitle();
            if (uriName == null || uriName.isEmpty()) {
                uriName = shortened(message.getBody());
                if (uriName == null || uriName.isEmpty()) {
                    throw new AppClientException("Message must have at least a uri, title, or body.");
                }
            }
        }
        uriName = SLG.slugify(uriName);

        String title = message.getTitle();
        if (title == null) {
            title = shortened(message.getBody());
            if (title == null) {
                title = "";
            }
        }
        String body = message.getBody();
        if (body == null) {
            body = "";
        }

        return new Message(message.getId(), uriName, title, body);
    }

    private static String shortened(String text) {
        if (text == null || text.length() <= SHORTENED_MAX) {
            return text;
        }
        text = text.substring(0, SHORTENED_MAX);
        String candidate = text.replaceFirst("\\S+$", "");
        if (candidate.length() < SHORTENED_MIN) {
            candidate = text;
        }

        return candidate + "...";
    }

    private static Slugify initSlug() {
        Slugify sluggy;
        try {
            sluggy = new Slugify();
        } catch (IOException ex) {
            throw new AppServerException("Couldn't initialize Slugify.");
        }
        return sluggy;
    }

}
