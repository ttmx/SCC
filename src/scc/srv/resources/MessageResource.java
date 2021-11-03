package scc.srv.resources;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import scc.Env;
import scc.entities.Message;
import scc.entities.User;
import scc.utils.Log;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.UUID;

@Path("/messages")
public class MessageResource {
    public static final String DB_NAME = "messages";
    MongoClient mc = new MongoClient(new MongoClientURI(Env.DB_URI));
    MongoDatabase mdb = mc.getDatabase(Env.DB_NAME);
    MongoCollection<Document> mCol = mdb.getCollection(DB_NAME);

    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Message getMessage(@PathParam("id") String id){
        Document messageDoc = mCol.find(new Document("_id", id) ).first();
        if (messageDoc != null) {
            return Message.fromDocument(messageDoc);
        }
        throw new NotFoundException();
    }

    @Path("/")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String createMessage(Message message) {
        Log.d("MessageResource","Creating "+ message.toString());

        // TODO Error handling, can channel not exist?

        // Generate an id for the message
        UUID uuid = UUID.randomUUID();
        message.setId(uuid.toString());

        this.insertMessage(message);

        return message.getId();
    }

    private void insertMessage(Message m){
        mCol.insertOne(m.toDocument());
    }
}
