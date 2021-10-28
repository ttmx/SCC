package scc.srv.resources;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/messages")
public class MessageResource {
    @Path("/{channel}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    int post(int channel){
        return 0;
    }
}
