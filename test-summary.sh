#!/bin/bash

# Test Coverage Summary Script
# Usage: ./test-summary.sh [options]
# Options:
#   --run-tests    Run tests first, then show summary
#   --coverage     Generate coverage report first, then show summary
#   --help         Show this help message

show_help() {
    echo "Test Coverage Summary Script"
    echo "Usage: ./test-summary.sh [options]"
    echo ""
    echo "Options:"
    echo "  --run-tests    Run tests first, then show summary"
    echo "  --coverage     Generate coverage report first, then show summary"
    echo "  --help         Show this help message"
    echo ""
    echo "Examples:"
    echo "  ./test-summary.sh                    # Show summary from existing test results"
    echo "  ./test-summary.sh --run-tests        # Run tests and show summary"
    echo "  ./test-summary.sh --coverage         # Generate coverage and show summary"
}

case "$1" in
    --run-tests)
        echo "🧪 Running tests..."
        ./gradlew :aivoice:test
        echo ""
        echo "📊 Generating test summary..."
        python3 test_coverage_summary.py
        ;;
    --coverage)
        echo "🧪 Running tests with coverage..."
        ./gradlew :aivoice:test :aivoice:jacocoTestReport
        echo ""
        echo "📊 Generating test summary..."
        python3 test_coverage_summary.py
        ;;
    --help)
        show_help
        ;;
    "")
        echo "📊 Generating test summary from existing results..."
        python3 test_coverage_summary.py
        ;;
    *)
        echo "❌ Unknown option: $1"
        echo ""
        show_help
        exit 1
        ;;
esac 