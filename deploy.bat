@echo off
setlocal enabledelayedexpansion

echo ==================================================
echo [1/2] Building Backend Spring Boot JAR locally...
echo ==================================================

cd backend
call gradle bootJar
if !ERRORLEVEL! neq 0 (
    echo [ERROR] Gradle build failed. Deployment aborted.
    cd ..
    exit /b !ERRORLEVEL!
)
cd ..

echo.
echo ==================================================
echo [2/2] Running Ansible Playbook via WSL...
echo ==================================================

wsl bash -c "cd /mnt/c/Users/82107/dev/project/nemologic/infra/ansible && ANSIBLE_TIMEOUT=60 ANSIBLE_BECOME_TIMEOUT=60 ansible-playbook -i hosts.ini playbook.yml"
if !ERRORLEVEL! neq 0 (
    echo [ERROR] Ansible playbook execution failed.
    exit /b !ERRORLEVEL!
)

echo.
echo ==================================================
echo Deployment Completed Successfully!
echo ==================================================
