package scc.srv.resources;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import scc.Env;
import scc.entities.User;
import scc.utils.Hash;
import scc.utils.Log;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static scc.entities.User.*;

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
    public User getUser(@PathParam("id") String id) {
        return getUserById(id)
                .setPwd(null)
                .setChannelIds(null);
    }

    private User getUserById(String id) {
        Document userDoc = mCol.find(new Document(ID, id)).first();
        if (userDoc == null)
            throw new NotFoundException();
        return User.fromDocument(userDoc);
    }

    @Path("/{id}/channels")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String[] getUserChannels(@PathParam("id") String id) {
        Document userDoc = mCol.find(new Document(ID, id)).first();
        User user;
        if (userDoc != null) {
            user = User.fromDocument(userDoc);
            return user.getChannelIds();
        } else {
            throw new NotFoundException();
        }
    }

    @Path("/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void createUser(User user) {
        Log.d("UserResource", "Creating " + user.toString());

        if (mCol.find(new Document(ID, user.getId())).first() == null) {
            insertUser(user);
        } else {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
    }

    @Path("/")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public void update(User user, @HeaderParam(PWD) String password) {
        Document d = mCol.find(new Document(ID, user.getId())).first();
        if (Hash.of(password).equals(d.get(PWD))) {

            Document update = new Document();
            if (user.getPwd()!= null)
                update.append(PWD,Hash.of(user.getPwd()));
            if(user.getName()!=null)
                update.append(NAME,user.getName());

            insertUser(user);
        }
    }
    private Document a(Document d,String k, String v){
        if (v!=null)
            d.append(k,v);
        return d;
    }

    private void insertUser(User u) {
        if (u.getPwd() != null)
            u.setPwd(Hash.of(u.getPwd()));
        mCol.insertOne(u.toDocument());
    }
}
