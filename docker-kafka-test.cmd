@echo off
echo Kafka Connection Test with Docker
echo ===============================

echo.
echo Checking for running Kafka container...
docker ps -q --filter "name=kafka" > temp.txt
set /p KAFKA_CONTAINER=<temp.txt
del temp.txt

if "%KAFKA_CONTAINER%"=="" (
  echo No Kafka container found running!
  exit /b 1
) else (
  echo Found Kafka container: %KAFKA_CONTAINER%
)

echo.
echo Testing Kafka port 9092 from a test container...
docker run --rm --network order-network busybox sh -c "echo 'Testing 9092' && (nc -z -v -w 5 kafka 9092 2>&1 || echo 'Connection failed')"

echo.
echo Testing Kafka port 29092 from a test container...
docker run --rm --network order-network busybox sh -c "echo 'Testing 29092' && (nc -z -v -w 5 kafka 29092 2>&1 || echo 'Connection failed')"

echo.
echo Testing connectivity inside Kafka container itself...
docker exec %KAFKA_CONTAINER% sh -c "echo 'Testing Kafka connectivity from inside container:' && nc -z -v 127.0.0.1 9092 -w 2"

echo.
echo Checking for 'notification-events' topic...
docker exec %KAFKA_CONTAINER% sh -c "kafka-topics --bootstrap-server kafka:29092 --list"

echo.
echo Creating the notification-events topic if it doesn't exist...
docker exec %KAFKA_CONTAINER% sh -c "kafka-topics --bootstrap-server kafka:29092 --create --if-not-exists --topic notification-events --partitions 1 --replication-factor 1"

echo.
echo Test complete. 