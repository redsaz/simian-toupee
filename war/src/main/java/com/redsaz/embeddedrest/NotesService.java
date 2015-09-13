/*
 * Copyright 2015 Redsaz <redsaz@gmail.com>.
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
package com.redsaz.embeddedrest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * An endpoint for accessing notes. The REST endpoints and browser endpoints are
 * identical; look at docs/endpoints.md for why.
 *
 * @author Redsaz <redsaz@gmail.com>
 */
@Path("/notes")
public class NotesService {

    @Inject
    @ApplicationScoped
    private NotesResource notesRes;

    /**
     * Lists all of the notes URI and titles.
     *
     * @return Notes, by URI and title.
     */
    @GET
    @Produces(MediaType.NOTES_V1_JSON)
    public Response listNotes() {
        return Response.status(200).entity(notesRes.getNotes()).build();
    }

    /**
     * Lists all of the notes URI and titles.
     *
     * @param uriName The note to get.
     * @return Notes, by URI and title.
     */
    @GET
    @Produces(MediaType.NOTES_V1_JSON)
    @Path("{uriName}")
    public Response getNote(@PathParam("uriName") String uriName) {
        return Response.status(200).entity(notesRes.getNote(uriName)).build();
    }

    /**
     * Lists all of the notes URI and titles.
     *
     * @return Notes, by URI and title.
     */
    @GET
    public Response asdfaasdf() {
        return Response.status(200).entity("asdf aasfd").build();
    }
}
