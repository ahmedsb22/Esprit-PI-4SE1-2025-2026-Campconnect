import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './forgot-password.component.html'
})
export class ForgotPasswordComponent {
  private authService = inject(AuthService);

  email = '';
  message = '';
  error = '';
  loading = false;

  onSubmit() {
    if (!this.email) {
      this.error = 'Veuillez saisir votre email.';
      return;
    }

    this.loading = true;
    this.error = '';
    this.message = '';

    this.authService.forgotPassword(this.email).subscribe({
      next: () => {
        this.loading = false;
        this.message = 'Si un compte existe avec cet email, un lien de réinitialisation a été envoyé.';
      },
      error: () => {
        this.loading = false;
        // Generic message for security
        this.message = 'Si un compte existe avec cet email, un lien de réinitialisation a été envoyé.';
      }
    });
  }
}
