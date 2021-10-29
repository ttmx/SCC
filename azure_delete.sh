#!/bin/sh
. ./configs.azure

echo "This will take some time... You do not need to wait for the process to finish, you can close the shell in a few seconds!"
az resource delete --ids "$(az group show --name "$RESOURCE_GROUP"  | python3 -c "import sys, json; print(json.load(sys.stdin)['id'])")" & (sleep 15 && printf "It's probably safe to close the shell, feel free to wait though")
clear
echo "Resources Deleted!"
