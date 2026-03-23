import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';
import { LoginRequest, AuthResponse } from '../models/auth.model';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(() => {
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AuthService,
        { provide: Router, useValue: routerSpy }
      ]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should login and store user data', () => {
    const mockCredentials: LoginRequest = { email: 'test@test.tn', password: 'password' };
    const mockResponse: AuthResponse = {
      token: 'jwt-token',
      email: 'test@test.tn',
      firstName: 'Test',
      lastName: 'User',
      roles: ['CAMPER']
    };

    service.login(mockCredentials).subscribe(response => {
      expect(response).toEqual(mockResponse);
      expect(localStorage.getItem('campconnect_token')).toBe('jwt-token');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('should logout and clear storage', () => {
    localStorage.setItem('campconnect_token', 'token');
    service.logout();
    expect(localStorage.getItem('campconnect_token')).toBeNull();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/frontoffice/login']);
  });

  it('should check authentication status correctly', () => {
    expect(service.isAuthenticated()).toBeFalse();
    localStorage.setItem('campconnect_token', 'token');
    expect(service.isAuthenticated()).toBeTrue();
  });

  it('should handle 401 Unauthorized during login', () => {
    const mockCredentials: LoginRequest = { email: 'wrong@test.tn', password: 'wrongpassword' };
    
    service.login(mockCredentials).subscribe({
      next: () => fail('Should have failed with 401 error'),
      error: (error) => {
        expect(error.status).toBe(401);
      }
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    req.flush('Invalid credentials', { status: 401, statusText: 'Unauthorized' });
  });

  it('should handle registration with full profile data', () => {
    const mockRegisterData = {
      firstName: 'Ahmed',
      lastName: 'SB',
      email: 'ahmed@test.tn',
      password: 'password123',
      phone: '12345678',
      address: 'Tunis',
      roles: ['CAMPER']
    };

    service.register(mockRegisterData).subscribe(response => {
      expect(response.email).toBe('ahmed@test.tn');
      expect(localStorage.getItem('campconnect_token')).toBeTruthy();
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/register`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(mockRegisterData);
    req.flush({ token: 'token-123', email: 'ahmed@test.tn', roles: ['CAMPER'] });
  });
});
