import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { DashboardComponent } from './dashboard.component';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { environment } from '../../../environments/environment';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardComponent, HttpClientTestingModule, RouterTestingModule],
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    
    // Mock Chart
    (window as any).Chart = class {
      constructor() {}
    };
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load stats on init', () => {
    const mockStats = {
      totalSites: 5,
      totalUsers: 10,
      totalBookings: 3,
      totalOrders: 2,
      revenueStats: { totalRevenue: 5000 }
    };

    fixture.detectChanges(); // ngOnInit

    const req = httpMock.expectOne(`${environment.apiUrl}/analytics/dashboard`);
    expect(req.request.method).toBe('GET');
    req.flush(mockStats);

    expect(component.stats.totalSites).toBe(5);
    expect(component.stats.totalUsers).toBe(10);
    expect(component.stats.totalOrders).toBe(5); // 3 bookings + 2 orders
    expect(component.stats.revenue).toBe(5000);
  });

  it('should handle stats loading error', () => {
    fixture.detectChanges();
    
    const req = httpMock.expectOne(`${environment.apiUrl}/analytics/dashboard`);
    req.error(new ErrorEvent('Network error'));
    
    expect(component.stats.totalSites).toBe(0);
  });
});
