import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth.service';
import { AlertService } from '../../../core/services/alert.service';

@Component({
  selector: 'app-admin-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  private fb = inject(FormBuilder);
  private userService = inject(UserService);
  private authService = inject(AuthService);
  private alertService = inject(AlertService);

  profileForm!: FormGroup;
  passwordForm!: FormGroup;
  loading = signal(false);
  user = signal<any>(null);

  ngOnInit(): void {
    this.initForms();
    this.loadProfile();
  }

  initForms(): void {
    this.profileForm = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: ['']
    });

    this.passwordForm = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required]
    });
  }

  loadProfile(): void {
    this.loading.set(true);
    const currentUser = this.authService.getCurrentUser();
    if (currentUser) {
      this.user.set(currentUser);
      this.profileForm.patchValue({
        name: currentUser.name,
        email: currentUser.email,
        phone: currentUser.phone || ''
      });
    }
    this.loading.set(false);
  }

  updateProfile(): void {
    if (this.profileForm.invalid) return;

    this.loading.set(true);
    const userId = this.user()?.id;
    
    this.userService.updateUser(userId, this.profileForm.value).subscribe({
      next: () => {
        this.alertService.success('Profile updated successfully!');
        this.loading.set(false);
      },
      error: () => {
        this.alertService.error('Failed to update profile');
        this.loading.set(false);
      }
    });
  }

  changePassword(): void {
    if (this.passwordForm.invalid) return;

    const { newPassword, confirmPassword } = this.passwordForm.value;
    if (newPassword !== confirmPassword) {
      this.alertService.error('Passwords do not match');
      return;
    }

    this.loading.set(true);
    this.authService.changePassword(this.passwordForm.value).subscribe({
      next: () => {
        this.alertService.success('Password changed successfully!');
        this.passwordForm.reset();
        this.loading.set(false);
      },
      error: () => {
        this.alertService.error('Failed to change password');
        this.loading.set(false);
      }
    });
  }
}
