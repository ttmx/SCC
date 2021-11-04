package scc.srv.resources;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import scc.entities.Channel;
import scc.entities.Message;
import scc.srv.DataAbstractionLayer;
import scc.utils.Log;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/channel")
public class ChannelResource {
    public static final String DB_NAME = "channels";
    MongoCollection<Document> mCol;
    DataAbstractionLayer data;

    public ChannelResource(DataAbstractionLayer data) {
        mCol = data.getChannelCol();
        this.data = data;
    }

    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Channel getChannel(@PathParam("id") String id) {
        Document channelDoc = this.mCol.find(new Document("_id", id)).first();
        if (channelDoc != null) {
            return Channel.fromDocument(channelDoc);
        }
        throw new NotFoundException();
    }

    @Path("/{id}")
    @DELETE
    public void deleteChannel(@PathParam("id") String id) {
        // TODO Authenticate, garbage collect users and messages

        DeleteResult result = this.mCol.deleteOne(new Document("_id", id));

        if (result.getDeletedCount() == 0) throw new NotFoundException();
    }

    @Path("/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createChannel(Channel channel) {
        Log.d("ChannelResource", "Creating " + channel.toString());

        // TODO Error handling

        // Generate an id for the message
        UUID uuid = UUID.randomUUID();
        channel.setId(uuid.toString());

        this.insertChannel(channel);

        return channel.getId();
    }

    @Path("/{id}/add/{userId}")
    @POST
    public void addUserToChannel(@PathParam("id") String id, @PathParam("userId") String userId) {
        Document channelDoc = this.mCol.find(new Document("_id", id)).first();

        if (channelDoc != null) {
            // Add user to channel
            this.mCol.updateOne(new Document("_id", id), new Document("$addToSet" , new Document("members", userId)));

            // Add channel to user
            data.getUserCol().updateOne(new Document("_id", userId), new Document("$addToSet" , new Document("channelIds", id)));
        } else {
            throw new BadRequestException();
        }
    }

    @Path("/{id}/remove/{userId}")
    @DELETE
    public void removeUserFromChannel(@PathParam("id") String id, @PathParam("userId") String userId) {
        Document channelDoc = this.mCol.find(new Document("_id", id)).first();

        if (channelDoc != null) {
            // Remove user from channel
            this.mCol.updateOne(new Document("_id", id), new Document("$pull", new Document("members", userId)));

            // Remove channel from user
            data.getUserCol().updateOne(new Document("_id", userId), new Document("$pull" , new Document("channelIds", id)));
        } else {
            throw new BadRequestException();
        }
    }

    @Path("/{id}/messages")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Object[] getChannelMessages(@PathParam("id") String id) {
        List<Document> messageDocs = this.data.getMessageCol().find(new Document("channel", id)).into(new ArrayList<>());

        if (messageDocs != null) {
            Object[] messages = messageDocs.stream().map(e -> Message.fromDocument(e)).toArray();
            return messages;
        }
        throw new NotFoundException();
    }

    @Path("/{id}/users")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String[] getChannelMembers(@PathParam("id") String id) {
        Document channelDoc = this.mCol.find(new Document("_id", id)).first();
        if (channelDoc != null) {
            return Channel.fromDocument(channelDoc).getMembers();
        }
        throw new NotFoundException();
    }

    private void insertChannel(Channel c) {
        this.mCol.insertOne(c.toDocument());
    }
}
