import { Component, OnInit, AfterViewInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { SiteService } from '../../services/site.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit, AfterViewInit {
  public authService = inject(AuthService);
  private siteService = inject(SiteService);

  sites: any[] = [];
  stats = {
    sites: 50,
    campers: 2500,
    rating: 4.8,
    equipment: 200
  };

  ngOnInit() {
    this.loadSites();
  }

  ngAfterViewInit() {
    // Initialize AOS and other scripts after view init (once)
    setTimeout(() => {
      if (typeof window !== 'undefined') {
        if ((window as any).AOS) {
          (window as any).AOS.refresh();
        }
        // Initialize other scripts safely
        this.initScripts();
      }
    }, 100);
  }

  scrollTo(elementId: string) {
    const element = document.getElementById(elementId);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }

  scrollToTop() {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  initScripts() {
    // Initialize mobile nav toggle (avoid duplicate listeners)
    const mobileNavToggle = document.querySelector('.mobile-nav-toggle') as HTMLElement | null;
    const navmenu = document.querySelector('#navmenu');

    if (mobileNavToggle && navmenu && !mobileNavToggle.dataset['bound']) {
      mobileNavToggle.dataset['bound'] = 'true';
      mobileNavToggle.addEventListener('click', () => {
        navmenu.classList.toggle('mobile-nav-active');
      });
    }

    // Initialize scroll to top (avoid duplicate listeners)
    const scrollTop = document.getElementById('scroll-top');
    if (scrollTop && !scrollTop.dataset['bound']) {
      scrollTop.dataset['bound'] = 'true';
      const onScroll = () => {
        if (window.scrollY > 100) {
          scrollTop.classList.add('active');
        } else {
          scrollTop.classList.remove('active');
        }
      };
      window.addEventListener('scroll', onScroll, { passive: true });
      onScroll();
      scrollTop.addEventListener('click', (e) => {
        e.preventDefault();
        window.scrollTo({ top: 0, behavior: 'smooth' });
      });
    }
  }

  contactSubmitted = false;

  onContactSubmit(event: Event) {
    event.preventDefault();
    this.contactSubmitted = true;
  }

  loadSites() {
    this.siteService.getActiveSites().subscribe({
      next: (sites) => {
        this.sites = sites.map(site => this.mapSite(site)).slice(0, 6);
      },
      error: (err) => {
        console.error('Error loading sites:', err);
      }
    });
  }

  private mapSite(site: any) {
    return {
      id: site.id,
      name: site.name,
      location: site.location,
      description: site.description,
      price: site.pricePerNight ?? site.price ?? 0,
      rating: site.rating ?? 4.5,
      image: site.imageUrl ?? site?.images?.[0]?.url ?? 'assets/img/placeholder.svg'
    };
  }
}
