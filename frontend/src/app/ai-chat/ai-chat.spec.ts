import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AIChatComponent } from './ai-chat';
import { UserSimulationService } from '../services/user-simulation.service';

describe('AIChatComponent', () => {
  let component: AIChatComponent;
  let fixture: ComponentFixture<AIChatComponent>;
  let userService: UserSimulationService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AIChatComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(AIChatComponent);
    component = fixture.componentInstance;
    userService = TestBed.inject(UserSimulationService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load user on init', () => {
    expect(component.currentUser).toBeTruthy();
  });

  it('should create new conversation', () => {
    const initialCount = component.conversations.length;
    component.newConversation();
    expect(component.conversations.length).toBe(initialCount + 1);
    expect(component.activeConversation).toBeTruthy();
  });

  it('should not allow visitors to use AI', () => {
    userService.switchUserByUsername('visitor');
    expect(component.userSimulationService.canUseAI()).toBe(false);
  });

  it('should allow analysts to use AI', () => {
    userService.switchUserByUsername('analyst1');
    expect(component.userSimulationService.canUseAI()).toBe(true);
  });
});
