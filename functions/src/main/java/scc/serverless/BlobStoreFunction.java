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
public class BlobStoreFunction
{
	@FunctionName("blobReplicate")
	public void setLastBlobInfo(@BlobTrigger(name = "blobReplicate", 
									dataType = "binary", 
									path = "images/{name}", 
									connection = "BlobStoreConnection")
								byte[] content,
								@BindingName("name") String blobname, 
								final ExecutionContext context) {
		BlobContainerClient containerClient = new BlobContainerClientBuilder()
		    .connectionString(System.getenv("storageAccountReplicaConnectionString"))
		    .containerName("images")
		    .buildClient();

		BlobClient blob = containerClient.getBlobClient(blobname);
            	blob.upload(BinaryData.fromBytes(content));
	}

}
