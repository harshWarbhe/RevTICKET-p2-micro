#!/bin/bash

# ============================================
# RevTicket Complete Development Manager
# ============================================
# Usage: ./revticket.sh [start|stop|status|restart]
# ============================================

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

# Paths
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$PROJECT_ROOT/Microservices-Backend"
FRONTEND_DIR="$PROJECT_ROOT/Frontend"

# Services
SERVICES=(
    "user-service:8081"
    "movie-service:8082"
    "theater-service:8083"
    "showtime-service:8084"
    "booking-service:8085"
    "payment-service:8086"
    "review-service:8087"
    "search-service:8088"
    "notification-service:8089"
    "settings-service:8090"
    "dashboard-service:8091"
    "api-gateway:8080"
)

STATUS_SERVICES=(
    "4200:Frontend"
    "8081:User Service"
    "8082:Movie Service"
    "8083:Theater Service"
    "8084:Showtime Service"
    "8085:Booking Service"
    "8086:Payment Service"
    "8087:Review Service"
    "8088:Search Service"
    "8089:Notification Service"
    "8090:Settings Service"
    "8091:Dashboard Service"
    "8080:API Gateway"
)

# ============================================
# UTILITY FUNCTIONS
# ============================================

print_header() {
    echo -e "${PURPLE}============================================${NC}"
    echo -e "${PURPLE}$1${NC}"
    echo -e "${PURPLE}============================================${NC}"
}

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# ============================================
# DEPENDENCY MANAGEMENT
# ============================================

check_dependencies() {
    print_header "CHECKING DEPENDENCIES"
    
    # Java
    if ! command -v java &> /dev/null; then
        print_error "Java not found. Install Java 17+"
        exit 1
    fi
    print_success "Java: $(java -version 2>&1 | head -n 1)"
    
    # Maven
    if ! command -v mvn &> /dev/null; then
        print_error "Maven not found. Installing..."
        brew install maven
    fi
    print_success "Maven found"
    
    # Node.js
    if ! command -v node &> /dev/null; then
        print_error "Node.js not found. Install Node.js 18+"
        exit 1
    fi
    print_success "Node.js: $(node --version)"
    
    # npm
    if ! command -v npm &> /dev/null; then
        print_error "npm not found"
        exit 1
    fi
    print_success "npm: $(npm --version)"
}

setup_environment() {
    print_header "SETTING UP ENVIRONMENT"
    
    # Start databases
    print_status "Starting MySQL and MongoDB..."
    brew services start mysql 2>/dev/null || true
    brew services start mongodb-community 2>/dev/null || true
    
    # Start Consul
    print_status "Starting Consul..."
    brew services start consul 2>/dev/null || true
    sleep 5
    
    # Create .env if not exists
    cd "$BACKEND_DIR"
    if [ ! -f ".env" ]; then
        cp .env.example .env
        print_status ".env created from template"
    fi
    
    # Create databases
    print_status "Creating databases..."
    mysql -u root -pAdmin123 -e "
        CREATE DATABASE IF NOT EXISTS user_service_db;
        CREATE DATABASE IF NOT EXISTS movie_service_db;
        CREATE DATABASE IF NOT EXISTS theater_service_db;
        CREATE DATABASE IF NOT EXISTS showtime_service_db;
        CREATE DATABASE IF NOT EXISTS booking_service_db;
        CREATE DATABASE IF NOT EXISTS payment_service_db;
        CREATE DATABASE IF NOT EXISTS settings_service_db;
    " 2>/dev/null || print_warning "Database creation failed - check MySQL"
    
    print_success "Environment ready"
}

install_dependencies() {
    print_header "INSTALLING DEPENDENCIES"
    
    # Backend
    cd "$BACKEND_DIR"
    print_status "Installing Maven dependencies..."
    mvn clean install -DskipTests -q
    print_success "Backend dependencies installed"
    
    # Frontend
    cd "$FRONTEND_DIR"
    print_status "Installing npm dependencies..."
    npm install --silent
    print_success "Frontend dependencies installed"
}

# ============================================
# SERVICE MANAGEMENT
# ============================================

