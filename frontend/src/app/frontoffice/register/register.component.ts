import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  firstName = '';
  lastName = '';
  email = '';
  password = '';
  phone = '';
  address = '';
  role = 'CAMPER';
  error = '';
  loading = false;

  onSubmit() {
    if (!this.firstName || !this.lastName || !this.email || !this.password) {
      this.error = 'Veuillez remplir les champs obligatoires.';
      return;
    }
    
    this.loading = true;
    this.error = '';

    const registerData = {
      firstName: this.firstName,
      lastName: this.lastName,
      email: this.email,
      password: this.password,
      phone: this.phone,
      address: this.address,
      roles: [this.role]
    };

    this.authService.register(registerData).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/frontoffice/home']);
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'L\'inscription a échoué. L\'email est peut-être déjà utilisé.';
      }
    });
  }
}
