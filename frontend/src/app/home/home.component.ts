import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { RoleSwitcherComponent } from '../role-switcher/role-switcher';
import { AuthService } from '../services/auth.service';

interface GenerationStats {
  totalRegulations: number;
  totalRelationships: number;
  cfr_titles_covered: number;
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RoleSwitcherComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {
  isLoading = false;
  showResults = false;
  hasError = false;
  resultMessage = '';
  generationStats: GenerationStats | null = null;
  
  // Role-based permissions
  currentRole: string = 'VISITOR';
  canGenerate: boolean = false;

  private readonly API_BASE_URL = 'http://localhost:8081/api';

  constructor(
    private router: Router, 
    private http: HttpClient,
    private authService: AuthService
  ) {
    // Initialize role and permissions
    this.updateRolePermissions();
    
    // Subscribe to role changes
    this.authService.onRoleChange((newRole: string) => {
      this.currentRole = newRole;
      this.updateRolePermissions();
    });
  }

  updateRolePermissions() {
    this.currentRole = this.authService.getCurrentRole();
    this.canGenerate = this.authService.canGenerate();
  }

  async generateMockData(): Promise<void> {
    if (!this.canGenerate) {
      console.warn('Mock data generation is not permitted for current role');
      return;
    }
    
    this.isLoading = true;
    this.showResults = false;
    this.hasError = false;
    this.resultMessage = '';
    this.generationStats = null;

    try {
      const response = await this.http.post<any>(
        `${this.API_BASE_URL}/generate-mock-data-all-titles-with-relationships/5`,
        {}
      ).toPromise();

      this.showResults = true;
      this.hasError = false;
      this.resultMessage = 'Successfully generated mock data with relationships across all CFR titles!';
      
      // Parse the response to extract statistics
      if (response) {
        this.generationStats = {
          totalRegulations: response.totalRegulations || 100,
          totalRelationships: response.totalRelationships || 110,
          cfr_titles_covered: response.cfr_titles_covered || 50
        };
      }
    } catch (error) {
      console.error('Error generating mock data:', error);
      this.showResults = true;
      this.hasError = true;
      this.resultMessage = 'Error generating mock data. Please ensure the backend server is running on localhost:8081.';
    } finally {
      this.isLoading = false;
    }
  }

  viewAnalytics(): void {
    this.router.navigate(['/analytics']);
  }
}