import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RegisterComponent } from './register.component';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { CommonModule } from '@angular/common';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['register']);

    await TestBed.configureTestingModule({
      imports: [RegisterComponent, FormsModule, RouterTestingModule, CommonModule],
      providers: [
        { provide: AuthService, useValue: authServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    spyOn(router, 'navigate');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show error if mandatory fields are empty', () => {
    component.firstName = '';
    component.lastName = '';
    component.email = '';
    component.password = '';
    component.onSubmit();
    expect(component.error).toBe('Veuillez remplir les champs obligatoires.');
  });

  it('should call authService.register on submit', () => {
    const mockResponse = {
      token: 'jwt-123',
      email: 'john@test.tn',
      firstName: 'John',
      lastName: 'Doe',
      roles: ['CAMPER']
    };
    authServiceSpy.register.and.returnValue(of(mockResponse));
    
    component.firstName = 'John';
    component.lastName = 'Doe';
    component.email = 'john@test.tn';
    component.password = 'password123';
    component.onSubmit();

    expect(authServiceSpy.register).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/frontoffice/home']);
  });

  it('should handle registration error', () => {
    authServiceSpy.register.and.returnValue(throwError(() => ({ error: { message: 'Email already exists' } })));
    
    component.firstName = 'John';
    component.lastName = 'Doe';
    component.email = 'existing@test.tn';
    component.password = 'password123';
    component.onSubmit();

    expect(component.error).toBe('Email already exists');
    expect(component.loading).toBeFalse();
  });
});
