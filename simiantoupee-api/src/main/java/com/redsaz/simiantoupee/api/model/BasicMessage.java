/**
 * Copyright 2016 Redsaz <redsaz@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.redsaz.simiantoupee.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Contains the title and content of a message.
 *
 * @author Redsaz <redsaz@gmail.com>
 */
public class BasicMessage {

    private final String id;
    private final String subject;
    private final String body;
    private final long size;

    @JsonCreator
    public BasicMessage(
            @JsonProperty("id") String inId,
            @JsonProperty("subject") String inSubject,
            @JsonProperty("body") String inBody,
            @JsonProperty() long inSize) {
        id = inId;
        subject = inSubject;
        body = inBody;
        size = inSize;
    }

    public String getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public long getSize() {
        return size;
    }

}
