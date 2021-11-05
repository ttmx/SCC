package scc.srv.resources;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
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

import static scc.entities.User.ID;


@Path("/messages")
public class MessageResource {
    public static final String DB_NAME = "messages";
    MongoCollection<Document> mCol;
    DataAbstractionLayer data;
    Redis redis = Redis.getInstance();

    public MessageResource(DataAbstractionLayer data) {
        mCol = data.getMessageCol();
        this.data = data;
    }

    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Message getMessage(@CookieParam(UserResource.SESSION_COOKIE) Cookie session, @PathParam("id") String id) {
        try {
            String userId = this.redis.getUserfromCookie(session);
            Document messageDoc = mCol.find(new Document("_id", id)).first();
            if (messageDoc != null) {
                Message m = Message.fromDocument(messageDoc);
                Document channelDoc = this.data.getChannelCol().find(new Document("_id", m.getChannel())).first();
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
        // TODO Authenticate, garbage collect
        try {
            String userId = this.redis.getUserfromCookie(session);
            Document messageDoc = mCol.find(new Document("_id", id)).first();
            if (messageDoc != null) {
                Message m = Message.fromDocument(messageDoc);
                Document channelDoc = this.data.getChannelCol().find(new Document("_id", m.getChannel())).first();
                if(channelDoc != null){
                    Channel c = Channel.fromDocument(channelDoc);
                    if( c.hasMember(userId) && (c.getOwner().equals(userId)) || m.getUser().equals(userId)) {
                        this.mCol.deleteOne(new Document("_id", id));
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

        // TODO Error handling, can channel not exist?
        try {
            this.redis.checkCookieUser(session, message.getUser());
            Document channelDoc = this.data.getChannelCol().find(new Document("_id", message.getChannel())).first();
            if(channelDoc != null && (message.getReplyTo().equals("") || mCol.find(new Document("_id", message.getReplyTo())).first() != null)) {
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
        mCol.insertOne(m.toDocument());
    }
}
