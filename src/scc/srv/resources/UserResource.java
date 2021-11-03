package scc.srv.resources;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import scc.Env;
import scc.entities.Channel;
import scc.entities.User;
import scc.utils.Hash;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/user")
public class UserResource {
    public static final String DB_NAME = "users";
    MongoClient mc = new MongoClient(new MongoClientURI(Env.DB_URI));
    MongoDatabase mdb = mc.getDatabase(Env.DB_NAME);
    MongoCollection<Document> mCol = mdb.getCollection(DB_NAME);

    public UserResource() {
    }

    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public User getUser(@PathParam("id") String id){
        Document userDoc = mCol.find(new Document("_id", id) ).first();
        if (userDoc != null) {
            return User.fromDocument(userDoc);
        }
        return new User("id","name","pwd","photoid",null);
    }

    @Path("/{id}/channels")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String[] getUserChannels(@PathParam("id") String id) {
        Document userDoc = mCol.find(new Document("_id", id) ).first();
        User user;
        if (userDoc != null) {
            user = User.fromDocument(userDoc);
            return user.getChannelIds();
        }
        else {
            return null;
        }
    }

    @Path("/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void createUser(User user){
        System.out.println("Tried to create user");
        if (mCol.find(new Document("_id",user.getId())).first() == null) {
            insertUser(user);
        }
    }

    @Path("/")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public void update(User user, @HeaderParam("pass") String password){
        var d = mCol.find(new Document("_id",user.getId())).first();
        if(Hash.of(password).equals(d.get("pwd"))){
            insertUser(user);
        }
    }

    private void insertUser(User u){
        u.setPwd(Hash.of(u.getPwd()));
        mCol.insertOne(u.toDocument());
    }
}
