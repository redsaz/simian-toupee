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
 * @author shayne
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
