# Digital Banking Platform - Helm Chart

This Helm chart deploys the complete Digital Banking Platform including PostgreSQL and Auth Service.

## Prerequisites

- Kubernetes 1.19+
- Helm 3.0+
- Docker for building images

## Quick Start

### 1. Build the Docker Image

```bash
cd backend/auth
docker build -t auth-service:latest .
```

### 2. Install the Chart

```bash
# From the project root
helm install digital-banking ./helm/digital-banking

# Or with custom values
helm install digital-banking ./helm/digital-banking -f ./helm/digital-banking/values.yaml
```

### 3. Check Deployment Status

```bash
# Check pods
kubectl get pods

# Check services
kubectl get svc

# Check logs
kubectl logs -f deployment/auth-service
```

## Configuration

The following table lists the configurable parameters of the Digital Banking chart and their default values.

### PostgreSQL Parameters

| Parameter | Description | Default |
|-----------|-------------|---------|
| `postgresql.enabled` | Enable PostgreSQL | `true` |
| `postgresql.image.repository` | PostgreSQL image repository | `postgres` |
| `postgresql.image.tag` | PostgreSQL image tag | `15` |
| `postgresql.auth.username` | PostgreSQL username | `username` |
| `postgresql.auth.password` | PostgreSQL password | `password` |
| `postgresql.auth.database` | PostgreSQL database name | `database` |
| `postgresql.persistence.enabled` | Enable persistence | `true` |
| `postgresql.persistence.size` | PVC size | `5Gi` |

### Auth Service Parameters

| Parameter | Description | Default |
|-----------|-------------|---------|
| `authService.enabled` | Enable Auth Service | `true` |
| `authService.replicaCount` | Number of replicas | `1` |
| `authService.image.repository` | Auth service image repository | `auth-service` |
| `authService.image.tag` | Auth service image tag | `latest` |
| `authService.service.port` | Service port | `8080` |
| `authService.resources.requests.memory` | Memory request | `512Mi` |
| `authService.resources.limits.memory` | Memory limit | `1Gi` |

## Customizing the Chart

You can customize the chart by creating a custom `values.yaml` file:

```bash
# Create custom values
cat > custom-values.yaml <<EOF
postgresql:
  auth:
    username: myuser
    password: mypassword
    database: bankdb

authService:
  replicaCount: 2
  resources:
    requests:
      memory: "1Gi"
      cpu: "500m"
EOF

# Install with custom values
helm install digital-banking ./helm/digital-banking -f custom-values.yaml
```

## Upgrading the Chart

```bash
# Upgrade with new values
helm upgrade digital-banking ./helm/digital-banking -f custom-values.yaml

# Upgrade with new image
helm upgrade digital-banking ./helm/digital-banking --set authService.image.tag=v1.0.1
```

## Uninstalling the Chart

```bash
helm uninstall digital-banking
```

## Testing the Deployment

```bash
# Port forward to access the service locally
kubectl port-forward svc/auth-service 8080:8080

# Test the health endpoint
curl http://localhost:8080/actuator/health
```

## Troubleshooting

### Check Pod Status
```bash
kubectl get pods
kubectl describe pod <pod-name>
```

### View Logs
```bash
kubectl logs -f deployment/auth-service
kubectl logs -f deployment/postgres
```

### Check Service
```bash
kubectl get svc
kubectl describe svc auth-service
```

### Check PVC
```bash
kubectl get pvc
kubectl describe pvc postgres-pvc
```

## Production Considerations

For production deployments, consider:

1. **Use Secrets for credentials**
   ```bash
   kubectl create secret generic db-credentials \
     --from-literal=username=myuser \
     --from-literal=password=securepassword
   ```

2. **Enable Ingress**
   ```yaml
   ingress:
     enabled: true
     className: nginx
     hosts:
       - host: banking.yourdomain.com
   ```

3. **Configure resource limits**
4. **Set up proper monitoring**
5. **Configure backups for PostgreSQL**
6. **Use a proper storage class**

## License

This chart is part of the Digital Banking Platform project.
