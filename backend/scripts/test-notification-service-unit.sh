#!/bin/bash

# Script to run unit tests for the notification service
echo "===== Running Notification Service Unit Tests ====="

# Current directory
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd $DIR/../services/notification-service

# Run unit tests
mvn clean test

echo "===== Unit Tests Complete =====" 