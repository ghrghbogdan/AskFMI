#!/bin/bash

echo "Running all tests..."
echo ""

echo "=== Backend Tests ==="
cd backend
./gradlew test
BACKEND_STATUS=$?
cd ..

echo ""
echo "=== Frontend Tests ==="
cd frontend
npm test -- --watchAll=false
FRONTEND_STATUS=$?
cd ..

echo ""
echo "==========================="
if [ $BACKEND_STATUS -eq 0 ] && [ $FRONTEND_STATUS -eq 0 ]; then
    echo "All tests passed!"
    exit 0
else
    echo "Some tests failed"
    exit 1
fi
