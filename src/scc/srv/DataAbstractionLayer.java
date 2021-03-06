package scc.srv;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import org.bson.Document;
import redis.clients.jedis.Jedis;
import scc.Env;
import scc.entities.Channel;
import scc.srv.resources.ChannelResource;
import scc.srv.resources.MediaResource;
import scc.srv.resources.MessageResource;
import scc.srv.resources.UserResource;
import scc.utils.Log;
import scc.utils.Redis;

import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
public class DataAbstractionLayer {

    public static final char CHANNEL = 'C';
    public static final char USER = 'U';
    public static final char MESSAGE = 'M';
    public boolean useCache = true;

    public ExecutorService threadPool = Executors.newFixedThreadPool(4);

    MongoClient mc = new MongoClient(new MongoClientURI(Env.DB_URI));
    MongoDatabase mdb = mc.getDatabase(Env.DB_NAME);
    MongoCollection<Document> userCol = mdb.getCollection(UserResource.DB_NAME);
    MongoCollection<Document> messageCol = mdb.getCollection(MessageResource.DB_NAME);
    MongoCollection<Document> channelCol = mdb.getCollection(ChannelResource.DB_NAME);
    MongoCollection<Document> mediaCol = mdb.getCollection(MediaResource.DB_NAME);
    ObjectMapper mapper = new ObjectMapper();

    public DataAbstractionLayer() {

    }

    public DataAbstractionLayer(boolean cache) {
        useCache = cache;
    }

    private MongoCollection<Document> map(char type) {
        switch (type) {
            case MESSAGE:
                return messageCol;
            case USER:
                return userCol;
            case CHANNEL:
                return channelCol;
            default:
                return null;
        }
    }

    public MongoCollection<Document> getUserCol() {
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

    private void writeToCache(Document doc, String key) {
        try (Jedis jedis = Redis.getCachePool().getResource()) {
            Log.d("Writing to cache", doc.toString());
            jedis.set(key, this.mapper.writeValueAsString(doc));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void removeFromCache(String key) {
        try (Jedis jedis = Redis.getCachePool().getResource()) {
            Log.d("Removing from cache", key);
            jedis.del(key);
        }
    }

    private Document readFromCache(String key) {
        try (Jedis jedis = Redis.getCachePool().getResource()) {
            String res = jedis.get(key);
            if (res != null && !res.equals("")) {
                Log.d("Found value in cache", res);
                return this.mapper.readValue(res, Document.class);
            } else {
                Log.d("No value found in cache", "");
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Document getChannel(String channelId) {
        Document doc = this.getDocument(channelId, new Document("_id", channelId).append(Channel.DELETED, false), CHANNEL);
        return doc == null || ((boolean) doc.get(Channel.DELETED)) ? null : doc;
    }

    public Document getDocument(String id, Document filter, char collection) {
        // Check in cache
        if (useCache) {
            String key = this.getKey(collection, id);

            Document doc = this.readFromCache(key);

            if (doc != null) {
                return doc;
            }

            assert map(collection) != null;

            doc = map(collection).find(filter).first();

            if (doc != null)
                this.writeToCache(doc, key);

            return doc;
        } else {
            return map(collection).find(filter).first();
        }
    }

    public void updateOneDocument(String id, Document filter, Document update, char collection) {
        assert map(collection) != null;
        if (useCache) {
            Document d = map(collection).findOneAndUpdate(filter, update, new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER));
            this.removeFromCache(this.getKey(collection, id));
            this.writeToCache(d, this.getKey(collection, id));
        } else {
            map(collection).updateOne(filter, update);
        }
    }

    public void deleteOneDocument(String id, Document filter, char collection) {
        map(collection).deleteOne(filter);
        if (useCache) {
            this.removeFromCache(this.getKey(collection, id));
        }
    }

    public void insertOneDocument(String id, Document insert, char collection) {
        map(collection).insertOne(insert);
        if (useCache) {
            this.writeToCache(insert, this.getKey(collection, id));
        }
    }

    private String getKey(char collection, String id) {
        return collection + id;
    }

}
