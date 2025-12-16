#!/bin/bash

# Configuration
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$PROJECT_ROOT/Microservices-Backend"

# Function to print section headers
print_header() {
    echo "============================================"
    echo "$1"
    echo "============================================"
}

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in your PATH."
    exit 1
fi

# Navigate to backend directory
cd "$BACKEND_DIR" || { echo "Error: Backend directory not found!"; exit 1; }

# Run tests
print_header "RUNNING BACKEND TESTS"
echo "Executing 'mvn test' in $BACKEND_DIR..."
echo "This will run unit tests for all microservices."
echo ""

mvn test

# Capture exit code
EXIT_CODE=$?

echo ""
if [ $EXIT_CODE -eq 0 ]; then
    print_header "TESTS COMPLETED SUCCESSFULLY"
    echo "All tests passed!"
else
    print_header "TESTS FAILED"
    echo "Some tests failed. Check the output above for details."
fi

# Summary of report locations
print_header "TEST RESULTS LOCATIONS"
echo "Detailed test reports can be found in the 'target/surefire-reports' directory of each service:"
echo ""
for module in */ ; do
    if [ -d "$module/target/surefire-reports" ]; then
        echo "- ${module%/}: $BACKEND_DIR/${module}target/surefire-reports"
    fi
done

echo ""
echo "To run tests for a specific service, navigate to its directory and run 'mvn test'."
