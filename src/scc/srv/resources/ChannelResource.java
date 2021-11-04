package scc.srv.resources;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import scc.entities.Channel;
import scc.srv.DataAbstractionLayer;
import scc.utils.Log;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.UUID;

@Path("/channel")
public class ChannelResource {
    public static final String DB_NAME = "channels";
    MongoCollection<Document> mCol;
    DataAbstractionLayer data;

    public ChannelResource(DataAbstractionLayer data) {
        mCol = data.getChannelCol();
        //TODO MCOL? UNNECESSARY?
        this.data = data;
    }

    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Channel getChannel(@PathParam("id") String id) {
        Document channelDoc = mCol.find(new Document("_id", id)).first();
        if (channelDoc != null) {
            return Channel.fromDocument(channelDoc);
        }
        throw new NotFoundException();
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
    @Produces(MediaType.APPLICATION_JSON)
    public void addUserToChannel(@PathParam("id") String id, @PathParam("userId") String userId) {
        Document channelDoc = mCol.find(new Document("_id", id)).first();

        // TODO make update better with array operations
        // TODO make sure user is in the channel to add?

        if (channelDoc != null) {
            mCol.updateOne(new Document("_id", id), new Document("$addToSet" , new Document("members", id)));

            // TODO add channel to user's list
            data.getUserCol().updateOne(new Document("_id", userId), new Document("$addToSet" , new Document("channelIds", id)));
        }
        throw new BadRequestException();
    }

    private void insertChannel(Channel c) {
        mCol.insertOne(c.toDocument());
    }
}
