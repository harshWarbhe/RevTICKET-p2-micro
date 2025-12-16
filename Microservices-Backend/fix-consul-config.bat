@echo off
echo Fixing Consul configuration in all services...

for %%s in (user-service movie-service theater-service showtime-service booking-service payment-service review-service search-service notification-service settings-service dashboard-service) do (
    echo Updating %%s...
    powershell -Command "(Get-Content '%%s\src\main\resources\application.yml') -replace 'instance-id: \$\{spring\.application\.name\}:\$\{server\.port\}', 'instance-id: ${spring.application.name}:${server.port}^r^n        hostname: localhost^r^n        prefer-ip-address: false' | Set-Content '%%s\src\main\resources\application.yml'"
)

echo Done! Please restart all services.
pause