package scc.serverless;// package scc.serverless;

import java.text.SimpleDateFormat;
import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;



import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * Azure Functions with Timer Trigger.
 */
public class TimerFunction {
    @FunctionName("periodic-compute")
    public void cosmosFunction( @TimerTrigger(name = "periodicSetTime", 
    								schedule = "30 * * * * *") 
    				String timerInfo,
    				ExecutionContext context) {

        String DB_URI = System.getenv("mongoConnectionString");
        String DB_NAME = System.getenv("DB_NAME");

        MongoClient mc = new MongoClient(new MongoClientURI(DB_URI));
        MongoDatabase mdb = mc.getDatabase(DB_NAME);


        MongoCollection<Document> messageCol = mdb.getCollection("messages");
        MongoCollection<Document> channelCol = mdb.getCollection("channels");
        MongoCollection<Document> usersCol = mdb.getCollection("users");


        FindIterable<Document> channels = channelCol.find(new Document("softDeleted", true));
        for (Document channel : channels) {

            messageCol.deleteMany(new Document("channel", channel.get("_id")));

            List<String> users = ((List<String>) channel.get("members"));

            usersCol.updateMany(new Document("_id",users),
                    new Document("$pull",channel.get("_id"))
            );

            channelCol.deleteOne(channel);
        }



    }
}
