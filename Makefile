.PHONY: help build load deploy upgrade test clean status logs shell restart port-forward

# Variables
IMAGE_NAME := auth-service
IMAGE_TAG := v2
HELM_RELEASE := digital-banking
NAMESPACE := default
CHART_PATH := ./helm/digital-banking

help: ## Show this help message
	@echo 'Usage: make [target]'
	@echo ''
	@echo 'Available targets:'
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'

build: ## Build Docker image for auth service
	@echo "🔨 Building Docker image..."
	cd backend/auth && docker build -t $(IMAGE_NAME):$(IMAGE_TAG) .
	@echo "✅ Build complete!"

load: ## Load Docker image into Minikube
	@echo "📦 Loading image into Minikube..."
	minikube image load $(IMAGE_NAME):$(IMAGE_TAG)
	@echo "✅ Image loaded!"

deploy: build load ## Build, load, and deploy everything with Helm
	@echo "🚀 Deploying Digital Banking Platform..."
	helm upgrade --install $(HELM_RELEASE) $(CHART_PATH) --namespace $(NAMESPACE)
	@echo "✅ Deployment complete!"
	@echo ""
	@echo "Waiting for pods to be ready..."
	@kubectl wait --for=condition=ready pod -l app=postgres --timeout=120s --namespace $(NAMESPACE) || true
	@kubectl wait --for=condition=ready pod -l app=auth-service --timeout=180s --namespace $(NAMESPACE) || true
	@make status

upgrade: build load ## Rebuild image and upgrade Helm release
	@echo "🔄 Upgrading deployment..."
	helm upgrade $(HELM_RELEASE) $(CHART_PATH) --namespace $(NAMESPACE)
	@kubectl rollout restart deployment auth-service --namespace $(NAMESPACE)
	@echo "✅ Upgrade complete!"

install: deploy ## Alias for deploy

test: ## Test the deployment health
	@echo "🧪 Testing deployment..."
	@echo ""
	@echo "📊 Pod Status:"
	@kubectl get pods --namespace $(NAMESPACE) | grep -E "NAME|auth-service|postgres"
	@echo ""
	@echo "🔍 Service Status:"
	@kubectl get svc --namespace $(NAMESPACE) | grep -E "NAME|auth-service|postgres"
	@echo ""
	@echo "💚 Health Check:"
	@kubectl exec -n $(NAMESPACE) deployment/auth-service -- wget -qO- http://localhost:8080/actuator/health || echo "❌ Health check failed"
	@echo ""
	@echo "🗄️  Database Tables:"
	@kubectl exec -n $(NAMESPACE) deployment/postgres -- psql -U username -d database -c "\\dt" || echo "❌ Database check failed"

status: ## Show deployment status
	@echo "📊 Deployment Status"
	@echo "===================="
	@echo ""
	@echo "Helm Release:"
	@helm list --namespace $(NAMESPACE) | grep $(HELM_RELEASE) || echo "No release found"
	@echo ""
	@echo "Pods:"
	@kubectl get pods --namespace $(NAMESPACE)
	@echo ""
	@echo "Services:"
	@kubectl get svc --namespace $(NAMESPACE)
	@echo ""
	@echo "PVCs:"
	@kubectl get pvc --namespace $(NAMESPACE)

logs: ## Show auth-service logs
	@echo "📋 Auth Service Logs:"
	@kubectl logs -f deployment/auth-service --namespace $(NAMESPACE)

logs-postgres: ## Show PostgreSQL logs
	@echo "📋 PostgreSQL Logs:"
	@kubectl logs -f deployment/postgres --namespace $(NAMESPACE)

shell: ## Open shell in auth-service pod
	@kubectl exec -it deployment/auth-service --namespace $(NAMESPACE) -- sh

shell-postgres: ## Open PostgreSQL shell
	@kubectl exec -it deployment/postgres --namespace $(NAMESPACE) -- psql -U username -d database

restart: ## Restart auth-service deployment
	@echo "🔄 Restarting auth-service..."
	@kubectl rollout restart deployment auth-service --namespace $(NAMESPACE)
	@echo "✅ Restart initiated!"

restart-postgres: ## Restart PostgreSQL deployment
	@echo "🔄 Restarting PostgreSQL..."
	@kubectl rollout restart deployment postgres --namespace $(NAMESPACE)
	@echo "✅ Restart initiated!"

