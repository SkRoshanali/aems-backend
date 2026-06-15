@echo off
echo 🚀 Starting AEMS with Role-Based RAG Architecture
echo ==================================================
echo.

REM Check if .env exists
if not exist .env (
    echo ⚠️  .env file not found!
    echo 📝 Creating .env from .env.example...
    copy .env.example .env
    echo ✅ .env created. Please edit it and add your OPENAI_API_KEY
    echo Then run this script again.
    pause
    exit /b 1
)

echo ✅ Environment configured
echo.

REM Create Python package structure
echo 📦 Setting up Python package structure...
if not exist aems-rag-service\app\models mkdir aems-rag-service\app\models
if not exist aems-rag-service\app\services mkdir aems-rag-service\app\services
if not exist aems-rag-service\app\routers mkdir aems-rag-service\app\routers
type nul > aems-rag-service\app\__init__.py
type nul > aems-rag-service\app\models\__init__.py
type nul > aems-rag-service\app\services\__init__.py
type nul > aems-rag-service\app\routers\__init__.py

echo ✅ Package structure ready
echo.

REM Start services
echo 🐳 Starting Docker Compose services...
docker-compose up -d

echo.
echo ⏳ Waiting for services to start (30 seconds)...
timeout /t 30 /nobreak > nul

echo.
echo 🏥 Health Checks:
echo ==================
echo Checking services...

REM Check PostgreSQL
docker exec aems-postgres pg_isready -U aems_user -d aems_db > nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ PostgreSQL: Running
) else (
    echo ❌ PostgreSQL: Not ready
)

REM Check Python RAG Service
curl -f http://localhost:8000/health > nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Python RAG Service: Running (http://localhost:8000^)
) else (
    echo ❌ Python RAG Service: Not ready
)

REM Check Spring Boot
curl -f http://localhost:8080/api/auth/login > nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Spring Boot Backend: Running (http://localhost:8080^)
) else (
    echo ⚠️  Spring Boot Backend: May still be starting...
)

echo.
echo 📚 Service URLs:
echo ==================
echo Python RAG API Docs:  http://localhost:8000/docs
echo Spring Boot Backend:  http://localhost:8080
echo PostgreSQL:           localhost:5432

echo.
echo 📋 View Logs:
echo ==================
echo docker-compose logs -f rag-service
echo docker-compose logs -f spring-backend
echo docker-compose logs -f postgres

echo.
echo 🧪 Test Ingestion:
echo ==================
echo curl -X POST http://localhost:8000/api/ingest/document ^
echo   -H "Content-Type: application/json" ^
echo   -d "{\"content\": \"Test product\", \"metadata\": {\"visibility\": \"public\"}}"

echo.
echo ✅ Setup complete! Check the logs above for any issues.
echo.
pause
