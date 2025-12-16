import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { VenueManagementComponent } from './venue-management.component';
import { AlertService } from '../../../core/services/alert.service';
import { TheaterService } from '../../../core/services/theater.service';

describe('VenueManagementComponent', () => {
  let component: VenueManagementComponent;
  let fixture: ComponentFixture<VenueManagementComponent>;
  let mockAlertService: jasmine.SpyObj<AlertService>;
  let mockTheaterService: jasmine.SpyObj<TheaterService>;

  beforeEach(async () => {
    const alertSpy = jasmine.createSpyObj('AlertService', ['success', 'error']);
    const theaterSpy = jasmine.createSpyObj('TheaterService', ['getAdminTheaters', 'createTheater', 'updateTheater', 'deleteTheater']);

    await TestBed.configureTestingModule({
      imports: [
        VenueManagementComponent,
        HttpClientTestingModule,
        RouterTestingModule,
        ReactiveFormsModule,
        FormsModule
      ],
      providers: [
        { provide: AlertService, useValue: alertSpy },
        { provide: TheaterService, useValue: theaterSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(VenueManagementComponent);
    component = fixture.componentInstance;
    mockAlertService = TestBed.inject(AlertService) as jasmine.SpyObj<AlertService>;
    mockTheaterService = TestBed.inject(TheaterService) as jasmine.SpyObj<TheaterService>;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default values', () => {
    expect(component.theatres()).toEqual([]);
    expect(component.loading()).toBeFalse();
    expect(component.showTheatreForm()).toBeFalse();
    expect(component.searchTerm()).toBe('');
  });

  it('should open theatre form when addTheatre is called', () => {
    component.addTheatre();
    
    expect(component.showTheatreForm()).toBeTrue();
    expect(component.editingTheatreId()).toBeNull();
    expect(component.theatreForm.get('name')?.value).toBe('');
    expect(component.theatreForm.get('isActive')?.value).toBeTrue();
  });

  it('should validate required fields in theatre form', () => {
    component.addTheatre();
    component.submitTheatre();
    
    expect(component.theatreForm.invalid).toBeTrue();
    expect(mockAlertService.error).toHaveBeenCalledWith('Please fill all required fields correctly');
  });

  it('should filter theatres based on search term', () => {
    const mockTheatres = [
      { id: '1', name: 'PVR Cinema', location: 'Mumbai', address: 'Test Address 1', isActive: true },
      { id: '2', name: 'INOX Theatre', location: 'Delhi', address: 'Test Address 2', isActive: true }
    ];
    
    component.theatres.set(mockTheatres);
    component.searchTerm.set('PVR');
    component.applyFilters();
    
    expect(component.filteredTheatres().length).toBe(1);
    expect(component.filteredTheatres()[0].name).toBe('PVR Cinema');
  });

  it('should toggle expanded theatre view', () => {
    const theatreId = 'theatre-1';
    
    component.toggleScreensView(theatreId);
    expect(component.expandedTheatreId()).toBe(theatreId);
    
    component.toggleScreensView(theatreId);
    expect(component.expandedTheatreId()).toBeNull();
  });

  it('should open screen configuration', () => {
    const theatreId = 'theatre-1';
    const screenId = 'screen-1';
    
    component.openScreenConfig(theatreId, screenId);
    
    expect(component.configTheatreId()).toBe(theatreId);
    expect(component.configScreenId()).toBe(screenId);
  });

  it('should close screen configuration', () => {
    component.configTheatreId.set('theatre-1');
    component.configScreenId.set('screen-1');
    
    component.closeScreenConfig();
    
    expect(component.configTheatreId()).toBeNull();
    expect(component.configScreenId()).toBeNull();
  });

  it('should extract unique cities from theatres', () => {
    const mockTheatres = [
      { id: '1', name: 'Theatre 1', location: 'Mumbai', address: 'Address 1', isActive: true },
      { id: '2', name: 'Theatre 2', location: 'Delhi', address: 'Address 2', isActive: true },
      { id: '3', name: 'Theatre 3', location: 'Mumbai', address: 'Address 3', isActive: true }
    ];
    
    component['extractCities'](mockTheatres);
    
    expect(component.cities()).toEqual(['Delhi', 'Mumbai']);
  });

  it('should validate image URL format', () => {
    expect(component.isImage('test.jpg')).toBeTrue();
    expect(component.isImage('test.png')).toBeTrue();
    expect(component.isImage('data:image/jpeg;base64,abc')).toBeTrue();
    expect(component.isImage('test.txt')).toBeFalse();
  });
});