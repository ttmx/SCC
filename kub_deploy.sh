#!/bin/bash

NAME=scc55697

az group create -l westeurope -n $NAME
az aks create --resource-group $NAME --name $NAME --node-vm-size Standard_B2s --node-count 2
