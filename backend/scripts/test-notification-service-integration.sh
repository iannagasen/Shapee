#!/bin/bash

# Script to run integration tests for the notification service
echo "===== Running Notification Service Integration Tests ====="

# Current directory
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd $DIR/../services/notification-service

# Run integration tests
mvn clean verify -DskipUnitTests

echo "===== Integration Tests Complete =====" 