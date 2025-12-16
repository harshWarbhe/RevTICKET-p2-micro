import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { BookingsReportComponent } from './bookings-report.component';
import { HttpClient, HttpParams } from '@angular/common/http';
import { AlertService } from '../../../core/services/alert.service';
import { MovieService } from '../../../core/services/movie.service';
import { TheaterService } from '../../../core/services/theater.service';
import { of, throwError } from 'rxjs';
import { environment } from '../../../../environments/environment';

describe('BookingsReportComponent', () => {
  let component: BookingsReportComponent;
  let fixture: ComponentFixture<BookingsReportComponent>;
  let mockHttp: jasmine.SpyObj<HttpClient>;
  let mockAlertService: jasmine.SpyObj<AlertService>;
  let mockMovieService: jasmine.SpyObj<MovieService>;
  let mockTheaterService: jasmine.SpyObj<TheaterService>;

  const mockSummary = {
    totalBookings: 100,
    totalRevenue: 50000,
    cancelledBookings: 5,
    totalRefunds: 2500,
    todayBookings: 10,
    avgTicketPrice: 500
  };

  const mockBookings = [
    {
      id: 'booking-123',
      customerName: 'John Doe',
      customerEmail: 'john@example.com',
      customerPhone: '1234567890',
      movieTitle: 'Test Movie',
      theaterName: 'Test Theater',
      screen: 'Screen 1',
      showtime: '2024-12-25T19:00:00',
      seats: ['A1', 'A2'],
      totalAmount: 500,
      status: 'CONFIRMED',
      bookingDate: '2024-12-20T10:00:00',
      ticketNumber: 'TKT123'
    }
  ];

  const mockPageResponse = {
    content: mockBookings,
    totalElements: 1,
    totalPages: 1,
    number: 0
  };

  const mockMovies: any[] = [
    { id: 'movie1', title: 'Movie 1', description: '', genre: '', duration: 120, rating: 'PG', releaseDate: new Date(), posterUrl: '', trailerUrl: '' },
    { id: 'movie2', title: 'Movie 2', description: '', genre: '', duration: 120, rating: 'PG', releaseDate: new Date(), posterUrl: '', trailerUrl: '' }
  ];

  const mockTheaters: any[] = [
    { id: 'theater1', name: 'Theater 1', location: 'Location 1', address: 'Address 1', isActive: true },
    { id: 'theater2', name: 'Theater 2', location: 'Location 2', address: 'Address 2', isActive: true }
  ];

  beforeEach(async () => {
    mockHttp = jasmine.createSpyObj('HttpClient', ['get', 'delete']);
    mockAlertService = jasmine.createSpyObj('AlertService', ['success', 'error', 'info']);
    mockMovieService = jasmine.createSpyObj('MovieService', ['getMovies']);
    mockTheaterService = jasmine.createSpyObj('TheaterService', ['getAllTheaters']);

    await TestBed.configureTestingModule({
      imports: [BookingsReportComponent],
      providers: [
        { provide: HttpClient, useValue: mockHttp },
        { provide: AlertService, useValue: mockAlertService },
        { provide: MovieService, useValue: mockMovieService },
        { provide: TheaterService, useValue: mockTheaterService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(BookingsReportComponent);
    component = fixture.componentInstance;
  });

  describe('Component Initialization', () => {
    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should initialize with default values', () => {
      expect(component.currentPage()).toBe(0);
      expect(component.pageSize()).toBe(10);
      expect(component.loading()).toBe(false);
      expect(component.bookings()).toEqual([]);
    });
  });

  describe('Utility Functions', () => {
    it('should format currency correctly', () => {
      expect(component.formatCurrency(1000)).toBe('₹1,000');
      expect(component.formatCurrency(50000)).toBe('₹50,000');
    });

    it('should get status class', () => {
      expect(component.getStatusClass('CONFIRMED')).toBe('status-confirmed');
      expect(component.getStatusClass('CANCELLED')).toBe('status-cancelled');
    });

    it('should get screen label for UUID', () => {
      const uuid = '123e4567-e89b-12d3-a456-426614174000';
      expect(component.getScreenLabel(uuid)).toBe('Screen 1');
    });

    it('should get screen label for normal string', () => {
      expect(component.getScreenLabel('Screen 2')).toBe('Screen 2');
    });

    it('should return N/A for empty screen', () => {
      expect(component.getScreenLabel('')).toBe('N/A');
    });

    it('should format seat labels', () => {
      const seats = ['A1', 'A2', 'B1'];
      expect(component.getSeatLabels(seats)).toBe('A1, A2, B1');
    });

    it('should return N/A for empty seats', () => {
      expect(component.getSeatLabels([])).toBe('N/A');
    });

    it('should calculate start index correctly', () => {
      component.currentPage.set(0);
      component.pageSize.set(10);
      component.totalElements.set(50);
      expect(component.getStartIndex()).toBe(1);
    });

    it('should calculate end index correctly', () => {
      component.currentPage.set(0);
      component.pageSize.set(10);
      component.totalElements.set(50);
      expect(component.getEndIndex()).toBe(10);
    });

    it('should return 0 for start index when no elements', () => {
      component.totalElements.set(0);
      expect(component.getStartIndex()).toBe(0);
    });
  });
});
