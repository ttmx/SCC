package scc.srv.resources;

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
    DataAbstractionLayer data;
    public static final String SESSION_COOKIE = "session";
    Redis redis = Redis.getInstance();

    public UserResource(DataAbstractionLayer data) {
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
        NewCookie cookie = new NewCookie(SESSION_COOKIE, uid, "/", null,
                "sessionid", 3600, false, true);
        this.redis.putSession(new Session(uid, ua.getUser()));
        return Response.ok().cookie(cookie).build();
    }



    @Path("/checkcookie/{id}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response checkCookie(@CookieParam(SESSION_COOKIE) Cookie s,@PathParam("id") String userId){
        System.out.println(s.getValue());
        this.redis.checkCookieUser(s,userId);
        return Response.ok().build();

    }

    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public User getUserEndpoint(@CookieParam(SESSION_COOKIE) Cookie session, @PathParam("id") String id) {
        User user = getUser(id).setPwd(null);
        try {
            this.redis.checkCookieUser(session, id);
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
    public void deleteUser(@CookieParam(SESSION_COOKIE) Cookie session, @PathParam("id") String id) {
        // TODO Authenticate, garbage collect avatar and channels
        this.redis.checkCookieUser(session, id);
        this.data.deleteOneDocument(id, new Document(ID, id), DataAbstractionLayer.USER);
    }

    private User getUser(String id) throws NotFoundException {
        Document userDoc = this.data.getDocument(id, new Document(ID, id), DataAbstractionLayer.USER).first();
        if (userDoc == null)
            throw new NotFoundException();
        return User.fromDocument(userDoc);
    }

    @Path("/{id}/channels")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String[] getUserChannels(@CookieParam(SESSION_COOKIE) Cookie session, @PathParam("id") String id) {
        try {
            this.redis.checkCookieUser(session, id);
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

        if (this.data.getDocument(user.getId(),new Document(ID, user.getId()), DataAbstractionLayer.USER).first() == null) {
            user.setChannelIds(new String[0]);
            user.setPwd(Hash.of(user.getPwd()));
            this.data.insertOneDocument(user.getId(), user.toDocument(), DataAbstractionLayer.USER);
        } else {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
    }

    @Path("/{id}/subscribe/{channelId}")
    @POST
    public void addChannelToUser(@CookieParam(SESSION_COOKIE) Cookie session, @PathParam("id") String id, @PathParam("channelId") String channelId) {
        // TODO deal with authentication for this to work, this only works for public channels?
        try {
            this.redis.checkCookieUser(session, id);
            Document channelDoc = this.data.getDocument(channelId, new Document("_id", channelId), DataAbstractionLayer.CHANNEL).first();

            if(channelDoc == null) {
                throw new BadRequestException();
            }

            Channel channel = Channel.fromDocument(channelDoc);

            if (channel.getPublicChannel()) {
                // Add user to channel
                this.data.updateOneDocument(channelId, new Document("_id", channelId), new Document("$addToSet" , new Document("members", id)), DataAbstractionLayer.CHANNEL);

                // Add channel to user
                this.data.updateOneDocument(id, new Document("_id", id), new Document("$addToSet" , new Document("channelIds", channelId)), DataAbstractionLayer.USER);
            } else {
                throw new ForbiddenException();
            }
        } catch(WebApplicationException e) {
            throw e;
        } catch(Exception e) {
            throw new InternalServerErrorException(e);
        }

    }

    @Path("/{id}/unsubscribe/{channelId}")
    @DELETE
    public void removeChannelToUser(@CookieParam(SESSION_COOKIE) Cookie session, @PathParam("id") String id, @PathParam("channelId") String channelId) {
        // TODO deal with authentication for this to work, this only works for public channels?
        this.redis.checkCookieUser(session, id);
        Document channelDoc = this.data.getDocument(channelId, new Document("_id", channelId), DataAbstractionLayer.CHANNEL).first();

        if(channelDoc == null) {
            throw new BadRequestException();
        }

        Channel channel = Channel.fromDocument(channelDoc);
        if (channel.hasMember(id)) {
            // Remove user from channel
            this.data.updateOneDocument(channelId, new Document("_id", channelId), new Document("$pull" , new Document("members", id)), DataAbstractionLayer.CHANNEL);

            // Remove channel from user
            this.data.updateOneDocument(id, new Document("_id", id), new Document("$pull" , new Document("channelIds", channelId)), DataAbstractionLayer.USER);

        } else {
            throw new ForbiddenException();
        }
    }

    @Path("/")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateUser(@CookieParam(SESSION_COOKIE) Cookie session, User user) {
        if (user == null) {
            throw new BadRequestException();
        }

        String userId = this.redis.getUserfromCookie(session);
        Log.d("UserResource", "Update " + user);
        Document userDoc = this.data.getDocument(userId, new Document(ID, userId), DataAbstractionLayer.USER).first();
        if(userDoc != null) {
            Document update = new Document();
            if (user.getPwd() != null)
                update.append(PWD, Hash.of(user.getPwd()));
            if (user.getName() != null)
                update.append(NAME, user.getName());
            if (user.getPhotoId() != null)
                update.append(PHOTOID, user.getPhotoId());

            this.data.updateOneDocument(userId, new Document(ID, userId), new Document("$set", update), DataAbstractionLayer.USER);

        } else throw new NotFoundException();
    }

}
