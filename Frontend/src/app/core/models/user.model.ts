export interface User {
  id: string;
  email: string;
  name: string;
  role: 'USER' | 'ADMIN';
  phone?: string;
  dateOfBirth?: string;
  gender?: string;
  address?: string;
  preferredLanguage?: string;
  emailNotifications?: boolean;
  smsNotifications?: boolean;
  pushNotifications?: boolean;
  isActive?: boolean;
  status?: 'ACTIVE' | 'BLOCKED';
  lastLogin?: Date;
  createdAt: Date;
  profileImage?: string;
}

export interface UserBooking {
  id: string;
  movieTitle: string;
  theaterName: string;
  showDate: string;
  showTime: string;
  totalAmount: number;
  status: string;
  bookingDate: Date;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  name: string;
  email: string;
  password: string;
  phone?: string;
  dateOfBirth?: string;
  gender?: string;
  address?: string;
  preferredLanguage?: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}