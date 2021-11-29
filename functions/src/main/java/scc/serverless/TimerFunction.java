// package scc.serverless;

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


        MongoCollection<Document> messageCol = mdb.getCollection("channels");
        MongoCollection<Document> channelCol = mdb.getCollection("channels");



        FindIterable<Document> channels = channelCol.find(new Document("deleted", true));
        for (Document channel : channels) {
            messageCol.deleteMany(new Document("channel", channel.get("_id")));
            channelCol.deleteOne(channel);
        }
		// System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAA\n\n\n\n\nAAAAAAAAAAAAAAAAAa");
        // System.out.println(e.keySet().stream().reduce("",(a,b) ->b+"="+e.get(b)+"\n"+a));
    }
}
