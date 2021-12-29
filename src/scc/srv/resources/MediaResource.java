package scc.srv.resources;

import scc.srv.DataAbstractionLayer;
import scc.utils.Hash;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.nio.file.Files;

import static scc.Env.BLOB_PATH;

@Path("/media")
public class MediaResource {

    public static final String DB_NAME = "media";

    public MediaResource(DataAbstractionLayer data) {

    }

    @Path("/{id}")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] download(@PathParam("id") String id) {
        try {
            File file = new File(BLOB_PATH + id);
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Path("/")
    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public String upload(byte[] data) {
        String hash = Hash.of(data);

        try {
            FileOutputStream outputStream = new FileOutputStream(BLOB_PATH + hash);
            outputStream.write(data);
        } catch (IOException ignored) {
        }

        return hash;
    }

}
