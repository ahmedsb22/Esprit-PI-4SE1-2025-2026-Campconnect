import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  email = '';
  password = '';
  error = '';
  loading = false;

  private apiUrl = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient, private router: Router) {}

  onSubmit() {
    if (!this.email) {
      this.error = 'Email is required.';
      return;
    }
    this.loading = true;
    this.error = '';

    // No auth: find user by email from public /api/users, redirect to home
    this.http.get<any[]>(this.apiUrl).subscribe({
      next: (users) => {
        const user = users.find(u => u.email === this.email);
        if (user) {
          this.router.navigate(['/frontoffice/home']);
        } else {
          this.error = 'No account found with this email. Please register first.';
          this.loading = false;
        }
      },
      error: (err: HttpErrorResponse) => {
        this.error = 'Could not connect to server.';
        this.loading = false;
      }
    });
  }
}
