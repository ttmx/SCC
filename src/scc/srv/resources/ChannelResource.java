package scc.srv.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/channels")
public class ChannelResource {
    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    int get(int id){
        return 0;
    }
}
