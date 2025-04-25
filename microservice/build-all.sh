#!/bin/bash
# save as build-all.sh and run chmod +x build-all.sh

services=(
  "discovery-server"
  "api-gateway"
  "access-control-service"
  "user-management-service"
  "model-management-service"
  "biometrics-service"
  "training-data-service"
)

for service in "${services[@]}"
do
  echo "Building $service..."
  cd $service
  mvn clean package -DskipTests
  cd ..
done

echo "All services built successfully!"