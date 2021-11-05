package scc.srv.resources;


import org.bson.Document;
import scc.entities.Channel;
import scc.entities.Message;
import scc.srv.DataAbstractionLayer;
import scc.utils.Log;
import scc.utils.Redis;

import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import java.util.UUID;

@Path("/messages")
public class MessageResource {
    public static final String DB_NAME = "messages";
    DataAbstractionLayer data;
    Redis redis = Redis.getInstance();

    public MessageResource(DataAbstractionLayer data) {
        this.data = data;
    }

    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Message getMessage(@CookieParam(UserResource.SESSION_COOKIE) Cookie session, @PathParam("id") String id) {
        try {
            String userId = this.redis.getUserfromCookie(session);
            Document messageDoc = this.data.getDocument(id, new Document("_id", id), DataAbstractionLayer.MESSAGE);
            if (messageDoc != null) {
                Message m = Message.fromDocument(messageDoc);
                Document channelDoc = this.data.getDocument(m.getChannel(), new Document("_id", m.getChannel()), DataAbstractionLayer.CHANNEL);
                if(channelDoc != null && Channel.fromDocument(channelDoc).hasMember(userId)) {
                    return m;
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
    public void deleteMessage(@CookieParam(UserResource.SESSION_COOKIE) Cookie session, @PathParam("id") String id) {
        // TODO garbage collect
        try {
            String userId = this.redis.getUserfromCookie(session);
            Document messageDoc = this.data.getDocument(id, new Document("_id", id), DataAbstractionLayer.MESSAGE);
            if (messageDoc != null) {
                Message m = Message.fromDocument(messageDoc);
                Document channelDoc = this.data.getDocument(m.getChannel(), new Document("_id", m.getChannel()), DataAbstractionLayer.CHANNEL);
                if(channelDoc != null){
                    Channel c = Channel.fromDocument(channelDoc);
                    if( c.hasMember(userId) && (c.getOwner().equals(userId)) || m.getUser().equals(userId)) {
                        this.data.deleteOneDocument(id, new Document("_id", id), DataAbstractionLayer.MESSAGE);
                    } else throw new NotAuthorizedException("Cannot delete this message");
                }
            } else throw new NotFoundException();
        } catch(WebApplicationException e) {
            throw e;
        } catch(Exception e) {
            throw new InternalServerErrorException(e);
        }

    }

    @Path("/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createMessage(@CookieParam(UserResource.SESSION_COOKIE) Cookie session, Message message) {
        Log.d("MessageResource", "Creating " + message.toString());

        try {
            this.redis.checkCookieUser(session, message.getUser());
            Document channelDoc = this.data.getDocument(message.getChannel(), new Document("_id", message.getChannel()), DataAbstractionLayer.CHANNEL);
            if(channelDoc != null && (message.getReplyTo().equals("") || this.data.getDocument(message.getReplyTo(), new Document("_id", message.getReplyTo()), DataAbstractionLayer.MESSAGE) != null)) {
                UUID uuid = UUID.randomUUID();
                message.setId(uuid.toString());

                this.insertMessage(message);

                return message.getId();
            }
        } catch(WebApplicationException e) {
            throw e;
        } catch(Exception e) {
            throw new InternalServerErrorException(e);
        }
        throw new BadRequestException();
    }

    private void insertMessage(Message m) {
        this.data.insertOneDocument(m.getId(), m.toDocument(), DataAbstractionLayer.MESSAGE);
    }
}
