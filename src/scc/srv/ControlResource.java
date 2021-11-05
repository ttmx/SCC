package scc.srv;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
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
        return "v: 0006";
    }
    @Path("/vargaming")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String var() {
        var e = System.getenv();
        return e.keySet().stream().reduce("",(a,b) ->b+"="+e.get(b)+"\n"+a);
    }

    @Path("/redis")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String redis() {
        return Env.REDIS_HOSTNAME + "\n" + Env.REDIS_KEY+"\n";
    }
}
