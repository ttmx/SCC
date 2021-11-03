package scc.srv.resources;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import scc.Env;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.UUID;

@Path("/media")
public class MediaResource {

    BlobContainerClient containerClient;

    public MediaResource() {
        this.containerClient = new BlobContainerClientBuilder()
                .connectionString(Env.BLOB_CONN_STRING)
                .containerName("images")
                .buildClient();
    }

    @Path("/{id}")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] download(@PathParam("id") String id){
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

        UUID uuid = UUID.randomUUID();
        BlobClient blob = containerClient.getBlobClient(uuid.toString());

        blob.upload(BinaryData.fromBytes(data));

        return uuid.toString();
    }

}
