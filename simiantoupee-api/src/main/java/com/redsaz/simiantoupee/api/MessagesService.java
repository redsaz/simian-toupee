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
package com.redsaz.simiantoupee.api;

import com.redsaz.simiantoupee.api.model.Message;
import java.util.List;

/**
 * Stores and accesses messages.
 *
 * @author Redsaz <redsaz@gmail.com>
 */
public interface MessagesService {

    List<Message> getMessages();

    public Message getMessage(long id);

    public List<Message> createAll(List<Message> messages);

    public List<Message> updateAll(List<Message> messages);

    public void deleteMessage(long id);
}
