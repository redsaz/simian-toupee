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

import com.redsaz.simiantoupee.api.model.BasicMessage;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.redsaz.simiantoupee.api.MessagesService;

/**
 * An endpoint for accessing messages.
 *
 * @author Redsaz <redsaz@gmail.com>
 */
@Path("/messages")
public class BrowserMessagesResource {

    private MessagesService messagesSrv;
    private Templater cfg;

    public BrowserMessagesResource() {
    }

    @Inject
    public BrowserMessagesResource(MessagesService messagesService, Templater config) {
        messagesSrv = messagesService;
        cfg = config;
    }

    /**
     * Presents a web page of messages.
     *
     * @param httpRequest The request for the page.
     * @return Messages, by URI and title.
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response listMessages(@Context HttpServletRequest httpRequest) {
        String base = httpRequest.getContextPath();
        String dist = base + "/dist";
        List<BasicMessage> messages = messagesSrv.getBasicMessages();

        Map<String, Object> root = new HashMap<>();
        root.put("messages", messages);
        root.put("base", base);
        root.put("dist", dist);
        root.put("title", "Messages");
        root.put("content", "messages-list.ftl");
        return Response.ok(cfg.buildFromTemplate(root, "page.ftl")).build();
    }

    /**
     * Presents a web page for viewing a specific message.
     *
     * @param httpRequest The request for the page.
     * @param id The id of the message.
     * @return Message view page.
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("{id}")
    public Response getMessage(@Context HttpServletRequest httpRequest, @PathParam("id") String id) {
        String base = httpRequest.getContextPath();
        String dist = base + "/dist";
        BasicMessage message = messagesSrv.getBasicMessage(id);
        if (message == null) {
            throw new NotFoundException("Could not find message id=" + id);
        }
        Map<String, Object> root = new HashMap<>();
        root.put("message", message);
        root.put("base", base);
        root.put("dist", dist);
        root.put("title", message.getSubject());
        root.put("content", "message-view.ftl");
        return Response.ok(cfg.buildFromTemplate(root, "page.ftl")).build();
    }

    @POST
    @Path("delete")
    public Response deleteMessage(@FormParam("id") String id) {
        messagesSrv.deleteMessage(id);
        Response resp = Response.seeOther(URI.create("/messages")).build();
        return resp;
    }

}