stop_services() {
    print_header "STOPPING SERVICES"
    
    # Kill processes
    pkill -f "spring-boot" 2>/dev/null || true
    pkill -f "ng serve" 2>/dev/null || true
    
    # Clean up PID files
    rm -rf "$PROJECT_ROOT/logs"/*.pid 2>/dev/null || true
    
    print_success "All services stopped"
}

start_services() {
    print_header "STARTING SERVICES"
    
    mkdir -p "$PROJECT_ROOT/logs"
    
    # Check if Consul is running
    if curl -s http://localhost:8500/v1/status/leader &>/dev/null; then
        print_success "Consul is running - using service discovery"
        USE_CONSUL=true
    else
        print_warning "Consul not available - running standalone"
        USE_CONSUL=false
    fi
    
    # Start backend services
    for service_info in "${SERVICES[@]}"; do
        service=$(echo $service_info | cut -d: -f1)
        port=$(echo $service_info | cut -d: -f2)
        
        if [ -d "$BACKEND_DIR/$service" ]; then
            print_status "Starting $service on port $port..."
            cd "$BACKEND_DIR/$service"
            
            if [ "$USE_CONSUL" = true ]; then
                # With Consul
                nohup mvn spring-boot:run -Dserver.port=$port \
                    > "$PROJECT_ROOT/logs/$service.log" 2>&1 &
            else
                # Without Consul
                env SPRING_CLOUD_CONSUL_ENABLED=false \
                    SPRING_CLOUD_DISCOVERY_ENABLED=false \
                    SPRING_CLOUD_SERVICE_REGISTRY_AUTO_REGISTRATION_ENABLED=false \
                    nohup mvn spring-boot:run -Dserver.port=$port \
                    > "$PROJECT_ROOT/logs/$service.log" 2>&1 &
            fi
            
            echo $! > "$PROJECT_ROOT/logs/$service.pid"
            sleep 2
        fi
    done
    
    # Start frontend
    print_status "Starting Frontend..."
    cd "$FRONTEND_DIR"
    nohup npm start > "$PROJECT_ROOT/logs/frontend.log" 2>&1 &
    echo $! > "$PROJECT_ROOT/logs/frontend.pid"
    
    print_success "All services started"
}

check_status() {
    print_header "SERVICE STATUS"
    
    for service in "${STATUS_SERVICES[@]}"; do
        port=$(echo $service | cut -d: -f1)
        name=$(echo $service | cut -d: -f2)
        
        if lsof -ti:$port &>/dev/null; then
            if curl -s "http://localhost:$port" &>/dev/null || curl -s "http://localhost:$port/actuator/health" &>/dev/null; then
                echo -e "${GREEN}✓${NC} $name (port $port) - Running"
            else
                echo -e "${YELLOW}⚠${NC} $name (port $port) - Starting"
            fi
        else
            echo -e "${RED}✗${NC} $name (port $port) - Stopped"
        fi
    done
    
    echo ""
    echo -e "${BLUE}Access URLs:${NC}"
    echo -e "Frontend:     ${GREEN}http://localhost:4200${NC}"
    echo -e "API Gateway:  ${GREEN}http://localhost:8080${NC}"
    echo -e "Consul:       ${GREEN}http://localhost:8500${NC}"
    
    if [ -d "$PROJECT_ROOT/logs" ]; then
        echo -e "Logs:         ${GREEN}$PROJECT_ROOT/logs/${NC}"
    fi
}

monitor_services() {
    print_header "MONITORING SERVICES"
    
    while true; do
        clear
        echo -e "${PURPLE}RevTicket Services - $(date)${NC}"
        echo -e "${PURPLE}================================${NC}"
        
        for service in "${STATUS_SERVICES[@]}"; do
            port=$(echo $service | cut -d: -f1)
            name=$(echo $service | cut -d: -f2)
            
            if lsof -ti:$port &>/dev/null; then
                echo -e "${GREEN}✓${NC} $name"
            else
                echo -e "${RED}✗${NC} $name"
            fi
        done
        
        echo -e "\n${YELLOW}Press Ctrl+C to exit monitoring${NC}"
        sleep 5
    done
}

# ============================================
# MAIN COMMANDS
# ============================================

cmd_start() {
    check_dependencies
    setup_environment
    install_dependencies
    start_services
    
    print_header "STARTUP COMPLETE"
    echo -e "${GREEN}Frontend:${NC} http://localhost:4200"
    echo -e "${GREEN}API Gateway:${NC} http://localhost:8080"
    echo -e "${GREEN}Consul:${NC} http://localhost:8500"
    
    echo ""
    echo "Commands:"
    echo "  ./revticket.sh status   - Check service status"
    echo "  ./revticket.sh stop     - Stop all services"
    echo "  ./revticket.sh monitor  - Live monitoring"
}

cmd_stop() {
    stop_services
}

cmd_status() {
    check_status
}

cmd_restart() {
    stop_services
    sleep 3
    cmd_start
}

cmd_monitor() {
    monitor_services
}

show_help() {
    echo -e "${PURPLE}RevTicket Development Manager${NC}"
    echo ""
    echo "Usage: ./revticket.sh [command]"
    echo ""
    echo "Commands:"
    echo "  start     - Start all services (default)"
    echo "  stop      - Stop all services"
    echo "  status    - Check service status"
    echo "  restart   - Restart all services"
    echo "  monitor   - Live service monitoring"
    echo "  help      - Show this help"
    echo ""
    echo "Features:"
    echo "  • Auto dependency check & install"
    echo "  • Database setup (MySQL, MongoDB)"
    echo "  • Consul service discovery"
    echo "  • Hot reload for development"
    echo "  • Comprehensive logging"
}

# ============================================
# MAIN EXECUTION
# ============================================

main() {
    case "${1:-start}" in
        start)
            cmd_start
            ;;
        stop)
            cmd_stop
            ;;
        status)
            cmd_status
            ;;
        restart)
            cmd_restart
            ;;
        monitor)
            cmd_monitor
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            echo -e "${RED}Unknown command: $1${NC}"
            show_help
            exit 1
            ;;
    esac
}

# Cleanup on exit
trap 'echo -e "\n${YELLOW}Exiting...${NC}"' EXIT INT TERM

main "$@"