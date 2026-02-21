import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { SiteService, CampingSite } from '../../services/site.service';
import { CampingSiteView } from '../../models/camping-site.model';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-camping-sites',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './camping-sites.component.html',
  styleUrls: ['./camping-sites.component.scss']
})
export class CampingSitesComponent implements OnInit, AfterViewInit {
  sites: CampingSiteView[] = [];
  loading = true;
  viewMode: 'grid' | 'list' = 'grid';

  filters = {
    location: '',
    priceRange: '',
    siteType: '',
    features: ''
  };

  sampleSites: CampingSiteView[] = [
    {
      id: 1,
      name: 'Mountain View Campsite',
      location: 'Ain Draham, North Tunisia',
      description: 'Nestled in the mountains with breathtaking views. Perfect for hiking and nature lovers.',
      price: 75,
      rating: 4.8,
      reviews: 127,
      image: 'assets/img/placeholder.svg',
      features: ['Electricity', 'Water', 'Fire Pit']
    },
    {
      id: 2,
      name: 'Coastal Paradise',
      location: 'Hammamet, Coastal',
      description: 'Beach camping at its finest. Wake up to the sound of waves and stunning sunrises.',
      price: 120,
      rating: 4.9,
      reviews: 203,
      image: 'assets/img/placeholder.svg',
      features: ['Electricity', 'WiFi', 'Store']
    },
    {
      id: 3,
      name: 'Desert Oasis',
      location: 'Tozeur, South Tunisia',
      description: 'Experience the magic of the Sahara. Star gazing and traditional Bedouin experiences.',
      price: 150,
      rating: 4.95,
      reviews: 156,
      image: 'assets/img/placeholder.svg',
      features: ['Star Gazing', 'Campfire', 'Meals']
    },
    {
      id: 4,
      name: 'Forest Retreat',
      location: 'Tabarka, North Tunisia',
      description: 'Peaceful forest setting perfect for families. Close to hiking trails and natural springs.',
      price: 65,
      rating: 4.7,
      reviews: 89,
      image: 'assets/img/placeholder.svg',
      features: ['Electricity', 'Water', 'Trails']
    },
    {
      id: 5,
      name: 'Lakeside Camping',
      location: 'Ichkeul, North Tunisia',
      description: 'Camping by the lake with opportunities for fishing, kayaking, and bird watching.',
      price: 85,
      rating: 4.6,
      reviews: 74,
      image: 'assets/img/placeholder.svg',
      features: ['Fishing', 'Kayaking', 'Water']
    },
    {
      id: 6,
      name: 'Glamping Luxury',
      location: 'Sidi Bou Said, Coastal',
      description: 'Luxury glamping tents with all amenities. Perfect for a comfortable outdoor experience.',
      price: 250,
      rating: 5.0,
      reviews: 45,
      image: 'assets/img/placeholder.svg',
      features: ['Electricity', 'WiFi', 'AC']
    }
  ];

  bookingApiUrl = `${environment.apiUrl}/bookings`;

  constructor(
    private siteService: SiteService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.loadSites();
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.initFrontofficeScripts();
    }, 100);
  }

  loadSites(): void {
    this.loading = true;
    this.siteService.getActiveSites().subscribe({
      next: (sites: CampingSite[]) => {
        this.sites = sites.length > 0
          ? sites.map((site: CampingSite) => this.mapSite(site))
          : this.sampleSites;
        this.loading = false;
      },
      error: (err: HttpErrorResponse) => {
        console.error('Error loading sites:', err);
        this.sites = this.sampleSites;
        this.loading = false;
      }
    });
  }

  searchSites(): void {
    const priceMin = this.filters.priceRange
      ? Number(this.filters.priceRange.split('-')[0])
      : undefined;
    const priceMax = this.filters.priceRange
      ? Number(this.filters.priceRange.split('-')[1])
      : undefined;

    this.loading = true;
    this.siteService
      .searchSites(undefined, this.filters.location || undefined, priceMin, priceMax)
      .subscribe({
        next: (sites: CampingSite[]) => {
          this.sites = sites.map((site: CampingSite) => this.mapSite(site));
          this.loading = false;
        },
        error: (err: HttpErrorResponse) => {
          console.error('Error searching sites:', err);
          this.sites = [];
          this.loading = false;
        }
      });
  }

  resetFilters(): void {
    this.filters = { location: '', priceRange: '', siteType: '', features: '' };
    this.loadSites();
  }

  setViewMode(mode: 'grid' | 'list'): void {
    this.viewMode = mode;
  }

  /** Book a site — no auth required (anonymous booking) */
  bookSite(siteId: number): void {
    const startDate = new Date();
    startDate.setDate(startDate.getDate() + 7);
    const endDate = new Date();
    endDate.setDate(endDate.getDate() + 9);

    // Payload matches Reservation JPA entity structure
    const payload = {
      campingSite: { id: siteId },
      checkInDate: startDate.toISOString().split('T')[0],
      checkOutDate: endDate.toISOString().split('T')[0],
      numberOfGuests: 2,
      status: 'PENDING'
    };

    this.http.post(this.bookingApiUrl, payload).subscribe({
      next: () => alert('✅ Booking created successfully! View it in the backoffice under Bookings.'),
      error: (err: HttpErrorResponse) => {
        console.error('Booking failed', err);
        alert(err.error?.message || 'Booking failed. Please try again.');
      }
    });
  }

  scrollToTop(): void {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  subscribeNewsletter(): void {
    alert('Newsletter subscription — coming soon!');
  }

  private mapSite(site: CampingSite): CampingSiteView {
    const image = site?.imageUrl ?? site?.images?.[0]?.url ?? 'assets/img/placeholder.svg';
    const features: string[] = [];
    if (site.hasElectricity) features.push('Electricity');
    if (site.hasWifi)        features.push('WiFi');
    if (site.hasParking)     features.push('Parking');
    if (site.hasRestrooms)   features.push('Restrooms');
    if (site.hasShowers)     features.push('Showers');
    if (site.hasPetFriendly) features.push('Pet Friendly');
    return {
      id: site.id ?? 0,
      name: site.name,
      location: site.location,
      description: site.description,
      price: site.pricePerNight ?? 0,
      rating: site.rating ?? 4.5,
      reviews: site.reviewCount ?? 0,
      image,
      features: features.length ? features : ['Nature', 'Fresh Air', 'Relaxation']
    };
  }

  initFrontofficeScripts(): void {
    if ((window as any)['AOS']) {
      (window as any)['AOS'].init({ duration: 800, easing: 'ease-in-out', once: true });
    }
  }
}
