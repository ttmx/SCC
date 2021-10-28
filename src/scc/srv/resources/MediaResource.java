package scc.srv.resources;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/media")
public class MediaResource {
    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    byte[] get(int id){
        return null;
    }
}
