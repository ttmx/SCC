package scc.srv.resources;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobStorageException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
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

        try {
            BlobClient blob = containerClient.getBlobClient(hash);
            blob.upload(BinaryData.fromBytes(data));
        } catch (BlobStorageException ignored) {
        }

        return hash;
    }

    public void delete(String hash) {
        DeleteResult a = mCol.deleteOne(
                new Document(Media.ID, hash)
                        .append(Media.REFERENCES, 1)
        );
        if (a.getDeletedCount() <= 0) {
            Document beforeUpdate = mCol.findOneAndUpdate(
                    new Document(Media.ID, hash),
                    new Document()
                            .append("$dec", new Document(Media.REFERENCES, 1))
            );

        } else {
            containerClient.getBlobClient(hash).delete();
        }

    }

}
