import { TestBed } from '@angular/core/testing';
import { UserSimulationService, UserRole } from './user-simulation.service';

describe('UserSimulationService', () => {
  let service: UserSimulationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(UserSimulationService);
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should have default user (Marcus Rodriguez - Analyst)', () => {
    const user = service.getCurrentUser();
    expect(user).toBeTruthy();
    expect(user?.username).toBe('analyst1');
    expect(user?.role).toBe(UserRole.ANALYST);
  });

  it('should switch users', () => {
    const users = service.getAvailableUsers();
    const adminUser = users.find(u => u.role === UserRole.ADMIN);
    
    if (adminUser) {
      service.switchUser(adminUser.id);
      const current = service.getCurrentUser();
      expect(current?.id).toBe(adminUser.id);
      expect(current?.role).toBe(UserRole.ADMIN);
    }
  });

  it('should check roles correctly', () => {
    service.switchUserByUsername('admin');
    expect(service.isAdmin()).toBe(true);
    expect(service.isAnalyst()).toBe(false);

    service.switchUserByUsername('analyst1');
    expect(service.isAdmin()).toBe(false);
    expect(service.isAnalyst()).toBe(true);
  });

  it('should allow AI access for non-visitors', () => {
    service.switchUserByUsername('admin');
    expect(service.canUseAI()).toBe(true);

    service.switchUserByUsername('analyst1');
    expect(service.canUseAI()).toBe(true);

    service.switchUserByUsername('visitor');
    expect(service.canUseAI()).toBe(false);
  });

  it('should persist user to localStorage', () => {
    service.switchUserByUsername('auditor');
    
    // Create new service instance (simulates page reload)
    const newService = new UserSimulationService();
    const user = newService.getCurrentUser();
    
    expect(user?.username).toBe('auditor');
    expect(user?.role).toBe(UserRole.AUDITOR);
  });

  it('should logout correctly', () => {
    service.logout();
    expect(service.getCurrentUser()).toBeNull();
  });
});
