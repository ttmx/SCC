package scc.serverless;

import com.microsoft.azure.functions.annotation.*;

import redis.clients.jedis.Jedis;
import scc.cache.RedisCache;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with Timer Trigger.
 */
public class CosmosDBFunction {
    @FunctionName("cosmosDBtest")
    public void updateMostRecentUsers(@CosmosDBTrigger(name = "cosmosTest",
    										databaseName = "scc2122dbnmp",
    										collectionName = "users",
    										createLeaseCollectionIfNotExists = true,
    										connectionStringSetting = "AzureCosmosDBConnection") 
        							String[] users,
        							final ExecutionContext context ) {
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			jedis.incr("cnt:cosmos");
			for( String u : users) {
				jedis.lpush("serverless::cosmos::users", u);
			}
			jedis.ltrim("serverless::cosmos::users", 0, 9);
		}
    }

}
