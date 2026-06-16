#!/bin/bash
set -e

echo "=================================================="
echo "[1/2] Building Backend Spring Boot JAR..."
echo "=================================================="

cd /mnt/c/Users/82107/dev/project/nemologic/backend

echo "Calling Windows gradle (with Java 17)..."
cmd.exe /c "gradle bootJar"

echo ""
echo "=================================================="
echo "[2/2] Running Ansible Playbook..."
echo "=================================================="

cd /mnt/c/Users/82107/dev/project/nemologic/infra/ansible
ANSIBLE_TIMEOUT=60 ANSIBLE_BECOME_TIMEOUT=60 ansible-playbook -i hosts.ini playbook.yml

echo ""
echo "=================================================="
echo "Deployment Completed Successfully!"
echo "=================================================="
