package scc.srv.resources;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import scc.entities.Channel;
import scc.entities.Session;
import scc.entities.User;
import scc.entities.UserAuth;
import scc.srv.DataAbstractionLayer;
import scc.utils.Hash;
import scc.utils.Log;
import scc.utils.Redis;

import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static scc.entities.User.*;

@Path("/user")
public class UserResource {
    public static final String DB_NAME = "users";
    MongoCollection<Document> mCol;
    DataAbstractionLayer data;

    public UserResource(DataAbstractionLayer data) {
        mCol = data.getUserCol();
        this.data = data;
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
        NewCookie cookie = new NewCookie("session", uid, "/", null,
                "sessionid", 3600, false, true);
        Redis.getInstance().putSession(new Session(uid, ua.getUser()));
        return Response.ok().cookie(cookie).build();
    }



    @Path("/checkcookie/{id}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response checkCookie(@CookieParam("session") Cookie s,@PathParam("id") String userId){
        System.out.println(s.getValue());
        Redis.getInstance().checkCookieUser(s,userId);
        return Response.ok().build();

    }

    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public User getUserEndpoint(@CookieParam("scc:session") Cookie session, @PathParam("id") String id) {
        User user = getUser(id).setPwd(null);
        try {
            Redis.getInstance().checkCookieUser(session, id);
            return user;
        } catch (NotAuthorizedException e) {
            return user.setChannelIds(null);
        } catch(WebApplicationException e) {
            throw e;
        } catch(Exception e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Path("/{id}")
    @DELETE
    public void deleteUser(@CookieParam("scc:session") Cookie session, @PathParam("id") String id) {
        // TODO Authenticate, garbage collect avatar and channels
        try {
            Redis.getInstance().checkCookieUser(session, id);
            DeleteResult result = this.mCol.deleteOne(new Document(ID, id));
            //TODO v is this necessary?
            if (result.getDeletedCount() == 0) throw new NotFoundException();
        } catch(WebApplicationException e) {
            throw e;
        } catch(Exception e) {
            throw new InternalServerErrorException(e);
        }
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
    public String[] getUserChannels(@CookieParam("scc:session") Cookie session, @PathParam("id") String id) {
        try {
            Redis.getInstance().checkCookieUser(session, id);
            User user = getUser(id);
            return user.getChannelIds();
        } catch(WebApplicationException e) {
            throw e;
        } catch(Exception e) {
            throw new InternalServerErrorException(e);
        }
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

    @Path("/{id}/subscribe/{channelId}")
    @POST
    public void addChannelToUser(@CookieParam("scc:session") Cookie session, @PathParam("id") String id, @PathParam("channelId") String channelId) {
        // TODO deal with authentication for this to work, this only works for public channels?
        try {
            Redis.getInstance().checkCookieUser(session, id);
            Document channelDoc = this.data.getChannelCol().find(new Document("_id", channelId)).first();

            if(channelDoc == null) {
                throw new BadRequestException();
            }

            Channel channel = Channel.fromDocument(channelDoc);

            if (channel.getPublicChannel()) {
                // Add user to channel
                this.data.getChannelCol().updateOne(new Document("_id", channelId), new Document("$addToSet" , new Document("members", id)));

                // Add channel to user
                this.mCol.updateOne(new Document("_id", id), new Document("$addToSet" , new Document("channelIds", channelId)));
            } else {
                throw new ForbiddenException();
            }
        } catch(WebApplicationException e) {
            throw e;
        } catch(Exception e) {
            throw new InternalServerErrorException(e);
        }

    }

    @Path("/")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateUser(@CookieParam("scc:session") Cookie session, User user, @HeaderParam("pwd") String password) {
        if (user == null || user.getId() == null) {
            throw new BadRequestException();
        }

        try {
            Redis.getInstance().checkCookieUser(session, user.getId());
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
        } catch(WebApplicationException e) {
            throw e;
        } catch(Exception e) {
            throw new InternalServerErrorException(e);
        }

    }

}
