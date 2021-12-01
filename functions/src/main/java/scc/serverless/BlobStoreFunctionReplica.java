package scc.serverless;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.core.util.BinaryData;

/**
 * Azure Functions with Blob Trigger.
 */
public class BlobStoreFunctionReplica
{
	@FunctionName("blobReplicateReplica")
	public void setLastBlobInfo(@BlobTrigger(name = "blobReplicateReplica",
									dataType = "binary", 
									path = "images/{name}", 
									connection = "storageAccountReplicaConnectionString")
								byte[] content,
								@BindingName("name") String blobname, 
								final ExecutionContext context) {
		BlobContainerClient containerClient = new BlobContainerClientBuilder()
		    .connectionString(System.getenv("BlobStoreConnection"))
		    .containerName("images")
		    .buildClient();

		BlobClient blob = containerClient.getBlobClient(blobname);
            	blob.upload(BinaryData.fromBytes(content));
	}

}
