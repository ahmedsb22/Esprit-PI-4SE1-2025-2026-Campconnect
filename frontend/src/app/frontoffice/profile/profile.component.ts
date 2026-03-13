import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/auth.model';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.component.html'
})
export class ProfileComponent implements OnInit {
  private authService = inject(AuthService);

  user: User | null = null;
  loading = false;
  successMessage = '';
  errorMessage = '';

  ngOnInit() {
    this.loadProfile();
  }

  loadProfile() {
    this.loading = true;
    this.authService.getProfile().subscribe({
      next: (user) => {
        this.user = user;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = 'Impossible de charger le profil.';
        this.loading = false;
      }
    });
  }

  onSubmit() {
    if (!this.user) return;

    this.loading = true;
    this.successMessage = '';
    this.errorMessage = '';

    const updateRequest = {
      firstName: this.user.firstName,
      lastName: this.user.lastName,
      phone: this.user.phone,
      address: this.user.address,
      profileImage: this.user.profileImage
    };

    this.authService.updateProfile(updateRequest).subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = 'Profil mis à jour avec succès !';
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = 'Erreur lors de la mise à jour.';
      }
    });
  }
}
