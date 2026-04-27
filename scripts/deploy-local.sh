#!/bin/bash
# CloudOps Dashboard - Local Deployment Script
# This script ensures a reproducible local deployment using Docker Compose.

set -e

echo "🚀 Starting local deployment of CloudOps Dashboard..."

# Check if .env exists, if not copy from template
if [ ! -f .env ]; then
    echo "⚠️ .env file not found. Creating from .env.template..."
    cp .env.template .env
    echo "✅ .env file created. Please update it with your actual secrets before production deployment."
fi

# Build and start services
echo "🐳 Building Docker images and starting services..."
docker-compose --env-file .env up --build -d

echo "✅ Deployment successful! Services are starting up."
echo "🔗 Frontend: http://localhost:80"
echo "🔗 Backend API: http://localhost:8080/api"
echo "📊 Check container status with: docker-compose ps"
