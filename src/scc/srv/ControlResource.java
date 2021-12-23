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
        return "v: 1001";
    }

    @Path("/redis")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String redis() {
        return Env.REDIS_HOSTNAME + "\n";
    }
}
