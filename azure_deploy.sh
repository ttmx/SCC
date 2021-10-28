#!/bin/bash

RESOURCE_GROUP="scc-backend-rg-56187"

#BLOB STORAGE
STORAGE_ACCOUNT_NAME="sccstorage56187"
CONTAINER_NAME="images"

#COSMOS DB
COSMOS_DB_ACCOUNT_NAME="sccdb56187"
DATABASE_TYPE="SQL" #SQL or MONGODB
DATABASE_NAME="scc56187db"
CO_NAME="users" #Container name for "SQL" or collection name for Mongo


generate_sql_cosmos(){
printf "{
    \"\$schema\": \"https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#\",
    \"contentVersion\": \"1.0.0.0\",
    \"variables\": {},
    \"resources\": [
        {
            \"type\": \"Microsoft.DocumentDB/databaseAccounts\",
            \"apiVersion\": \"2021-07-01-preview\",
            \"name\": \"$COSMOS_DB_ACCOUNT_NAME\",
            \"location\": \"West Europe\",
            \"tags\": {
                \"defaultExperience\": \"Core (SQL)\",
                \"hidden-cosmos-mmspecial\": \"\"
            },
            \"kind\": \"GlobalDocumentDB\",
            \"identity\": {
                \"type\": \"None\"
            },
            \"properties\": {
                \"publicNetworkAccess\": \"Enabled\",
                \"enableAutomaticFailover\": false,
                \"enableMultipleWriteLocations\": false,
                \"isVirtualNetworkFilterEnabled\": false,
                \"virtualNetworkRules\": [],
                \"disableKeyBasedMetadataWriteAccess\": false,
                \"enableFreeTier\": true,
                \"enableAnalyticalStorage\": false,
                \"analyticalStorageConfiguration\": {
                    \"schemaType\": \"WellDefined\"
                },
                \"databaseAccountOfferType\": \"Standard\",
                \"networkAclBypass\": \"None\",
                \"disableLocalAuth\": false,
                \"consistencyPolicy\": {
                    \"defaultConsistencyLevel\": \"Session\",
                    \"maxIntervalInSeconds\": 5,
                    \"maxStalenessPrefix\": 100
                },
                \"locations\": [
                    {
                        \"locationName\": \"West Europe\",
                        \"failoverPriority\": 0,
                        \"isZoneRedundant\": false
                    }
                ],
                \"cors\": [],
                \"capabilities\": [],
                \"ipRules\": [],
                \"backupPolicy\": {
                    \"type\": \"Periodic\",
                    \"periodicModeProperties\": {
                        \"backupIntervalInMinutes\": 240,
                        \"backupRetentionIntervalInHours\": 8,
                        \"backupStorageRedundancy\": \"Local\"
                    }
                },
                \"networkAclBypassResourceIds\": [],
                \"diagnosticLogSettings\": {
                    \"enableFullTextQuery\": \"None\"
                }
            }
        }
    ]
}" > depl.json
}

generate_mongo_cosmos(){
printf "{
    \"\$schema\": \"https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#\",
    \"contentVersion\": \"1.0.0.0\",
    \"variables\": {},
    \"resources\": [
        {
            \"type\": \"Microsoft.DocumentDB/databaseAccounts\",
            \"apiVersion\": \"2021-07-01-preview\",
            \"name\": \"$COSMOS_DB_ACCOUNT_NAME\",
            \"location\": \"West Europe\",
            \"kind\": \"MongoDB\",
            \"identity\": {
                \"type\": \"None\"
            },
            \"properties\": {
                \"publicNetworkAccess\": \"Enabled\",
                \"enableAutomaticFailover\": false,
                \"enableMultipleWriteLocations\": false,
                \"isVirtualNetworkFilterEnabled\": false,
                \"virtualNetworkRules\": [],
                \"disableKeyBasedMetadataWriteAccess\": false,
                \"enableFreeTier\": true,
                \"enableAnalyticalStorage\": false,
                \"analyticalStorageConfiguration\": {
                    \"schemaType\": \"FullFidelity\"
                },
                \"databaseAccountOfferType\": \"Standard\",
                \"networkAclBypass\": \"None\",
                \"disableLocalAuth\": false,
                \"consistencyPolicy\": {
                    \"defaultConsistencyLevel\": \"Session\",
                    \"maxIntervalInSeconds\": 5,
                    \"maxStalenessPrefix\": 100
                },
                \"apiProperties\": {
                    \"serverVersion\": \"4.0\"
                },
                \"locations\": [
                    {
                        \"locationName\": \"West Europe\",
                        \"failoverPriority\": 0,
                        \"isZoneRedundant\": false
                    }
                ],
                \"cors\": [],
                \"capabilities\": [
                    {
                        \"name\": \"EnableMongo\"
                    }
                ],
                \"ipRules\": [],
                \"backupPolicy\": {
                    \"type\": \"Periodic\",
                    \"periodicModeProperties\": {
                        \"backupIntervalInMinutes\": 240,
                        \"backupRetentionIntervalInHours\": 8,
                        \"backupStorageRedundancy\": \"Local\"
                    }
                },
                \"networkAclBypassResourceIds\": [],
                \"diagnosticLogSettings\": {
                    \"enableFullTextQuery\": \"None\"
                }
            }
        }
    ]
}" > depl.json
}

