@echo off
setlocal EnableDelayedExpansion

REM Configuration
set "PROJECT_ROOT=%~dp0"
set "BACKEND_DIR=%PROJECT_ROOT%Microservices-Backend"

REM Function to print section headers (Label at end of file)

echo ============================================
echo RUNNING BACKEND TESTS
echo ============================================

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo Error: Maven is not installed or not in your PATH.
    pause
    exit /b 1
)

REM Navigate to backend directory
if not exist "%BACKEND_DIR%" (
    echo Error: Backend directory not found!
    pause
    exit /b 1
)
cd /d "%BACKEND_DIR%"

REM Run tests
echo Executing 'mvn test' in %BACKEND_DIR%...
echo This will run unit tests for all microservices.
echo.

call mvn test
set "EXIT_CODE=%ERRORLEVEL%"

echo.
if %EXIT_CODE% equ 0 (
    echo ============================================
    echo TESTS COMPLETED SUCCESSFULLY
    echo ============================================
    echo All tests passed!
) else (
    echo ============================================
    echo TESTS FAILED
    echo ============================================
    echo Some tests failed. Check the output above for details.
)

REM Summary of report locations
echo ============================================
echo TEST RESULTS LOCATIONS
echo ============================================
echo Detailed test reports can be found in the 'target\surefire-reports' directory of each service:
echo.

for /d %%D in (*) do (
    if exist "%%D\target\surefire-reports" (
        echo - %%D: %BACKEND_DIR%\%%D\target\surefire-reports
    )
)

echo.
echo To run tests for a specific service, navigate to its directory and run 'mvn test'.
pause
exit /b %EXIT_CODE%
