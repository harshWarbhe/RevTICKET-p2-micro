import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { AlertService } from '../services/alert.service';
import { AuthService } from '../services/auth.service';

let lastUnauthorizedTime = 0;
const UNAUTHORIZED_THROTTLE = 3000;

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const alertService = inject(AlertService);
  const authService = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'An unknown error occurred';
      let showAlert = true;

      if (error.status === 0) {
        errorMessage = 'Backend is not reachable. Please ensure Spring Boot server is running.';
        console.error('Connection refused - Backend server is offline');
      } else if (error.error instanceof ErrorEvent) {
        errorMessage = `Error: ${error.error.message}`;
      } else {
        switch (error.status) {
          case 400:
            errorMessage = error.error?.message || error.error || 'Bad request';
            break;
          case 401:
            // Don't logout on auth endpoints (login/register)
            if (req.url.includes('/auth/login') || req.url.includes('/auth/register')) {
              errorMessage = error.error?.message || 'Invalid credentials';
            } else {
              const now = Date.now();
              if (now - lastUnauthorizedTime > UNAUTHORIZED_THROTTLE) {
                lastUnauthorizedTime = now;
                errorMessage = 'Session expired. Please login again.';
                authService.logout();
                router.navigate(['/auth/login']);
              } else {
                showAlert = false;
              }
            }
            break;
          case 403:
            // Don't show alerts for admin/booking endpoints - user simply lacks permission
            if (req.url.includes('/admin/') || req.url.includes('/bookings/all')) {
              showAlert = false;
              errorMessage = 'Access forbidden';
            } else {
              errorMessage = 'Access forbidden';
            }
            break;
          case 404:
            errorMessage = error.error?.message || error.error || 'Resource not found';
            break;
          case 500:
            errorMessage = 'Server error. Please try again later.';
            break;
          default:
            errorMessage = error.error?.message || error.error || `Error: ${error.status} ${error.statusText}`;
        }
      }

      // Don't log 403 errors for admin/booking endpoints to console
      // Also suppress 503 errors for bookings during startup
      const isExpectedError = (error.status === 403 && (req.url.includes('/admin/') || req.url.includes('/bookings/all'))) ||
        (error.status === 503 && req.url.includes('/bookings/all'));

      if (!isExpectedError) {
        console.error('HTTP Error:', {
          status: error.status,
          message: errorMessage,
          url: error.url
        });
      }

      if (showAlert) {
        alertService.error(errorMessage);
      }

      return throwError(() => error);
    })
  );
};

