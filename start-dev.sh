#!/bin/bash

echo "🚀 Starting AEMS with Role-Based RAG Architecture"
echo "=================================================="

# Check if .env exists
if [ ! -f .env ]; then
    echo "⚠️  .env file not found!"
    echo "📝 Creating .env from .env.example..."
    cp .env.example .env
    echo "✅ .env created. Please edit it and add your OPENAI_API_KEY"
    echo "Then run this script again."
    exit 1
fi

# Check if OPENAI_API_KEY is set
if ! grep -q "OPENAI_API_KEY=sk-" .env; then
    echo "⚠️  OPENAI_API_KEY not configured in .env"
    echo "Please edit .env and add your OpenAI API key"
    exit 1
fi

echo "✅ Environment configured"
echo ""

# Create Python __init__ files if missing
echo "📦 Setting up Python package structure..."
mkdir -p aems-rag-service/app/models
mkdir -p aems-rag-service/app/services
mkdir -p aems-rag-service/app/routers
touch aems-rag-service/app/__init__.py
touch aems-rag-service/app/models/__init__.py
touch aems-rag-service/app/services/__init__.py
touch aems-rag-service/app/routers/__init__.py

echo "✅ Package structure ready"
echo ""

# Start services
echo "🐳 Starting Docker Compose services..."
docker-compose up -d

echo ""
echo "⏳ Waiting for services to start (30 seconds)..."
sleep 30

echo ""
echo "🏥 Health Checks:"
echo "=================="

# Check PostgreSQL
if docker exec aems-postgres pg_isready -U aems_user -d aems_db > /dev/null 2>&1; then
    echo "✅ PostgreSQL: Running"
else
    echo "❌ PostgreSQL: Not ready"
fi

# Check Python RAG Service
if curl -f http://localhost:8000/health > /dev/null 2>&1; then
    echo "✅ Python RAG Service: Running (http://localhost:8000)"
else
    echo "❌ Python RAG Service: Not ready"
fi

# Check Spring Boot
if curl -f http://localhost:8080/api/auth/login > /dev/null 2>&1; then
    echo "✅ Spring Boot Backend: Running (http://localhost:8080)"
else
    echo "⚠️  Spring Boot Backend: May still be starting..."
fi

echo ""
echo "📚 Service URLs:"
echo "=================="
echo "Python RAG API Docs:  http://localhost:8000/docs"
echo "Spring Boot Backend:  http://localhost:8080"
echo "PostgreSQL:           localhost:5432"

echo ""
echo "📋 View Logs:"
echo "=================="
echo "docker-compose logs -f rag-service"
echo "docker-compose logs -f spring-backend"
echo "docker-compose logs -f postgres"

echo ""
echo "🧪 Test Ingestion:"
echo "=================="
echo 'curl -X POST http://localhost:8000/api/ingest/document \'
echo '  -H "Content-Type: application/json" \'
echo '  -d '"'"'{"content": "Test product", "metadata": {"visibility": "public"}}'"'"

echo ""
echo "✅ Setup complete! Check the logs above for any issues."
