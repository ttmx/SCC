package scc.srv.resources;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import scc.entities.Channel;
import scc.entities.Message;
import scc.srv.DataAbstractionLayer;
import scc.utils.Log;
import scc.utils.Redis;

import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/channel")
public class ChannelResource {
    public static final String DB_NAME = "channels";
    MongoCollection<Document> mCol;
    DataAbstractionLayer data;
    Redis redis = Redis.getInstance();

    public ChannelResource(DataAbstractionLayer data) {
        mCol = data.getChannelCol();
        this.data = data;
    }

    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Channel getChannel(@CookieParam(UserResource.SESSION_COOKIE) Cookie session, @PathParam("id") String id) {
        try {
            String userId = this.redis.getUserfromCookie(session);
            Document channelDoc = this.mCol.find(new Document("_id", id)).first();
            if (channelDoc != null) {
                Channel c = Channel.fromDocument(channelDoc);
                if (c.hasMember(userId)) {
                    return c;
                } else if (c.getPublicChannel()) {
                    c.setMembers(null);
                    return c;
                } else {
                    throw new NotAuthorizedException("Cannot access channels without a valid session.");
                }
            }
        } catch(WebApplicationException e) {
            throw e;
        } catch(Exception e) {
            throw new InternalServerErrorException(e);
        }
        throw new NotFoundException();
    }

    @Path("/{id}")
    @DELETE
    public void deleteChannel(@CookieParam(UserResource.SESSION_COOKIE) Cookie session, @PathParam("id") String id) {
        // TODO Authenticate, garbage collect users and messages
        Document channelDoc = this.mCol.find(new Document("_id", id)).first();
        if(channelDoc != null) {
            try {
                this.redis.checkCookieUser(session, Channel.fromDocument(channelDoc).getOwner());
                this.mCol.deleteOne(new Document("_id", id));

            } catch (WebApplicationException e) {
                throw e;
            } catch (Exception e) {
                throw new InternalServerErrorException(e);
            }
        }
        throw new NotFoundException();
    }

    @Path("/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createChannel(@CookieParam(UserResource.SESSION_COOKIE) Cookie session, Channel channel) {
        Log.d("ChannelResource", "Creating " + channel.toString());

        // TODO Error handling
        try {
            String owner = channel.getOwner();
            this.redis.checkCookieUser(session, owner);
            // Generate an id for the message
            UUID uuid = UUID.randomUUID();
            channel.setId(uuid.toString());
            this.insertChannel(channel);
            this.mCol.updateOne(new Document("_id", channel.getId()), new Document("$addToSet" , new Document("members", owner)));
            data.getUserCol().updateOne(new Document("_id", owner), new Document("$addToSet" , new Document("channelIds", channel.getId())));

            return channel.getId();

        } catch(WebApplicationException e) {
            throw e;
        } catch(Exception e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Path("/{id}/add/{userId}")
    @POST
    public void addUserToChannel(@CookieParam(UserResource.SESSION_COOKIE) Cookie session, @PathParam("id") String id, @PathParam("userId") String userId) {
        Document channelDoc = this.mCol.find(new Document("_id", id)).first();
        try {
            if (channelDoc != null) {
                Channel c = Channel.fromDocument(channelDoc);
                this.redis.checkCookieUser(session, c.getOwner());
                // Add user to channel
                this.mCol.updateOne(new Document("_id", id), new Document("$addToSet" , new Document("members", userId)));

                // Add channel to user
                data.getUserCol().updateOne(new Document("_id", userId), new Document("$addToSet" , new Document("channelIds", id)));
            } else {
                throw new BadRequestException();
            }
        } catch(WebApplicationException e) {
            throw e;
        } catch(Exception e) {
            throw new InternalServerErrorException(e);
        }

    }

    @Path("/{id}/remove/{userId}")
    @DELETE
    public void removeUserFromChannel(@CookieParam(UserResource.SESSION_COOKIE) Cookie session, @PathParam("id") String id, @PathParam("userId") String userId) {
        try {
            String userAccessingId = this.redis.getUserfromCookie(session);
            Document channelDoc = this.mCol.find(new Document("_id", id)).first();
            if (channelDoc != null) {
                if(userAccessingId.equals(Channel.fromDocument(channelDoc).getOwner())) {
                    // Remove user from channel
                    this.mCol.updateOne(new Document("_id", id), new Document("$pull", new Document("members", userId)));

                    // Remove channel from user
                    data.getUserCol().updateOne(new Document("_id", userId), new Document("$pull", new Document("channelIds", id)));
                } else throw new NotAuthorizedException("Only the owner of this channel can remove other users.");
            } else {
                throw new BadRequestException();
            }
        } catch(WebApplicationException e) {
            throw e;
        } catch(Exception e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Path("/{id}/messages")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Object[] getChannelMessages(@CookieParam(UserResource.SESSION_COOKIE) Cookie session, @PathParam("id") String id, @QueryParam("st") int start, @QueryParam("len") int length) {
        try {
            String userId = this.redis.getUserfromCookie(session);
            Document channelDoc = this.mCol.find(new Document("_id", id)).first();
            if(channelDoc != null) {
                Channel c = Channel.fromDocument(channelDoc);
                if (c.hasMember(userId)) {
                    List<Document> messageDocs = this.data.getMessageCol().find(new Document("channel", id)).skip(start).limit(length).into(new ArrayList<>());
                    if (messageDocs != null) { //TODO maybe this should be a !empty check
                        return messageDocs.stream().map(Message::fromDocument).toArray();
                    }
                } else throw new NotAuthorizedException("Cannot access messages from a channel one doesn't belong to.");
            }
        } catch(WebApplicationException e) {
            throw e;
        } catch(Exception e) {
            throw new InternalServerErrorException(e);
        }
        throw new NotFoundException();
    }

    @Path("/{id}/users")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String[] getChannelMembers(@CookieParam(UserResource.SESSION_COOKIE) Cookie session, @PathParam("id") String id) {
        try {
            String userId = this.redis.getUserfromCookie(session);
            Document channelDoc = this.mCol.find(new Document("_id", id)).first();
            if(channelDoc != null) {
                Channel c = Channel.fromDocument(channelDoc);
                if (c.hasMember(userId)) {
                    return c.getMembers();
                } else throw new NotAuthorizedException("Cannot access messages from a channel one doesn't belong to.");
            }
        } catch(WebApplicationException e) {
            throw e;
        } catch(Exception e) {
            throw new InternalServerErrorException(e);
        }
        throw new NotFoundException();
    }

    private void insertChannel(Channel c) {
        this.mCol.insertOne(c.toDocument());
    }
}
