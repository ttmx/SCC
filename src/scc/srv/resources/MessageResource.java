package scc.srv.resources;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import scc.entities.Message;
import scc.srv.DataAbstractionLayer;
import scc.utils.Log;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.UUID;


@Path("/messages")
public class MessageResource {
    public static final String DB_NAME = "messages";
    MongoCollection<Document> mCol;

    public MessageResource(DataAbstractionLayer data) {
        mCol = data.getMessageCol();
    }

    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Message getMessage(@PathParam("id") String id) {
        Document messageDoc = mCol.find(new Document("_id", id)).first();
        if (messageDoc != null) {
            return Message.fromDocument(messageDoc);
        }
        throw new NotFoundException();
    }

    @Path("/{id}")
    @DELETE
    public void deleteMessage(@PathParam("id") String id) {
        // TODO Authenticate, garbage collect

        DeleteResult result = this.mCol.deleteOne(new Document("_id", id));

        if (result.getDeletedCount() == 0) throw new NotFoundException();
    }

    @Path("/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createMessage(Message message) {
        Log.d("MessageResource", "Creating " + message.toString());

        // TODO Error handling, can channel not exist?

        // Generate an id for the message
        UUID uuid = UUID.randomUUID();
        message.setId(uuid.toString());

        this.insertMessage(message);

        return message.getId();
    }

    private void insertMessage(Message m) {
        mCol.insertOne(m.toDocument());
    }
}
