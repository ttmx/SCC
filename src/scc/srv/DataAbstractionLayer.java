package scc.srv;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import scc.Env;
import scc.srv.resources.ChannelResource;
import scc.srv.resources.MessageResource;
import scc.srv.resources.UserResource;

import javax.inject.Singleton;

@Singleton
public class DataAbstractionLayer {

    MongoClient mc = new MongoClient(new MongoClientURI(Env.DB_URI));
    MongoDatabase mdb = mc.getDatabase(Env.DB_NAME);
    MongoCollection<Document> userCol = mdb.getCollection(UserResource.DB_NAME);
    MongoCollection<Document> messageCol = mdb.getCollection(MessageResource.DB_NAME);
    MongoCollection<Document> channelCol = mdb.getCollection(ChannelResource.DB_NAME);

    BlobContainerClient containerClient = new BlobContainerClientBuilder()
                .connectionString(Env.BLOB_CONN_STRING)
                .containerName("images")
                .buildClient();

    public DataAbstractionLayer() {

    }
    public MongoCollection<Document> getUserCol () {
       return userCol;
    }

    public MongoCollection<Document> getMessageCol () {
        return messageCol;
    }

    public MongoCollection<Document> getChannelCol() {
        return channelCol;
    }

    public BlobContainerClient getBlobClient () {
        return containerClient;
    }


}
