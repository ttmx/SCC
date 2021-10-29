package scc.srv.resources;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import scc.Env;
import scc.entities.Channel;
import scc.entities.User;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/users")
public class UserResource {
    public static final String DB_NAME = "users";
    MongoClient mc = new MongoClient(new MongoClientURI(Env.DB_URI));
    MongoDatabase mdb = mc.getDatabase(Env.DB_NAME);
    MongoCollection<Document> mCol = mdb.getCollection(DB_NAME);
    public UserResource(){

    }

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
        if (mCol.find(new Document("_id",user.getId())).first() == null){
            mCol.insertOne(user.toDocument());
        }
    }

    @Path("/")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    void update(User user, @HeaderParam("pass") String password){
        var d = mCol.find(new Document("_id",user.getId())).first();
        if(password.hashCode().equals((String)d.get("pwd")))
        mCol.insertOne(user.toDocument());
        return null;
    }
}
