package scc.srv.resources;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import scc.entities.Media;
import scc.srv.DataAbstractionLayer;
import scc.utils.Hash;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/media")
public class MediaResource {

    public static final String DB_NAME = "media";
    BlobContainerClient containerClient;
    MongoCollection<Document> mCol;

    public MediaResource(DataAbstractionLayer data) {
        containerClient = data.getBlobClient();
        mCol = data.getMediaCol();
    }

    @Path("/{id}")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] download(@PathParam("id") String id) {
        // TODO Error handling
        BlobClient blob = containerClient.getBlobClient(id);

        // Download contents to BinaryData
        BinaryData data = blob.downloadContent();

        return data.toBytes();
    }

    @Path("/")
    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public String upload(byte[] data) {

        String hash = Hash.of(data);
        UpdateOptions options = new UpdateOptions().upsert(true);
        UpdateResult ret = mCol.updateOne(
                new Document(Media.ID, hash),
                new Document()
                        .append("$set", new Document(Media.ID, hash))
                        .append("$inc", new Document(Media.REFERENCES, 1)),
                options
        );

        if (ret.getMatchedCount() != 0) {
            BlobClient blob = containerClient.getBlobClient(hash);
            blob.upload(BinaryData.fromBytes(data));
        }
        return hash;
    }

    public void delete(String hash) {
        //TODO Possible race condition if is updated between these two requests???
        DeleteResult a = mCol.deleteOne(
                new Document(Media.ID, hash)
                        .append(Media.REFERENCES, 1)
        );
        if (a.getDeletedCount()<=0){
            Document beforeUpdate = mCol.findOneAndUpdate(
                    new Document(Media.ID, hash),
                    new Document()
                            .append("$dec", new Document(Media.REFERENCES, 1))
            );

        }else {
            containerClient.getBlobClient(hash).delete();
        }

    }

}
