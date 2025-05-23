# PowerShell script to test Kafka connection on Windows
Write-Host "Kafka Connection Test Script (Windows)" -ForegroundColor Yellow
Write-Host "======================================"

# Function to test TCP connection
function Test-TcpConnection {
    param (
        [string]$Host,
        [int]$Port,
        [string]$Description
    )

    Write-Host "`nTesting connection to $Host`:$Port ($Description)..." -ForegroundColor Yellow
    
    $timeout = 5000
    $tcpClient = New-Object System.Net.Sockets.TcpClient
    
    try {
        # Attempt connection with timeout
        $connection = $tcpClient.BeginConnect($Host, $Port, $null, $null)
        $success = $connection.AsyncWaitHandle.WaitOne($timeout, $false)
        
        if ($success -and $tcpClient.Connected) {
            Write-Host "✓ Successfully connected to $Host`:$Port" -ForegroundColor Green
            $tcpClient.EndConnect($connection)
            return $true
        } else {
            Write-Host "✗ Failed to connect to $Host`:$Port" -ForegroundColor Red
            return $false
        }
    } catch {
        Write-Host "✗ Error connecting to $Host`:$Port`: $_" -ForegroundColor Red
        return $false
    } finally {
        $tcpClient.Close()
    }
}

# Check for Docker
Write-Host "`nChecking for Docker..." -ForegroundColor Yellow
try {
    $dockerPs = docker ps | Out-String
    
    if ($dockerPs -match "kafka") {
        Write-Host "✓ Kafka container is running" -ForegroundColor Green
        
        # Get container IP
        $kafkaIp = docker inspect -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' (docker ps -q --filter "name=kafka")
        if ($kafkaIp) {
            Write-Host "Kafka container IP: $kafkaIp"
        }
    } else {
        Write-Host "✗ No Kafka container found running" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ Error running Docker command: $_" -ForegroundColor Red
}

# Test various Kafka connection possibilities
Test-TcpConnection -Host "localhost" -Port 9092 -Description "Public listener port"
Test-TcpConnection -Host "localhost" -Port 29092 -Description "Internal listener port"
Test-TcpConnection -Host "kafka" -Port 9092 -Description "Docker service on public port"
Test-TcpConnection -Host "kafka" -Port 29092 -Description "Docker service on internal port"

# Check application-docker.yml configuration
Write-Host "`nChecking application-docker.yml configuration..." -ForegroundColor Yellow
$configFile = "backend/services/order-service/src/main/resources/application-docker.yml"

if (Test-Path $configFile) {
    Write-Host "Current Kafka bootstrap server configuration:"
    $content = Get-Content $configFile -Raw
    if ($content -match "kafka:[\r\n\s]+bootstrap-servers:\s+([^\r\n]+)") {
        $bootstrapServer = $matches[1]
        Write-Host "  bootstrap-servers: $bootstrapServer"
        
        # Split host and port
        $hostPort = $bootstrapServer -split ':'
        if ($hostPort.Length -eq 2) {
            $host = $hostPort[0]
            $port = $hostPort[1]
            
            Write-Host "`nValidating configured bootstrap server..."
            Test-TcpConnection -Host $host -Port $port -Description "Configured bootstrap server"
        }
    } else {
        Write-Host "Could not find bootstrap-servers in the config file" -ForegroundColor Red
    }
} else {
    Write-Host "Cannot find application-docker.yml file" -ForegroundColor Red
}

# List Kafka topics using Docker command
Write-Host "`nAttempting to list Kafka topics via Docker exec..." -ForegroundColor Yellow
try {
    $kafkaContainer = docker ps -q --filter "name=kafka"
    if ($kafkaContainer) {
        Write-Host "Topics on the Kafka server:"
        docker exec $kafkaContainer kafka-topics --bootstrap-server kafka:29092 --list
    } else {
        Write-Host "No Kafka container found to list topics" -ForegroundColor Red
    }
} catch {
    Write-Host "Error listing Kafka topics: $_" -ForegroundColor Red
}

Write-Host "`nConnection test complete." -ForegroundColor Yellow 