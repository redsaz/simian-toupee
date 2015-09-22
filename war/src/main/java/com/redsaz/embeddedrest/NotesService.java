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

import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * An endpoint for accessing notes. The REST endpoints and browser endpoints are
 * identical; look at docs/endpoints.md for why.
 *
 * @author Redsaz <redsaz@gmail.com>
 */
@Path("/notes")
public class NotesService {

    private NotesResource notesRes;

    public NotesService() {
    }

    @Inject
    public NotesService(NotesResource notesResource) {
        notesRes = notesResource;
    }

    /**
     * Lists all of the notes URI and titles.
     *
     * @return Notes, by URI and title.
     */
    @GET
    @Produces(EmbeddedRestMediaType.NOTES_V1_JSON)
    public Response listNotes() {
        return Response.ok(notesRes.getNotes()).build();
    }

    /**
     * Get the note contents.
     *
     * @param id The id of the note.
     * @param uriName The uri title of the note, not used in retrieving the data
     * but helpful for users which may read the note.
     * @return Note.
     */
    @GET
    @Produces({EmbeddedRestMediaType.NOTE_V1_JSON})
    @Path("{id}/{uriName}")
    public Response getNote(@PathParam("id") long id, @PathParam("uriName") String uriName) {
        Note note = notesRes.getNote(id);
        if (note == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(note).build();
    }

    /**
     * Get the note contents.
     *
     * @param id The id of the note.
     * @return Note.
     */
    @GET
    @Produces({EmbeddedRestMediaType.NOTE_V1_JSON})
    @Path("{id}")
    public Response getNoteById(@PathParam("id") long id) {
        Note note = notesRes.getNote(id);
        if (note == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(note).build();
    }

    @POST
    @Consumes(EmbeddedRestMediaType.NOTES_V1_JSON)
    @Produces({EmbeddedRestMediaType.NOTES_V1_JSON})
    public Response createNotes(List<Note> notes) {
        return Response.status(Status.CREATED).entity(notesRes.createAll(notes)).build();
    }

    @POST
    @Consumes(EmbeddedRestMediaType.NOTE_V1_JSON)
    @Produces({EmbeddedRestMediaType.NOTE_V1_JSON})
    public Response createNote(Note note) {
        List<Note> notes = Collections.singletonList(note);
        return Response.status(Status.CREATED).entity(notesRes.createAll(notes)).build();
    }

    @PUT
    @Consumes(EmbeddedRestMediaType.NOTES_V1_JSON)
    @Produces({EmbeddedRestMediaType.NOTES_V1_JSON})
    public Response updateNotes(List<Note> notes) {
        return Response.status(Status.ACCEPTED).entity(notesRes.updateAll(notes)).build();
    }

    @PUT
    @Consumes(EmbeddedRestMediaType.NOTE_V1_JSON)
    @Produces({EmbeddedRestMediaType.NOTE_V1_JSON})
    @Path("{id}")
    public Response updateNote(@PathParam("id") long id, Note note) {
        Note withId = new Note(id, note.getUriName(), note.getTitle(), note.getBody());
        List<Note> notes = Collections.singletonList(withId);
        return Response.status(Status.ACCEPTED).entity(notesRes.updateAll(notes)).build();
    }

    @DELETE
    @Path("{id}")
    public Response deleteNote(@PathParam("id") long id) {
        notesRes.deleteNote(id);
        return Response.status(Status.NO_CONTENT).build();
    }
}
