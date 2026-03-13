import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  email = '';
  password = '';
  error = '';
  loading = false;

  onSubmit() {
    if (!this.email || !this.password) {
      this.error = 'Veuillez remplir tous les champs.';
      return;
    }
    
    this.loading = true;
    this.error = '';

    this.authService.login({ email: this.email, password: this.password }).subscribe({
      next: (response) => {
        this.loading = false;
        console.log('Login success, redirecting...', response.roles);
        // Redirect based on role
        if (response.roles.includes('ADMIN')) {
          this.router.navigate(['/backoffice/dashboard']);
        } else if (response.roles.includes('OWNER')) {
          this.router.navigate(['/backoffice/sites']);
        } else {
          this.router.navigate(['/frontoffice/home']);
        }
      },
      error: (err) => {
        this.loading = false;
        console.error('Login error:', err);
        this.error = err.error?.message || 'Email ou mot de passe incorrect.';
      }
    });
  }
}
