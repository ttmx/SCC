package scc.srv;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import scc.Env;
import scc.srv.resources.ChannelResource;
import scc.srv.resources.MediaResource;
import scc.srv.resources.MessageResource;
import scc.srv.resources.UserResource;

import javax.inject.Singleton;

@Singleton
public class DataAbstractionLayer {

    public static final char CHANNEL = 'C';
    public static final char USER = 'U';
    public static final char MESSAGE = 'M';

    MongoClient mc = new MongoClient(new MongoClientURI(Env.DB_URI));
    MongoDatabase mdb = mc.getDatabase(Env.DB_NAME);
    MongoCollection<Document> userCol = mdb.getCollection(UserResource.DB_NAME);
    MongoCollection<Document> messageCol = mdb.getCollection(MessageResource.DB_NAME);
    MongoCollection<Document> channelCol = mdb.getCollection(ChannelResource.DB_NAME);
    MongoCollection<Document> mediaCol = mdb.getCollection(MediaResource.DB_NAME);

    BlobContainerClient containerClient = new BlobContainerClientBuilder()
            .connectionString(Env.BLOB_CONN_STRING)
            .containerName("images")
            .buildClient();

    public DataAbstractionLayer() {

    }

    private MongoCollection<Document> map(char type) {
        switch (type) {
            case MESSAGE: return messageCol;
            case USER: return userCol;
            case CHANNEL: return channelCol;
            default: return null;
        }
    }

    public MongoCollection<Document> getUserCol () {
        return userCol;
    }

    public MongoCollection<Document> getMessageCol() {
        return messageCol;
    }

    public MongoCollection<Document> getChannelCol() {
        return channelCol;
    }

    public MongoCollection<Document> getMediaCol() {
        return mediaCol;
    }

    public BlobContainerClient getBlobClient() {
        return containerClient;
    }

    public FindIterable<Document> getDocument (String id, Document filter, char collection) {
        return map(collection).find(filter);
    }

    public void updateOneDocument(String id, Document filter, Document update, char collection) {
        map(collection).updateOne(filter, update);
    }

    public void deleteOneDocument(String id, Document filter, char collection) {
        map(collection).deleteOne(filter);
    }

    public void insertOneDocument(String id, Document insert, char collection) {
        map(collection).insertOne(insert);
    }

}
