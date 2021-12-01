package scc.srv;

import scc.Env;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Class with control endpoints.
 */
@Path("/ctrl")
public class ControlResource {

    /**
     * This methods just prints a string. It may be useful to check if the current
     * version is running on Azure.
     */
    @Path("/version")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "v: 0030";
    }

    @Path("/redis")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String redis() {
        return Env.REDIS_HOSTNAME + "\n" + Env.REDIS_KEY + "\n";
    }

    @Path("/search")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String search() {
        return Env.SEARCH_URL + "\n" + Env.SEARCH_KEY + "\n";
    }
}
