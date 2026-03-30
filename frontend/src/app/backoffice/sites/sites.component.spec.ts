import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { SitesComponent } from './sites.component';
import { SiteService } from '../../services/site.service';
import { AuthService } from '../../services/auth.service';
import { of } from 'rxjs';

describe('SitesComponent', () => {
  let component: SitesComponent;
  let fixture: ComponentFixture<SitesComponent>;
  let siteServiceSpy: jasmine.SpyObj<SiteService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    siteServiceSpy = jasmine.createSpyObj('SiteService', ['getAllSites', 'deleteSite', 'updateSite', 'getSiteById']);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['hasRole', 'logout'], {
      currentUser$: of({ id: 1, firstName: 'Admin', lastName: 'User', roles: ['ADMIN'] })
    });

    siteServiceSpy.getAllSites.and.returnValue(of([]));
    siteServiceSpy.getSiteById.and.returnValue(of({ id: 1, name: 'Test Site' } as any));

    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, SitesComponent],
      providers: [
        { provide: SiteService, useValue: siteServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({}),
            snapshot: { paramMap: { get: () => null } }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SitesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load sites on init', () => {
    expect(siteServiceSpy.getAllSites).toHaveBeenCalled();
  });

  it('should call deleteSite when deleteSite is called', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    siteServiceSpy.deleteSite.and.returnValue(of(undefined));
    
    component.deleteSite(1);
    
    expect(siteServiceSpy.deleteSite).toHaveBeenCalledWith(1);
  });
});
