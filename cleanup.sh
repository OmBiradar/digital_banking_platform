#!/bin/bash

# Digital Banking Platform Cleanup Script
# This script removes all deployed resources

set -e

echo "🧹 Digital Banking Platform Cleanup"
echo "===================================="

echo ""
echo "⚠️  This will delete all deployed resources!"
read -p "Are you sure? (y/N) " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Yy]$ ]]
then
    echo "Cleanup cancelled."
    exit 1
fi

echo ""
echo "🗑️  Uninstalling Helm release..."
helm uninstall digital-banking || echo "Helm release not found"

echo ""
echo "🗑️  Cleaning up PVCs..."
kubectl delete pvc postgres-pvc || echo "PVC not found"

echo ""
echo "✅ Cleanup completed!"
echo ""
echo "To verify:"
echo "  kubectl get pods"
echo "  kubectl get svc"
echo "  kubectl get pvc"
