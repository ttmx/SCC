package scc.srv.resources;

import scc.entities.Channel;
import scc.entities.User;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/users")
public class UserResource {
    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    User get(@PathParam("id") String id){
        return null;
    }

    @Path("/{id}/channels")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<Channel> channels(@PathParam("id") String id){
        return null;
    }

    @Path("/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    void post(User user){

    }

    @Path("/{id}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    User update(@PathParam("id") String id,User user, @HeaderParam("pass") String password){
        return null;
    }
}
