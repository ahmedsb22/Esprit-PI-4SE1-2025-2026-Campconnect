import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { AuthService } from '../../services/auth.service';
import { Router, ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { CommonModule } from '@angular/common';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['login']);

    await TestBed.configureTestingModule({
      imports: [LoginComponent, FormsModule, RouterTestingModule, CommonModule],
      providers: [
        { provide: AuthService, useValue: authServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    spyOn(router, 'navigate');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show error if fields are empty', () => {
    component.email = '';
    component.password = '';
    component.onSubmit();
    expect(component.error).toBe('Veuillez remplir tous les champs.');
  });

  it('should call authService.login on submit', () => {
    authServiceSpy.login.and.returnValue(of({ token: 'abc', email: 'test@test.tn', roles: ['CAMPER'], firstName: 'F', lastName: 'L' }));
    
    component.email = 'test@test.tn';
    component.password = 'password';
    component.onSubmit();

    expect(authServiceSpy.login).toHaveBeenCalledWith({ email: 'test@test.tn', password: 'password' });
    expect(router.navigate).toHaveBeenCalledWith(['/frontoffice/home']);
  });

  it('should handle login error', () => {
    authServiceSpy.login.and.returnValue(throwError(() => ({ error: { message: 'Invalid' } })));
    
    component.email = 'wrong@test.tn';
    component.password = 'wrong';
    component.onSubmit();

    expect(component.error).toBe('Invalid');
    expect(component.loading).toBeFalse();
  });
});
