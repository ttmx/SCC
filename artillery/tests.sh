#!/bin/bash

# Create users
artillery run create-users.yml --output create-users.json;
artillery report create-users.json --output create-users.html;
rm create-users.json;

# Create channels
artillery run create-channels.yml --output create-channels.json;
artillery report create-channels.json --output create-channels.html;
rm create-channels.json;

# Create messages
artillery run create-messages.yml --output create-messages.json;
artillery report create-messages.json --output create-messages.html;
rm create-messages.json;

# Workload 1
artillery run workload1.yml --output workload1.json;
artillery report workload1.json --output workload1.html;
rm workload1.json;

# Workload 2
artillery run workload2.yml --output workload2.json;
artillery report workload2.json --output workload2.html;
rm workload2.json;

# Workload 4
artillery run workload4.yml --output workload4.json;
artillery report workload4.json --output workload4.html;
rm workload4.json;

# Workload 5
artillery run workload5.yml --output workload5.json;
artillery report workload5.json --output workload5.html;
rm workload5.json;

node sendResults.js
