@echo off
REM ============================================
REM RevTicket Complete Development Manager (Windows)
REM ============================================
REM Usage: revticket.bat [start|stop|status|restart|monitor|help]
REM ============================================

setlocal enabledelayedexpansion

REM Colors (limited in batch)
set "RED=[91m"
set "GREEN=[92m"
set "YELLOW=[93m"
set "BLUE=[94m"
set "PURPLE=[95m"
set "NC=[0m"

REM Paths
set "PROJECT_ROOT=%~dp0"
set "BACKEND_DIR=%PROJECT_ROOT%Microservices-Backend"
set "FRONTEND_DIR=%PROJECT_ROOT%Frontend"

REM Services
set "SERVICES=user-service:8081 movie-service:8082 theater-service:8083 showtime-service:8084 booking-service:8085 payment-service:8086 review-service:8087 search-service:8088 notification-service:8089 settings-service:8090 dashboard-service:8091 api-gateway:8080"

REM Status Services
set "STATUS_SERVICES=4200:Frontend 8081:User-Service 8082:Movie-Service 8083:Theater-Service 8084:Showtime-Service 8085:Booking-Service 8086:Payment-Service 8087:Review-Service 8088:Search-Service 8089:Notification-Service 8090:Settings-Service 8091:Dashboard-Service 8080:API-Gateway"

REM ============================================
REM UTILITY FUNCTIONS
REM ============================================

:print_header
echo %PURPLE%============================================%NC%
echo %PURPLE%%~1%NC%
echo %PURPLE%============================================%NC%
goto :eof

:print_status
echo %BLUE%[INFO]%NC% %~1
goto :eof

:print_success
echo %GREEN%[SUCCESS]%NC% %~1
goto :eof

:print_warning
echo %YELLOW%[WARNING]%NC% %~1
goto :eof

:print_error
echo %RED%[ERROR]%NC% %~1
goto :eof

REM ============================================
REM DEPENDENCY MANAGEMENT
REM ============================================

:check_dependencies
call :print_header "CHECKING DEPENDENCIES"

REM Check Java
java -version >nul 2>&1
if errorlevel 1 (
    call :print_error "Java not found. Please install Java 17+"
    pause
    exit /b 1
) else (
    call :print_success "Java found"
)

REM Check Maven
mvn -version >nul 2>&1
if errorlevel 1 (
    call :print_error "Maven not found. Please install Maven"
    pause
    exit /b 1
) else (
    call :print_success "Maven found"
)

REM Check Node.js
node --version >nul 2>&1
if errorlevel 1 (
    call :print_error "Node.js not found. Please install Node.js 18+"
    pause
    exit /b 1
) else (
    call :print_success "Node.js found"
)

REM Check npm
npm --version >nul 2>&1
if errorlevel 1 (
    call :print_error "npm not found. Please install npm"
    pause
    exit /b 1
) else (
    call :print_success "npm found"
)

goto :eof

:setup_environment
call :print_header "SETTING UP ENVIRONMENT"

REM Create .env if not exists
cd /d "%BACKEND_DIR%"
if not exist ".env" (
    call :print_status "Creating .env file from template..."
    copy ".env.example" ".env" >nul
    call :print_warning "Please update .env with your credentials"
)

REM Create databases (assuming MySQL is running)
call :print_status "Creating databases..."
mysql -u root -pAdmin123 -e "CREATE DATABASE IF NOT EXISTS user_service_db; CREATE DATABASE IF NOT EXISTS movie_service_db; CREATE DATABASE IF NOT EXISTS theater_service_db; CREATE DATABASE IF NOT EXISTS showtime_service_db; CREATE DATABASE IF NOT EXISTS booking_service_db; CREATE DATABASE IF NOT EXISTS payment_service_db; CREATE DATABASE IF NOT EXISTS settings_service_db;" 2>nul || call :print_warning "Database creation failed - check MySQL"

call :print_success "Environment ready"
goto :eof

:install_dependencies
call :print_header "INSTALLING DEPENDENCIES"

REM Backend dependencies
cd /d "%BACKEND_DIR%"
call :print_status "Installing Maven dependencies..."
call mvn clean install -DskipTests -q
if errorlevel 1 (
    call :print_error "Failed to install backend dependencies"
    pause
    exit /b 1
)
call :print_success "Backend dependencies installed"

REM Frontend dependencies
cd /d "%FRONTEND_DIR%"
call :print_status "Installing npm dependencies..."
call npm install --silent
if errorlevel 1 (
    call :print_error "Failed to install frontend dependencies"
    pause
    exit /b 1
)
call :print_success "Frontend dependencies installed"

goto :eof

REM ============================================
REM SERVICE MANAGEMENT
REM ============================================

:stop_services
call :print_header "STOPPING SERVICES"

REM Kill Java processes
call :print_status "Stopping Java processes..."
taskkill /f /im java.exe >nul 2>&1

REM Kill Node processes
call :print_status "Stopping Node processes..."
taskkill /f /im node.exe >nul 2>&1

REM Clean up PID files
if exist "%PROJECT_ROOT%logs" (
    del /q "%PROJECT_ROOT%logs\*.pid" >nul 2>&1
)

call :print_success "All services stopped"
goto :eof

:start_services
call :print_header "STARTING SERVICES"

REM Create logs directory
if not exist "%PROJECT_ROOT%logs" mkdir "%PROJECT_ROOT%logs"

REM Check if Consul is running
curl -s http://localhost:8500/v1/status/leader >nul 2>&1
if !errorlevel! equ 0 (
    call :print_success "Consul is running - using service discovery"
    set "USE_CONSUL=true"
) else (
    call :print_warning "Consul not available - running standalone"
    set "USE_CONSUL=false"
)

