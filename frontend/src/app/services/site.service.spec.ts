import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { SiteService, CampingSite } from './site.service';
import { ApiConfigService } from './api-config.service';

describe('SiteService', () => {
  let service: SiteService;
  let httpMock: HttpTestingController;
  let apiConfigSpy: jasmine.SpyObj<ApiConfigService>;

  beforeEach(() => {
    apiConfigSpy = jasmine.createSpyObj('ApiConfigService', ['getEndpointUrl']);
    apiConfigSpy.getEndpointUrl.and.returnValue('http://localhost:8089/api/sites');

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        SiteService,
        { provide: ApiConfigService, useValue: apiConfigSpy }
      ]
    });

    service = TestBed.inject(SiteService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch all sites', () => {
    const mockSites: CampingSite[] = [
      { id: 1, name: 'Site 1', description: 'Desc 1', location: 'Loc 1', address: 'Addr 1', pricePerNight: 50, capacity: 4 }
    ];

    service.getAllSites().subscribe(sites => {
      expect(sites.length).toBe(1);
      expect(sites).toEqual(mockSites);
    });

    const req = httpMock.expectOne('http://localhost:8089/api/sites');
    expect(req.request.method).toBe('GET');
    req.flush(mockSites);
  });

  it('should create a new site', () => {
    const newSite: CampingSite = { name: 'New Site', description: 'New Desc', location: 'New Loc', address: 'New Addr', pricePerNight: 60, capacity: 2 };

    service.createSite(newSite).subscribe(site => {
      expect(site.name).toBe('New Site');
    });

    const req = httpMock.expectOne('http://localhost:8089/api/sites');
    expect(req.request.method).toBe('POST');
    req.flush({ ...newSite, id: 10 });
  });

  it('should handle error when site creation fails', () => {
    const newSite: CampingSite = { 
      name: 'Fail Site', 
      pricePerNight: 0,
      description: '',
      location: '',
      address: '',
      capacity: 0
    };

    service.createSite(newSite).subscribe({
      next: () => fail('Should have failed'),
      error: (error) => {
        expect(error.status).toBe(400);
      }
    });

    const req = httpMock.expectOne('http://localhost:8089/api/sites');
    req.flush({ message: 'Validation failed' }, { status: 400, statusText: 'Bad Request' });
  });

  it('should search sites with multiple query parameters', () => {
    service.searchSites('MOUNTAIN', 'Tunis', 10, 100).subscribe(sites => {
      expect(sites).toBeTruthy();
    });

    const req = httpMock.expectOne(request => 
      request.url.includes('/search') &&
      request.params.get('category') === 'MOUNTAIN' &&
      request.params.get('location') === 'Tunis' &&
      request.params.get('minPrice') === '10' &&
      request.params.get('maxPrice') === '100'
    );
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});
