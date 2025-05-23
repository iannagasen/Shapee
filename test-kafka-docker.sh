#!/bin/bash

# Print header
echo "Kafka Docker Connection Test"
echo "==========================="

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "Error: Docker is not installed"
    exit 1
fi

# Check for running Kafka container
echo -e "\nChecking for Kafka container..."
KAFKA_CONTAINER=$(docker ps -q --filter "name=kafka")

if [ -z "$KAFKA_CONTAINER" ]; then
    echo "Error: No Kafka container found running"
    exit 1
else
    echo "Found Kafka container: $KAFKA_CONTAINER"
fi

# Test Kafka connectivity using a temporary container in the same network
echo -e "\nTesting Kafka connectivity on port 9092..."
docker run --rm --network order-network alpine sh -c "apk add --no-cache netcat-openbsd && nc -zv kafka 9092"

echo -e "\nTesting Kafka connectivity on port 29092..."
docker run --rm --network order-network alpine sh -c "apk add --no-cache netcat-openbsd && nc -zv kafka 29092"

# Check application-docker.yml bootstrap-servers setting
echo -e "\nChecking application-docker.yml bootstrap servers config:"
BOOTSTRAP_CONFIG=$(grep -A 2 "kafka:" backend/services/order-service/src/main/resources/application-docker.yml)
echo "$BOOTSTRAP_CONFIG"

# Check that the Kafka address in the application config
echo -e "\nTesting connectivity to the configured bootstrap server..."
BOOTSTRAP_SERVER=$(echo "$BOOTSTRAP_CONFIG" | grep "bootstrap-servers" | awk '{print $2}')

if [ -n "$BOOTSTRAP_SERVER" ]; then
    echo "Found bootstrap-servers configuration: $BOOTSTRAP_SERVER"
    
    # Split host and port
    HOST=$(echo $BOOTSTRAP_SERVER | cut -d: -f1)
    PORT=$(echo $BOOTSTRAP_SERVER | cut -d: -f2)
    
    echo "Testing connection to $HOST:$PORT from within Docker network..."
    docker run --rm --network order-network alpine sh -c "apk add --no-cache netcat-openbsd && nc -zv $HOST $PORT"
else
    echo "Could not determine bootstrap-servers configuration"
fi

# Execute kafka-topics command inside the Kafka container
echo -e "\nListing Kafka topics:"
docker exec $KAFKA_CONTAINER sh -c "kafka-topics --bootstrap-server kafka:29092 --list"

echo -e "\nTest complete." 