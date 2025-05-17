#!/bin/bash

# Test script for notification service
echo "Testing Notification Service via Kafka"

# Send a test notification message to Kafka
echo "Sending test notification to Kafka topic 'notification-events'"

# Using Kafka's command-line tools to produce a message
kafka-console-producer.sh --bootstrap-server localhost:9092 --topic notification-events << EOF
{
  "userId": "test-user-1",
  "type": "EMAIL",
  "subject": "Test Notification",
  "content": "This is a test notification from the Kafka producer.",
  "eventType": "TEST_EVENT"
}
EOF

echo "Test message sent to Kafka. Check notification service logs to verify reception." 