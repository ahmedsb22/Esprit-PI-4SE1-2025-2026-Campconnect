import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { BookingService, Booking } from './booking.service';
import { ApiConfigService } from './api-config.service';

describe('BookingService', () => {
  let service: BookingService;
  let httpMock: HttpTestingController;
  let apiConfigSpy: jasmine.SpyObj<ApiConfigService>;

  beforeEach(() => {
    apiConfigSpy = jasmine.createSpyObj('ApiConfigService', ['getEndpointUrl']);
    apiConfigSpy.getEndpointUrl.and.returnValue('http://localhost:8089/api/bookings');

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        BookingService,
        { provide: ApiConfigService, useValue: apiConfigSpy }
      ]
    });

    service = TestBed.inject(BookingService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch all bookings', () => {
    const mockBookings: Booking[] = [
      { id: 1, startDate: '2025-06-01', endDate: '2025-06-05', guests: 2, status: 'CONFIRMED' }
    ];

    service.getAllBookings().subscribe(bookings => {
      expect(bookings.length).toBe(1);
      expect(bookings).toEqual(mockBookings);
    });

    const req = httpMock.expectOne('http://localhost:8089/api/bookings');
    expect(req.request.method).toBe('GET');
    req.flush(mockBookings);
  });

  it('should create a booking', () => {
    const newBooking: Booking = { id: 1, startDate: '2025-07-01', endDate: '2025-07-05', guests: 3 };

    service.createBooking(1, 1, '2025-07-01', '2025-07-05', 3).subscribe(booking => {
      expect(booking.id).toBe(1);
    });

    const req = httpMock.expectOne('http://localhost:8089/api/bookings');
    expect(req.request.method).toBe('POST');
    req.flush(newBooking);
  });

  it('should check availability', () => {
    service.checkAvailability(1, '2025-08-01', '2025-08-05').subscribe(res => {
      expect(res.available).toBeTrue();
    });

    const req = httpMock.expectOne(req => req.url.includes('/check-availability'));
    expect(req.request.params.get('siteId')).toBe('1');
    req.flush({ available: true });
  });
});