sql_cosmos_depl(){
    generate_sql_cosmos
	az group deployment create -g $RESOURCE_GROUP --template-file depl.json
	az cosmosdb sql database create --account-name $COSMOS_DB_ACCOUNT_NAME --name $DATABASE_NAME --resource-group $RESOURCE_GROUP --throughput 400
    az cosmosdb sql container create --account-name $COSMOS_DB_ACCOUNT_NAME --database-name $DATABASE_NAME --name $CO_NAME --partition-key-path "/id" --resource-group $RESOURCE_GROUP
}

mongo_cosmos_depl(){
    generate_mongo_cosmos
	az group deployment create -g $RESOURCE_GROUP --template-file depl.json
   	az cosmosdb mongodb database create --account-name $COSMOS_DB_ACCOUNT_NAME --name $DATABASE_NAME --resource-group $RESOURCE_GROUP --throughput 400
    az cosmosdb mongodb collection create --account-name $COSMOS_DB_ACCOUNT_NAME --database-name $DATABASE_NAME --name $CO_NAME --resource-group $RESOURCE_GROUP
}

az group create -l westeurope -n $RESOURCE_GROUP

az storage account create -n $STORAGE_ACCOUNT_NAME -g $RESOURCE_GROUP -l westeurope --sku Standard_LRS

#Set container soft-delete to false
az storage account blob-service-properties update --enable-container-delete-retention false --account-name $STORAGE_ACCOUNT_NAME --resource-group $RESOURCE_GROUP

#Set blob soft-delete to false
az storage account blob-service-properties update --account-name $STORAGE_ACCOUNT_NAME --resource-group $RESOURCE_GROUP --enable-delete-retention false


#Set shared file retention to disabled
az storage account file-service-properties update --account-name $STORAGE_ACCOUNT_NAME --resource-group $RESOURCE_GROUP --enable-delete-retention false

#Create "images" container
az storage container create -n $CONTAINER_NAME --account-name $STORAGE_ACCOUNT_NAME --public-access blob

if [ $DATABASE_TYPE = "SQL" ]; then
	sql_cosmos_depl
elif [ $DATABASE_TYPE = "MONGODB" ]; then
	mongo_cosmos_depl
fi

clear
#Get storage account connection string
echo "Here is your connection string for the blob storage: $(az storage account show-connection-string -g $RESOURCE_GROUP -n $STORAGE_ACCOUNT_NAME | python3 -c "import sys, json; print(json.load(sys.stdin)['connectionString'])")"
echo ""

if [ $DATABASE_TYPE = "SQL" ]; then
    echo "Here is your DB_KEY: $(az cosmosdb keys list --name $COSMOS_DB_ACCOUNT_NAME --resource-group $RESOURCE_GROUP | python3 -c "import sys, json; print(json.load(sys.stdin)['primaryMasterKey'])")"

elif [ $DATABASE_TYPE = "MONGODB" ]; then
    echo "Here is your connection string for MongoDB: $(az cosmosdb keys list --name $COSMOS_DB_ACCOUNT_NAME --resource-group $RESOURCE_GROUP --type connection-strings | python3 -c "import sys, json; print(json.load(sys.stdin)['connectionStrings'][0]['connectionString'])")"
fi
echo ""
echo "Press enter to deploy webapp after replacing connection string(s) and/or key in code..."

read

rm depl.json

#Deploy Web-app to Azure
mvn compile package azure-webapp:deploy

clear

echo "Deployment Complete!"
