import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
  firstName = '';
  lastName = '';
  email = '';
  password = '';
  error = '';
  loading = false;

  private apiUrl = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient, private router: Router) {}

  onSubmit() {
    if (!this.firstName || !this.lastName || !this.email) {
      this.error = 'First name, last name and email are required.';
      return;
    }
    this.loading = true;
    this.error = '';

    // No auth: POST directly to /api/users
    const payload = {
      firstName: this.firstName,
      lastName: this.lastName,
      email: this.email,
      password: this.password || null
    };

    this.http.post<any>(this.apiUrl, payload).subscribe({
      next: () => {
        this.router.navigate(['/frontoffice/home']);
      },
      error: (err: HttpErrorResponse) => {
        this.error = err.error?.message || 'Registration failed. Email may already be in use.';
        this.loading = false;
      }
    });
  }
}
