package scc.srv.resources;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import scc.Env;
import scc.entities.Session;
import scc.entities.User;
import scc.entities.UserAuth;
import scc.utils.Hash;
import scc.utils.Log;
import scc.utils.Redis;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static scc.entities.User.*;

@Path("/user")
public class UserResource {
    public static final String DB_NAME = "users";
    MongoClient mc = new MongoClient(new MongoClientURI(Env.DB_URI));
    MongoDatabase mdb = mc.getDatabase(Env.DB_NAME);
    MongoCollection<Document> mCol = mdb.getCollection(DB_NAME);

    public UserResource() {
    }

    @Path("/auth")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getCookie(UserAuth ua) {
        if (!ua.isValid())
            throw new BadRequestException();

        User u = getUser(ua.getUser());
        if (!Hash.of(ua.getPwd()).equals(u.getPwd())) {
            throw new ForbiddenException();
        }
        String uid = UUID.randomUUID().toString();
        NewCookie cookie = new NewCookie("scc:session", uid, "/", null,
                "sessionid", 3600, false, true);
        Redis.getInstance().putSession(new Session(uid, ua.getUser()));
        return Response.ok().cookie(cookie).build();
    }

    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public User getUserEndpoint(@PathParam("id") String id) {
        return getUser(id)
                .setPwd(null)
                .setChannelIds(null);
    }

    private User getUser(String id) throws NotFoundException {
        Document userDoc = mCol.find(new Document(ID, id)).first();
        if (userDoc == null)
            throw new NotFoundException();
        return User.fromDocument(userDoc);
    }

    @Path("/{id}/channels")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String[] getUserChannels(@PathParam("id") String id) {
        User user = getUser(id);
        return user.getChannelIds();
    }

    @Path("/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void createUser(User user) {
        if (user == null || user.getId() == null || user.getPwd() == null) {
            throw new BadRequestException();
        }
        Log.d("UserResource", "Creating " + user);

        if (mCol.find(new Document(ID, user.getId())).first() == null) {
            user.setChannelIds(new String[0]);
            user.setPwd(Hash.of(user.getPwd()));
            mCol.insertOne(user.toDocument());
        } else {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
    }

    @Path("/")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public void update(User user, @HeaderParam("pwd") String password) {
        if (user == null || user.getId() == null) {
            throw new BadRequestException();
        }
        Log.d("UserResource", "Creating " + user);
        Document d = mCol.find(new Document(ID, user.getId())).first();
        assert d != null;
        if (Hash.of(password).equals(d.get(PWD))) {

            Document update = new Document();
            if (user.getPwd() != null)
                update.append(PWD, Hash.of(user.getPwd()));
            if (user.getName() != null)
                update.append(NAME, user.getName());
            if (user.getPhotoId() != null)
                update.append(PHOTOID, user.getPhotoId());

            mCol.updateOne(new Document(ID, user.getId()), update);
        }
    }

}
