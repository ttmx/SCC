#!/bin/bash
RESOURCE_GROUP_NAME="scc-backend-rg-bst"


echo "This will take some time..."
az resource delete --ids $(az group show --name "$RESOURCE_GROUP_NAME" | python3 -c "import sys, json; print(json.load(sys.stdin)['id'])")
clear
echo "Resources Deleted!"
