import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

let tokenCleanupDone = false;

export const tokenInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const token = localStorage.getItem('token');
  
  console.log('Token interceptor - URL:', req.url, 'Token exists:', !!token);
  
  if (req.url.includes('/auth/') || (req.url.includes('/settings') && !req.url.includes('/admin/settings'))) {
    console.log('Skipping token for public endpoint');
    return next(req);
  }
  
  if (token) {
    tokenCleanupDone = false;
    console.log('Adding token to request');
    req = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`)
    });
  } else {
    console.log('No token available');
  }
  
  return next(req);
};

function isTokenValid(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    const currentTime = Math.floor(Date.now() / 1000);
    return payload.exp > currentTime;
  } catch (error) {
    return false;
  }
}