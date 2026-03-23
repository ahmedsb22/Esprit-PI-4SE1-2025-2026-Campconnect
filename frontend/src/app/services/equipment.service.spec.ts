import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { EquipmentService, Equipment } from './equipment.service';
import { ApiConfigService } from './api-config.service';

describe('EquipmentService', () => {
  let service: EquipmentService;
  let httpMock: HttpTestingController;
  let apiConfigSpy: jasmine.SpyObj<ApiConfigService>;

  beforeEach(() => {
    apiConfigSpy = jasmine.createSpyObj('ApiConfigService', ['getEndpointUrl']);
    apiConfigSpy.getEndpointUrl.and.returnValue('http://localhost:8089/api/equipment');

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        EquipmentService,
        { provide: ApiConfigService, useValue: apiConfigSpy }
      ]
    });

    service = TestBed.inject(EquipmentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch all equipment', () => {
    const mockEquipment: Equipment[] = [
      { id: 1, name: 'Tent', category: 'SHELTER', price: 20, stock: 10 }
    ];

    service.getAllEquipment().subscribe(equipment => {
      expect(equipment.length).toBe(1);
      expect(equipment).toEqual(mockEquipment);
    });

    const req = httpMock.expectOne('http://localhost:8089/api/equipment');
    expect(req.request.method).toBe('GET');
    req.flush(mockEquipment);
  });

  it('should create equipment', () => {
    const newEquipment: Equipment = { name: 'Grill', category: 'COOKING', price: 15, stock: 5 };

    service.createEquipment(newEquipment).subscribe(equipment => {
      expect(equipment.name).toBe('Grill');
    });

    const req = httpMock.expectOne('http://localhost:8089/api/equipment');
    expect(req.request.method).toBe('POST');
    req.flush({ ...newEquipment, id: 10 });
  });

  it('should update stock', () => {
    service.updateStock(1, 20).subscribe(res => {
      expect(res.message).toBe('Stock updated');
    });

    const req = httpMock.expectOne(r => r.url.includes('/1/stock') && r.url.includes('quantity=20'));
    expect(req.request.method).toBe('PUT');
    req.flush({ message: 'Stock updated' });
  });
});
