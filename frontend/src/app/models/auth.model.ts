export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phone?: string;
  address?: string;
  roles?: string[];
}

export interface AuthResponse {
  token: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
}

export interface User {
  id?: number;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  address?: string;
  profileImage?: string;
  roles: Role[];
  createdAt?: string;
  updatedAt?: string;
}

export interface Role {
  id?: number;
  name: string;
}

export interface UpdateProfileRequest {
  firstName: string;
  lastName: string;
  phone?: string;
  address?: string;
  profileImage?: string;
}