REM Start backend services
for %%s in (%SERVICES%) do (
    for /f "tokens=1,2 delims=:" %%a in ("%%s") do (
        set "service=%%a"
        set "port=%%b"
        
        if exist "%BACKEND_DIR%\!service!" (
            call :print_status "Starting !service! on port !port!..."
            cd /d "%BACKEND_DIR%\!service!"
            
            if "!USE_CONSUL!"=="true" (
                REM With Consul
                start "!service!" cmd /c "mvn spring-boot:run -Dserver.port=!port! > %PROJECT_ROOT%logs\!service!.log 2>&1"
            ) else (
                REM Without Consul
                start "!service!" cmd /c "set SPRING_CLOUD_CONSUL_ENABLED=false && set SPRING_CLOUD_DISCOVERY_ENABLED=false && set SPRING_CLOUD_SERVICE_REGISTRY_AUTO_REGISTRATION_ENABLED=false && mvn spring-boot:run -Dserver.port=!port! > %PROJECT_ROOT%logs\!service!.log 2>&1"
            )
            
            timeout /t 2 >nul
        )
    )
)

REM Start frontend
call :print_status "Starting Frontend..."
cd /d "%FRONTEND_DIR%"
start "Frontend" cmd /c "npm start > %PROJECT_ROOT%logs\frontend.log 2>&1"

call :print_success "All services started"
goto :eof

:check_status
call :print_header "SERVICE STATUS"

for %%s in (%STATUS_SERVICES%) do (
    for /f "tokens=1,2 delims=:" %%a in ("%%s") do (
        set "port=%%a"
        set "name=%%b"
        
        netstat -an | findstr :!port! >nul 2>&1
        if !errorlevel! equ 0 (
            curl -s "http://localhost:!port!" >nul 2>&1 || curl -s "http://localhost:!port!/actuator/health" >nul 2>&1
            if !errorlevel! equ 0 (
                echo %GREEN%✓%NC% !name! ^(port !port!^) - Running
            ) else (
                echo %YELLOW%⚠%NC% !name! ^(port !port!^) - Starting
            )
        ) else (
            echo %RED%✗%NC% !name! ^(port !port!^) - Stopped
        )
    )
)

echo.
echo %BLUE%Access URLs:%NC%
echo Frontend:     %GREEN%http://localhost:4200%NC%
echo API Gateway:  %GREEN%http://localhost:8080%NC%
echo Consul:       %GREEN%http://localhost:8500%NC%

if exist "%PROJECT_ROOT%logs" (
    echo Logs:         %GREEN%%PROJECT_ROOT%logs\%NC%
)

goto :eof

:monitor_services
call :print_header "MONITORING SERVICES"

:monitor_loop
cls
echo %PURPLE%RevTicket Services - %date% %time%%NC%
echo %PURPLE%================================%NC%

for %%s in (%STATUS_SERVICES%) do (
    for /f "tokens=1,2 delims=:" %%a in ("%%s") do (
        set "port=%%a"
        set "name=%%b"
        
        netstat -an | findstr :!port! >nul 2>&1
        if !errorlevel! equ 0 (
            echo %GREEN%✓%NC% !name!
        ) else (
            echo %RED%✗%NC% !name!
        )
    )
)

echo.
echo %YELLOW%Press Ctrl+C to exit monitoring%NC%
timeout /t 5 >nul
goto monitor_loop

REM ============================================
REM MAIN COMMANDS
REM ============================================

:cmd_start
call :check_dependencies
call :setup_environment
call :install_dependencies
call :start_services

call :print_header "STARTUP COMPLETE"
echo %GREEN%Frontend:%NC% http://localhost:4200
echo %GREEN%API Gateway:%NC% http://localhost:8080
echo %GREEN%Consul:%NC% http://localhost:8500

echo.
echo Commands:
echo   revticket.bat status   - Check service status
echo   revticket.bat stop     - Stop all services
echo   revticket.bat monitor  - Live monitoring

goto :eof

:cmd_stop
call :stop_services
goto :eof

:cmd_status
call :check_status
goto :eof

:cmd_restart
call :stop_services
timeout /t 3 >nul
call :cmd_start
goto :eof

:cmd_monitor
call :monitor_services
goto :eof

:show_help
echo %PURPLE%RevTicket Development Manager (Windows)%NC%
echo.
echo Usage: revticket.bat [command]
echo.
echo Commands:
echo   start     - Start all services ^(default^)
echo   stop      - Stop all services
echo   status    - Check service status
echo   restart   - Restart all services
echo   monitor   - Live service monitoring
echo   help      - Show this help
echo.
echo Features:
echo   • Auto dependency check
echo   • Database setup ^(MySQL, MongoDB^)
echo   • Consul service discovery
echo   • Hot reload for development
echo   • Comprehensive logging

goto :eof

REM ============================================
REM MAIN EXECUTION
REM ============================================

:main
set "command=%~1"
if "%command%"=="" set "command=start"

if "%command%"=="start" (
    call :cmd_start
) else if "%command%"=="stop" (
    call :cmd_stop
) else if "%command%"=="status" (
    call :cmd_status
) else if "%command%"=="restart" (
    call :cmd_restart
) else if "%command%"=="monitor" (
    call :cmd_monitor
) else if "%command%"=="help" (
    call :show_help
) else if "%command%"=="--help" (
    call :show_help
) else if "%command%"=="-h" (
    call :show_help
) else (
    echo %RED%Unknown command: %command%%NC%
    call :show_help
    exit /b 1
)

goto :eof

REM Start main execution
call :main %*