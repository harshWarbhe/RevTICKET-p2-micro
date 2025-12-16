import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { AlertService } from '../../core/services/alert.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private alertService = inject(AlertService);
  private router = inject(Router);

  forgotPasswordForm!: FormGroup;
  loading = signal(false);
  error = signal('');
  success = signal('');

  ngOnInit(): void {
    this.forgotPasswordForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onSubmit(): void {
    if (this.forgotPasswordForm.valid) {
      this.loading.set(true);
      this.error.set('');
      this.success.set('');
      
      const email = this.forgotPasswordForm.get('email')?.value;
      
      this.authService.forgotPassword(email).subscribe({
        next: () => {
          this.success.set('Password reset instructions sent to your email');
          this.alertService.success('Password reset email sent successfully');
          this.loading.set(false);
        },
        error: (error) => {
          this.error.set('Failed to send reset email. Please try again.');
          this.loading.set(false);
        }
      });
    }
  }
}