port-forward: ## Port forward auth-service to localhost:8080
	@echo "🔌 Port forwarding auth-service to localhost:8080..."
	@echo "Press Ctrl+C to stop"
	@kubectl port-forward svc/auth-service 8080:8080 --namespace $(NAMESPACE)

port-forward-db: ## Port forward PostgreSQL to localhost:5432
	@echo "🔌 Port forwarding PostgreSQL to localhost:5432..."
	@echo "Press Ctrl+C to stop"
	@kubectl port-forward svc/postgres 5432:5432 --namespace $(NAMESPACE)

health: ## Check health endpoint
	@echo "💚 Checking health endpoint..."
	@curl -s http://localhost:8080/actuator/health | python3 -m json.tool || echo "❌ Make sure to run 'make port-forward' first"

clean: ## Uninstall Helm release and clean up resources
	@echo "🧹 Cleaning up..."
	@helm uninstall $(HELM_RELEASE) --namespace $(NAMESPACE) || echo "Release not found"
	@kubectl delete pvc postgres-pvc --namespace $(NAMESPACE) || echo "PVC not found"
	@echo "✅ Cleanup complete!"

clean-all: clean ## Clean everything including Docker images
	@echo "🗑️  Removing Docker images..."
	@docker rmi $(IMAGE_NAME):$(IMAGE_TAG) || echo "Image not found"
	@docker rmi $(IMAGE_NAME):latest || echo "Image not found"
	@echo "✅ All cleaned up!"

describe-auth: ## Describe auth-service deployment
	@kubectl describe deployment auth-service --namespace $(NAMESPACE)

describe-postgres: ## Describe PostgreSQL deployment
	@kubectl describe deployment postgres --namespace $(NAMESPACE)

get-pods: ## Get all pods
	@kubectl get pods --namespace $(NAMESPACE) -o wide

get-all: ## Get all resources
	@kubectl get all --namespace $(NAMESPACE)

watch: ## Watch pod status
	@watch kubectl get pods --namespace $(NAMESPACE)

# Frontend targets
frontend-install: ## Install frontend dependencies
	@echo "📦 Installing frontend dependencies..."
	@cd frontend && npm install
	@echo "✅ Frontend dependencies installed!"

frontend-dev: ## Run frontend in development mode
	@echo "🚀 Starting frontend development server..."
	@cd frontend && npm start

frontend-build: ## Build frontend for production
	@echo "🔨 Building frontend..."
	@cd frontend && npm run build
	@echo "✅ Frontend build complete!"

# Full stack targets
dev: ## Start full development environment
	@echo "🚀 Starting development environment..."
	@make -j2 deploy frontend-dev

all: deploy frontend-build ## Deploy backend and build frontend

# Utility targets
minikube-start: ## Start Minikube
	@echo "🎮 Starting Minikube..."
	@minikube start
	@echo "✅ Minikube started!"

minikube-stop: ## Stop Minikube
	@echo "🛑 Stopping Minikube..."
	@minikube stop
	@echo "✅ Minikube stopped!"

minikube-status: ## Check Minikube status
	@minikube status

dashboard: ## Open Kubernetes dashboard
	@minikube dashboard

# Database operations
db-create-user: ## Create a test user in database
	@echo "👤 Creating test user..."
	@kubectl exec -it deployment/postgres --namespace $(NAMESPACE) -- psql -U username -d database -c "INSERT INTO users (username, password, email) VALUES ('testuser', 'password123', 'test@example.com') ON CONFLICT DO NOTHING;"
	@echo "✅ User created!"

db-list-users: ## List all users in database
	@echo "👥 Users in database:"
	@kubectl exec -it deployment/postgres --namespace $(NAMESPACE) -- psql -U username -d database -c "SELECT * FROM users;"

db-reset: ## Drop and recreate database (WARNING: destroys data)
	@echo "⚠️  This will destroy all data. Press Ctrl+C to cancel..."
	@sleep 5
	@kubectl exec -it deployment/postgres --namespace $(NAMESPACE) -- psql -U username -d database -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
	@make restart
	@echo "✅ Database reset!"

# Version management
version: ## Show current version
	@echo "Current version: $(IMAGE_TAG)"

set-version: ## Set new version (usage: make set-version VERSION=v3)
	@echo "Setting version to $(VERSION)..."
	@sed -i 's/IMAGE_TAG := .*/IMAGE_TAG := $(VERSION)/' Makefile
	@sed -i 's/tag: .*/tag: $(VERSION)/' $(CHART_PATH)/values.yaml
	@echo "✅ Version updated to $(VERSION)"
