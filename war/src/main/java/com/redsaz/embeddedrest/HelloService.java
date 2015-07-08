package com.redsaz.embeddedrest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/hello")
public class HelloService {

    @Inject
    @ApplicationScoped
    private HelloResource bob;

    @GET
    public Response responseMsg() {
        if (bob != null) {
            String response = bob.hello();
            return Response.status(200).entity(response).build();
        } else {
            return Response.status(200).entity("Bob was not injected.").build();
        }
    }
}
