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

import com.redsaz.simiantoupee.api.model.BasicMessage;
import java.io.InputStream;
import java.util.List;
import javax.mail.internet.MimeMessage;

/**
 * Stores and accesses messages.
 *
 * @author Redsaz <redsaz@gmail.com>
 */
public interface MessagesService {

    List<BasicMessage> getBasicMessages();

    public BasicMessage getBasicMessage(String id);

    public MimeMessage getMessage(String id);

    public InputStream getMessageStream(String id);

    public String create(InputStream messageStream);

    public void deleteMessage(String id);
}
