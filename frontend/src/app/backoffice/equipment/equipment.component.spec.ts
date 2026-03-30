import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { EquipmentComponent } from './equipment.component';
import { EquipmentService } from '../../services/equipment.service';
import { AuthService } from '../../services/auth.service';
import { of } from 'rxjs';

describe('EquipmentComponent', () => {
  let component: EquipmentComponent;
  let fixture: ComponentFixture<EquipmentComponent>;
  let equipmentServiceSpy: jasmine.SpyObj<EquipmentService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    equipmentServiceSpy = jasmine.createSpyObj('EquipmentService', ['getAllEquipment', 'deleteEquipment', 'updateEquipment', 'createEquipment']);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['hasRole', 'logout'], {
      currentUser$: of({ id: 1, firstName: 'Admin', lastName: 'User', roles: ['ADMIN'] })
    });

    equipmentServiceSpy.getAllEquipment.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, EquipmentComponent],
      providers: [
        { provide: EquipmentService, useValue: equipmentServiceSpy },
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

    fixture = TestBed.createComponent(EquipmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load equipment on init', () => {
    expect(equipmentServiceSpy.getAllEquipment).toHaveBeenCalled();
  });

  it('should call deleteEquipment when deleteItem is called', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    equipmentServiceSpy.deleteEquipment.and.returnValue(of({ message: 'Deleted' }));
    
    component.deleteItem(1);
    
    expect(equipmentServiceSpy.deleteEquipment).toHaveBeenCalledWith(1);
  });
});
