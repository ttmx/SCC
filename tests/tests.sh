#!/bin/bash

# Create users
docker run -v $(pwd):/config -t nunopreguica/scc2122-test artillery run create-users.yml --output create-users.json;
docker run -v $(pwd):/config -t nunopreguica/scc2122-test artillery report create-users.json --output create-users.html;
sudo rm create-users.json;

# Create channels
docker run -v $(pwd):/config -t nunopreguica/scc2122-test artillery run create-channels.yml --output create-channels.json;
docker run -v $(pwd):/config -t nunopreguica/scc2122-test artillery report create-channels.json --output create-channels.html;
sudo rm create-channels.json;

# Create messages
docker run -v $(pwd):/config -t nunopreguica/scc2122-test artillery run create-messages.yml --output create-messages.json;
docker run -v $(pwd):/config -t nunopreguica/scc2122-test artillery report create-messages.json --output create-messages.html;
sudo rm create-messages.json;

# Workload 1
docker run -v $(pwd):/config -t nunopreguica/scc2122-test artillery run workload1.yml --output workload1.json;
docker run -v $(pwd):/config -t nunopreguica/scc2122-test artillery report workload1.json --output workload1.html;
sudo rm workload1.json;

# Workload 2
docker run -v $(pwd):/config -t nunopreguica/scc2122-test artillery run workload2.yml --output workload2.json;
docker run -v $(pwd):/config -t nunopreguica/scc2122-test artillery report workload2.json --output workload2.html;
sudo rm workload2.json;

# Workload 3
docker run -v $(pwd):/config -t nunopreguica/scc2122-test artillery run workload3.yml --output workload3.json;
docker run -v $(pwd):/config -t nunopreguica/scc2122-test artillery report workload3.json --output workload3.html;
sudo rm workload3.json;

# Workload 4
docker run -v $(pwd):/config -t nunopreguica/scc2122-test artillery run workload4.yml --output workload4.json;
docker run -v $(pwd):/config -t nunopreguica/scc2122-test artillery report workload4.json --output workload4.html;
sudo rm workload4.json;

# Workload 5
docker run -v $(pwd):/config -t nunopreguica/scc2122-test artillery run workload5.yml --output workload5.json;
docker run -v $(pwd):/config -t nunopreguica/scc2122-test artillery report workload5.json --output workload5.html;
sudo rm workload5.json;