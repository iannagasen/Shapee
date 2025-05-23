#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Kafka Connection Test Script${NC}"
echo "=================================="

# Function to test TCP connection to a host:port
test_connection() {
  local host=$1
  local port=$2
  local description=$3
  
  echo -e "\n${YELLOW}Testing connection to ${host}:${port} (${description})...${NC}"
  
  # Check if nc (netcat) is available
  if ! command -v nc &> /dev/null; then
    echo -e "${RED}Error: netcat (nc) is not installed. Please install it first.${NC}"
    return 1
  fi
  
  # Try TCP connection
  if nc -zv ${host} ${port} -w 5 2>&1; then
    echo -e "${GREEN}✓ Successfully connected to ${host}:${port}${NC}"
    return 0
  else
    echo -e "${RED}✗ Failed to connect to ${host}:${port}${NC}"
    return 1
  fi
}

# Test Docker container existence
echo -e "\n${YELLOW}Checking for Kafka container...${NC}"
if docker ps | grep -q "kafka"; then
  echo -e "${GREEN}✓ Kafka container is running${NC}"
  
  # Get container IP if possible
  KAFKA_IP=$(docker inspect -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $(docker ps -q --filter "name=kafka"))
  if [ ! -z "$KAFKA_IP" ]; then
    echo -e "Kafka container IP: ${KAFKA_IP}"
  fi
else
  echo -e "${RED}✗ No Kafka container found running${NC}"
fi

# Test various Kafka connection possibilities
test_connection "localhost" "9092" "Public listener port"
test_connection "localhost" "29092" "Internal listener port"
test_connection "kafka" "9092" "Docker service on public port"
test_connection "kafka" "29092" "Docker service on internal port"

# Check if we can list Kafka topics (requires Kafka tools)
echo -e "\n${YELLOW}Attempting to list Kafka topics...${NC}"
if command -v kafka-topics.sh &> /dev/null || command -v kafka-topics &> /dev/null; then
  CMD="kafka-topics"
  if command -v kafka-topics.sh &> /dev/null; then
    CMD="kafka-topics.sh"
  fi
  
  echo "Using $CMD to connect to Kafka"
  echo -e "\nTrying localhost:9092..."
  $CMD --bootstrap-server localhost:9092 --list || echo -e "${RED}Failed to list topics on localhost:9092${NC}"
  
  echo -e "\nTrying kafka:9092..."
  $CMD --bootstrap-server kafka:9092 --list || echo -e "${RED}Failed to list topics on kafka:9092${NC}"
  
  echo -e "\nTrying kafka:29092..."
  $CMD --bootstrap-server kafka:29092 --list || echo -e "${RED}Failed to list topics on kafka:29092${NC}"
else
  echo -e "${RED}kafka-topics command not found. Cannot test topic listing.${NC}"
  echo "You can install Kafka tools or use the docker exec command instead:"
  echo "docker exec kafka kafka-topics --bootstrap-server kafka:29092 --list"
fi

echo -e "\n${YELLOW}Testing container-to-container networking...${NC}"
echo "Running a test with Docker exec to check connectivity from inside a container:"
docker exec -it $(docker ps -q --filter "name=kafka") bash -c "echo 'Testing connectivity to kafka:29092' && nc -zv kafka 29092 -w 5 || echo 'Connection failed'"

echo -e "\n${YELLOW}Checking application-docker.yml configuration...${NC}"
if [ -f "backend/services/order-service/src/main/resources/application-docker.yml" ]; then
  echo "Current Kafka bootstrap server configuration:"
  grep -A 2 "kafka:" backend/services/order-service/src/main/resources/application-docker.yml
else
  echo -e "${RED}Cannot find application-docker.yml file${NC}"
fi

echo -e "\n${YELLOW}Connection test complete.${NC}" 