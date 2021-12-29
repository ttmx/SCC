#!/bin/bash

NAME=scc55697
USERNAME=jbordalo

az group create -l westeurope -n $NAME
az aks create --resource-group $NAME --name $NAME --node-vm-size Standard_B2s --node-count 2
az aks get-credentials --resource-group $NAME --name $NAME

kubectl create secret docker-registry regcred --docker-server=https://index.docker.io/v1/ --docker-username=$USERNAME --docker-password=$1

kubectl apply -f kubernetes/
