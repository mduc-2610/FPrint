# Function to start a service in a new terminal
start_service() {
    local service_name=$1
    local service_dir=$2
    
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        # Linux (assuming GNOME Terminal)
        gnome-terminal --tab --title="$service_name" -- bash -c "cd $service_dir && mvn spring-boot:run; exec bash"
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        osascript -e "tell application \"Terminal\" to do script \"cd $service_dir && mvn spring-boot:run\""
    elif [[ "$OSTYPE" == "msys"* || "$OSTYPE" == "win32" ]]; then
        # Windows with Git Bash
        # Using start command for Windows
        start "cmd" /k "cd $service_dir && mvn spring-boot:run"
    else
        echo "Unsupported operating system: $OSTYPE"
        echo "Manually run: cd $service_dir && mvn spring-boot:run"
    fi
    
    # Wait a moment between service starts
    sleep 2
}

# Get base directory (where this script is located)
BASE_DIR="$(pwd)"

# Start each service in a new terminal
echo "Starting API Gateway..."
start_service "API Gateway" "$BASE_DIR/api-gateway"

echo "Starting Access Control Service..."
start_service "Access Control Service" "$BASE_DIR/access-control-service"

echo "Starting User Management Service..."
start_service "User Management Service" "$BASE_DIR/user-management-service"

echo "Starting Biometrics Service..."
start_service "Biometrics Service" "$BASE_DIR/biometrics-service"

echo "Starting Model Management Service..."
start_service "Model Management Service" "$BASE_DIR/model-management-service"

echo "Starting Training Data Service..."
start_service "Training Data Service" "$BASE_DIR/training-data-service"

echo "All services have been started!"