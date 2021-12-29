az group create --name scc55697-art --location westeurope

az container create --resource-group scc55697-art --name artillery-test --image atnica/artillery-tests --registry-login-server index.docker.io --registry-username atnica --registry-password $1 --restart-policy Never --cpu 4
