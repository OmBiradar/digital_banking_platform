#!/bin/bash

# Digital Banking Platform Deployment Script
# This script builds the Docker image and deploys the Helm chart

set -e

echo "🚀 Digital Banking Platform Deployment"
echo "======================================"

# Step 1: Build Docker Image
echo ""
echo "📦 Step 1: Building Docker image for auth-service..."
cd backend/auth
docker build -t auth-service:latest .
echo "✅ Docker image built successfully!"

# Step 2: Deploy with Helm
echo ""
echo "☸️  Step 2: Deploying to Kubernetes with Helm..."
cd ../..
helm upgrade --install digital-banking ./helm/digital-banking
echo "✅ Helm chart deployed successfully!"

# Step 3: Wait for pods
echo ""
echo "⏳ Step 3: Waiting for pods to be ready..."
kubectl wait --for=condition=ready pod -l app=postgres --timeout=120s
kubectl wait --for=condition=ready pod -l app=auth-service --timeout=120s
echo "✅ All pods are ready!"

# Step 4: Show deployment status
echo ""
echo "📊 Deployment Status:"
echo "===================="
kubectl get pods
echo ""
kubectl get svc

echo ""
echo "🎉 Deployment completed successfully!"
echo ""
echo "To test the auth service:"
echo "  kubectl port-forward svc/auth-service 8080:8080"
echo "  curl http://localhost:8080/actuator/health"
echo ""
echo "To view logs:"
echo "  kubectl logs -f deployment/auth-service"
echo "  kubectl logs -f deployment/postgres"
