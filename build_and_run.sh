#!/usr/bin/env bash

# Exit immediately if a command exits with a non-zero status
set -e

# Terminal formatting colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}=====================================================${NC}"
echo -e "${BLUE}       Microservices Build & Launch Pipeline         ${NC}"
echo -e "${BLUE}=====================================================${NC}"

# Step 1: Clean and compile Maven artifacts locally (Optional speed-up check)
echo -e "\n${GREEN}[1/3] Pre-building Maven multi-module parent project...${NC}"
mvn clean package -DskipTests

# Step 2: Build Docker images defined in docker-compose.yml
echo -e "\n${GREEN}[2/3] Building multi-stage Docker images...${NC}"
docker compose build --parallel

# Step 3: Tear down old containers and launch the stack
echo -e "\n${GREEN}[3/3] Starting container orchestration...${NC}"
docker compose down --remove-orphans
docker compose up -d

echo -e "\n${BLUE}=====================================================${NC}"
echo -e "${GREEN} System successfully started! Monitoring status...   ${NC}"
echo -e "${BLUE}=====================================================${NC}"

# Display real-time container health status
docker compose ps