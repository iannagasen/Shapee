#!/bin/bash

# Notification Service Test Script
echo "===== Notification Service Test ====="

# Current directory
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd $DIR/../../

# Step 1: Start the required services with Docker Compose
echo "Starting MongoDB, Kafka, and other required services..."
docker-compose -f docker/notification-test.yml down -v # Clean up previous runs
docker-compose -f docker/notification-test.yml up -d

# Wait for services to be ready
echo "Waiting for services to be ready..."
sleep 30

# Verify MongoDB is running
echo "Checking MongoDB status..."
docker exec notification-mongodb mongosh --eval "db.adminCommand('ping')"

# Verify Kafka is running
echo "Checking Kafka status..."
docker exec notification-kafka kafka-topics --list --bootstrap-server localhost:9092

# Step 2: Create the notification-events topic if it doesn't exist
echo "Creating Kafka topic 'notification-events'..."
docker exec notification-kafka kafka-topics --create --if-not-exists --topic notification-events --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1

# Step 3: Send a test notification to Kafka
echo "Sending test notification to Kafka topic..."
TEST_MESSAGE="{\"userId\":\"test-user-1\",\"type\":\"EMAIL\",\"subject\":\"Test Notification\",\"content\":\"This is a test notification from the test script.\",\"eventType\":\"TEST_EVENT\"}"
echo "Message being sent: $TEST_MESSAGE"
docker exec notification-kafka bash -c "echo '$TEST_MESSAGE' | kafka-console-producer.sh --broker-list localhost:9092 --topic notification-events"

# Step 4: Wait for the notification service to process the message
echo "Waiting for notification service to process the message..."
sleep 15  # Increased from 5 to 15 seconds

# Step 5: Check notification service logs
echo "Checking notification service logs..."
docker logs notification-service | tail -n 30

# Step 6: Check if notification was saved in MongoDB
echo "Checking MongoDB for the notification..."
docker exec notification-mongodb mongosh --quiet --eval "db = db.getSiblingDB('notification-service'); db.notifications.find({userId: 'test-user-1'}).toArray()"

# Step 7: Make a HTTP request to get user notifications
echo "Fetching notifications for user 'test-user-1'..."
curl -X GET http://localhost:8085/api/notifications/user/test-user-1

# Step 8: Clean up (uncomment to automatically stop services when done)
# echo "Cleaning up..."
# docker-compose -f docker/notification-test.yml down

echo "===== Test complete =====" 