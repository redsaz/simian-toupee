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
 * Contains the name and address of an email Sender, To, From, CC, BCC.
 *
 * @author Redsaz <redsaz@gmail.com>
 */
public class MessageAddress {

    private final long id;
    private final String address;
    private final String name;

    @JsonCreator
    public MessageAddress(
            @JsonProperty("id") long inId,
            @JsonProperty("address") String inAddress,
            @JsonProperty("name") String inName) {
        id = inId;
        address = inAddress;
        name = inName;
    }

    public long getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (name != null) {
            sb.append("\"").append(getName()).append("\" ");
        }
        sb.append("<").append(getAddress()).append(">");
        return sb.toString();
    }
}
