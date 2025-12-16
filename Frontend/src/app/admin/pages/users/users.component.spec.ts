import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UsersComponent } from './users.component';
import { AdminUserService } from '../../../core/services/admin-user.service';
import { AlertService } from '../../../core/services/alert.service';
import { of, throwError } from 'rxjs';
import { User, UserBooking } from '../../../core/models/user.model';

describe('UsersComponent', () => {
  let component: UsersComponent;
  let fixture: ComponentFixture<UsersComponent>;
  let mockAdminUserService: jasmine.SpyObj<AdminUserService>;
  let mockAlertService: jasmine.SpyObj<AlertService>;

  const mockUsers: User[] = [
    {
      id: '1',
      name: 'John Doe',
      email: 'john@example.com',
      phone: '1234567890',
      role: 'USER',
      isActive: true,
      createdAt: new Date('2024-01-01')
    },
    {
      id: '2',
      name: 'Jane Admin',
      email: 'jane@example.com',
      phone: '0987654321',
      role: 'ADMIN',
      isActive: true,
      createdAt: new Date('2024-01-02')
    },
    {
      id: '3',
      name: 'Bob Blocked',
      email: 'bob@example.com',
      phone: '5555555555',
      role: 'USER',
      isActive: false,
      createdAt: new Date('2024-01-03')
    }
  ];

  const mockBookings: UserBooking[] = [
    {
      id: 'b1',
      movieTitle: 'Test Movie',
      theaterName: 'Test Theater',
      showDate: new Date('2024-12-25'),
      showTime: '7:00 PM',
      totalAmount: 500,
      status: 'CONFIRMED'
    }
  ];

  beforeEach(async () => {
    mockAdminUserService = jasmine.createSpyObj('AdminUserService', [
      'getAllUsers',
      'getUserBookings',
      'updateUserRole',
      'updateUserStatus',
      'deleteUser'
    ]);
    mockAlertService = jasmine.createSpyObj('AlertService', ['success', 'error']);

    await TestBed.configureTestingModule({
      imports: [UsersComponent],
      providers: [
        { provide: AdminUserService, useValue: mockAdminUserService },
        { provide: AlertService, useValue: mockAlertService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(UsersComponent);
    component = fixture.componentInstance;
  });

  describe('Component Initialization', () => {
    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should load users on init', () => {
      mockAdminUserService.getAllUsers.and.returnValue(of(mockUsers));
      
      component.ngOnInit();
      
      expect(mockAdminUserService.getAllUsers).toHaveBeenCalled();
      expect(component.users().length).toBe(3);
      expect(component.loading()).toBe(false);
    });

    it('should handle error when loading users fails', () => {
      mockAdminUserService.getAllUsers.and.returnValue(throwError(() => new Error('Load failed')));
      
      component.ngOnInit();
      
      expect(mockAlertService.error).toHaveBeenCalledWith('Failed to load users. Please try again.');
      expect(component.users().length).toBe(0);
      expect(component.loading()).toBe(false);
    });
  });

  describe('Search Functionality', () => {
    beforeEach(() => {
      mockAdminUserService.getAllUsers.and.returnValue(of(mockUsers));
      component.ngOnInit();
    });

    it('should filter users by name', () => {
      component.onSearch('john');
      
      expect(component.filteredUsers().length).toBe(1);
      expect(component.filteredUsers()[0].name).toBe('John Doe');
    });

    it('should filter users by email', () => {
      component.onSearch('jane@example.com');
      
      expect(component.filteredUsers().length).toBe(1);
      expect(component.filteredUsers()[0].email).toBe('jane@example.com');
    });

    it('should filter users by phone', () => {
      component.onSearch('5555555555');
      
      expect(component.filteredUsers().length).toBe(1);
      expect(component.filteredUsers()[0].phone).toBe('5555555555');
    });

    it('should be case insensitive', () => {
      component.onSearch('JOHN');
      
      expect(component.filteredUsers().length).toBe(1);
      expect(component.filteredUsers()[0].name).toBe('John Doe');
    });

    it('should show all users when search is cleared', () => {
      component.onSearch('john');
      component.onSearch('');
      
      expect(component.filteredUsers().length).toBe(3);
    });
  });

  describe('Role Filter', () => {
    beforeEach(() => {
      mockAdminUserService.getAllUsers.and.returnValue(of(mockUsers));
      component.ngOnInit();
    });

    it('should filter by ADMIN role', () => {
      component.onRoleFilterChange('ADMIN');
      
      expect(component.filteredUsers().length).toBe(1);
      expect(component.filteredUsers()[0].role).toBe('ADMIN');
    });

    it('should filter by USER role', () => {
      component.onRoleFilterChange('USER');
      
      expect(component.filteredUsers().length).toBe(2);
      expect(component.filteredUsers().every(u => u.role === 'USER')).toBe(true);
    });

    it('should show all users when filter is ALL', () => {
      component.onRoleFilterChange('ALL');
      
      expect(component.filteredUsers().length).toBe(3);
    });
  });

  describe('Status Filter', () => {
    beforeEach(() => {
      mockAdminUserService.getAllUsers.and.returnValue(of(mockUsers));
      component.ngOnInit();
    });

    it('should filter by ACTIVE status', () => {
      component.onStatusFilterChange('ACTIVE');
      
      expect(component.filteredUsers().length).toBe(2);
      expect(component.filteredUsers().every(u => u.isActive === true)).toBe(true);
    });

    it('should filter by BLOCKED status', () => {
      component.onStatusFilterChange('BLOCKED');
      
      expect(component.filteredUsers().length).toBe(1);
      expect(component.filteredUsers()[0].isActive).toBe(false);
    });
  });

  describe('Sorting', () => {
    beforeEach(() => {
      mockAdminUserService.getAllUsers.and.returnValue(of(mockUsers));
      component.ngOnInit();
    });

    it('should sort by name ascending', () => {
      component.onSortChange('name');
      
      const names = component.filteredUsers().map(u => u.name);
      expect(names[0]).toBe('Bob Blocked');
      expect(names[2]).toBe('John Doe');
    });

    it('should toggle sort order', () => {
      component.onSortChange('name');
      component.onSortChange('name');
      
      const names = component.filteredUsers().map(u => u.name);
      expect(names[0]).toBe('John Doe');
      expect(names[2]).toBe('Bob Blocked');
    });

    it('should sort by email', () => {
      component.onSortChange('email');
      
      const emails = component.filteredUsers().map(u => u.email);
      expect(emails[0]).toBe('bob@example.com');
    });

    it('should sort by createdAt', () => {
      component.onSortChange('createdAt');
      
      const dates = component.filteredUsers().map(u => new Date(u.createdAt).getTime());
      expect(dates[0]).toBeLessThan(dates[1]);
    });
  });

  describe('Toggle Role', () => {
    beforeEach(() => {
      mockAdminUserService.getAllUsers.and.returnValue(of(mockUsers));
      component.ngOnInit();
    });

    it('should change USER to ADMIN', () => {
      const user = mockUsers[0];
      mockAdminUserService.updateUserRole.and.returnValue(of(void 0));
      
      component.toggleRole(user);
      
      expect(mockAdminUserService.updateUserRole).toHaveBeenCalledWith('1', 'ADMIN');
      expect(mockAlertService.success).toHaveBeenCalledWith('Role updated to ADMIN');
    });

    it('should change ADMIN to USER', () => {
      const user = mockUsers[1];
      mockAdminUserService.updateUserRole.and.returnValue(of(void 0));
      
      component.toggleRole(user);
      
      expect(mockAdminUserService.updateUserRole).toHaveBeenCalledWith('2', 'USER');
      expect(mockAlertService.success).toHaveBeenCalledWith('Role updated to USER');
    });

    it('should handle error when role update fails', () => {
      const user = mockUsers[0];
      mockAdminUserService.updateUserRole.and.returnValue(throwError(() => new Error('Update failed')));
      
      component.toggleRole(user);
      
      expect(mockAlertService.error).toHaveBeenCalledWith('Failed to update user role');
    });
  });

  describe('Toggle User Status', () => {
    beforeEach(() => {
      mockAdminUserService.getAllUsers.and.returnValue(of(mockUsers));
      component.ngOnInit();
    });

    it('should block active user', () => {
      const user = mockUsers[0];
      mockAdminUserService.updateUserStatus.and.returnValue(of(void 0));
      
      component.toggleUserStatus(user);
      
      expect(mockAdminUserService.updateUserStatus).toHaveBeenCalledWith('1', 'BLOCKED');
      expect(mockAlertService.success).toHaveBeenCalledWith('User blocked successfully');
    });

    it('should activate blocked user', () => {
      const user = mockUsers[2];
      mockAdminUserService.updateUserStatus.and.returnValue(of(void 0));
      
      component.toggleUserStatus(user);
      
      expect(mockAdminUserService.updateUserStatus).toHaveBeenCalledWith('3', 'ACTIVE');
      expect(mockAlertService.success).toHaveBeenCalledWith('User activated successfully');
    });

    it('should handle error when status update fails', () => {
      const user = mockUsers[0];
      mockAdminUserService.updateUserStatus.and.returnValue(throwError(() => new Error('Update failed')));
      
      component.toggleUserStatus(user);
      
      expect(mockAlertService.error).toHaveBeenCalled();
    });
  });

  describe('Delete User', () => {
    beforeEach(() => {
      mockAdminUserService.getAllUsers.and.returnValue(of(mockUsers));
      component.ngOnInit();
      spyOn(window, 'confirm').and.returnValue(true);
    });

    it('should delete user when confirmed', () => {
      const user = mockUsers[0];
      mockAdminUserService.deleteUser.and.returnValue(of(void 0));
      
      component.deleteUser(user);
      
      expect(mockAdminUserService.deleteUser).toHaveBeenCalledWith('1');
      expect(mockAlertService.success).toHaveBeenCalled();
    });

    it('should not delete user when cancelled', () => {
      (window.confirm as jasmine.Spy).and.returnValue(false);
      const user = mockUsers[0];
      
      component.deleteUser(user);
      
      expect(mockAdminUserService.deleteUser).not.toHaveBeenCalled();
    });

    it('should handle error when delete fails', () => {
      const user = mockUsers[0];
      mockAdminUserService.deleteUser.and.returnValue(throwError(() => new Error('Delete failed')));
      
      component.deleteUser(user);
      
      expect(mockAlertService.error).toHaveBeenCalledWith('Failed to delete user. User may have active bookings.');
    });
  });

  describe('View User Modal', () => {
    beforeEach(() => {
      mockAdminUserService.getAllUsers.and.returnValue(of(mockUsers));
      component.ngOnInit();
    });

    it('should open modal and load bookings', () => {
      const user = mockUsers[0];
      mockAdminUserService.getUserBookings.and.returnValue(of(mockBookings));
      
      component.viewUser(user);
      
      expect(component.showViewModal()).toBe(true);
      expect(component.selectedUser()).toBe(user);
      expect(mockAdminUserService.getUserBookings).toHaveBeenCalledWith('1');
    });

    it('should close modal', () => {
      component.viewUser(mockUsers[0]);
      component.closeModal();
      
      expect(component.showViewModal()).toBe(false);
      expect(component.selectedUser()).toBe(null);
      expect(component.userBookings().length).toBe(0);
    });

    it('should handle error when loading bookings fails', () => {
      const user = mockUsers[0];
      mockAdminUserService.getUserBookings.and.returnValue(throwError(() => new Error('Load failed')));
      
      component.viewUser(user);
      
      expect(mockAlertService.error).toHaveBeenCalledWith('Failed to load user bookings');
    });
  });

  describe('Combined Filters', () => {
    beforeEach(() => {
      mockAdminUserService.getAllUsers.and.returnValue(of(mockUsers));
      component.ngOnInit();
    });

    it('should apply search and role filter together', () => {
      component.onSearch('john');
      component.onRoleFilterChange('USER');
      
      expect(component.filteredUsers().length).toBe(1);
      expect(component.filteredUsers()[0].name).toBe('John Doe');
    });

    it('should apply all filters together', () => {
      component.onSearch('bob');
      component.onRoleFilterChange('USER');
      component.onStatusFilterChange('BLOCKED');
      
      expect(component.filteredUsers().length).toBe(1);
      expect(component.filteredUsers()[0].name).toBe('Bob Blocked');
    });
  });
});
