import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, timeout, catchError, throwError } from 'rxjs';
import { User } from '../models/user.model';
import { environment } from '../../../environments/environment';

export interface PasswordChangeRequest {
  currentPassword: string;
  newPassword: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private http = inject(HttpClient);

  getUserProfile(): Observable<User> {
    return this.http.get<User>(`${environment.apiUrl}/users/profile`).pipe(
      timeout(5000),
      catchError(err => {
        console.error('getUserProfile error:', err);
        if (err.name === 'TimeoutError') {
          return throwError(() => new Error('Backend server is not responding. Please ensure API Gateway is running.'));
        }
        return throwError(() => err);
      })
    );
  }

  updateProfile(userData: Partial<User>): Observable<User> {
    return this.http.put<User>(`${environment.apiUrl}/users/profile`, userData);
  }

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${environment.apiUrl}/users`);
  }

  changePassword(request: PasswordChangeRequest): Observable<string> {
    return this.http.put(`${environment.apiUrl}/users/change-password`, request, { responseType: 'text' });
  }

  updateUser(userId: string, userData: Partial<User>): Observable<User> {
    return this.http.put<User>(`${environment.apiUrl}/users/${userId}`, userData);
  }

  deleteUser(userId: string): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/users/${userId}`);
  }
}