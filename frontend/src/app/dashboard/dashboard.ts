import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { RegulationService, CFRTitle } from '../services/regulation';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class DashboardComponent implements OnInit {
  cfrTitles: CFRTitle[] = [];
  loading = true;
  generatingMockData = false;
  searchTerm = '';
  sortBy = 'number';
  sortDirection = 'asc';
  selectedAgency = 'all';
  error: string | null = null;
  agencies: string[] = [];

  // Development mode flag - set to false in production
  isDevelopmentMode = true;

  constructor(private regulationService: RegulationService, private router: Router) {}

  ngOnInit() {
    this.loadAgencies();
    this.loadCFRTitles();
  }

  loadAgencies() {
    this.agencies = this.regulationService.getUniqueAgencies();
  }

  loadCFRTitles() {
    this.loading = true;
    this.error = null;
    
    this.regulationService.getAllTitles().subscribe({
      next: (titles: CFRTitle[]) => {
        this.cfrTitles = titles;
        this.loading = false;
      },
      error: (error: any) => {
        console.error('Error loading CFR titles:', error);
        this.error = 'Failed to load CFR titles. Please ensure the backend is running on localhost:8081';
        this.loading = false;
      }
    });
  }

  get filteredTitles(): CFRTitle[] {
    let filtered = this.cfrTitles;

    // Filter by search term
    if (this.searchTerm) {
      filtered = filtered.filter(title => 
        title.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        title.agency.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        title.number.toString().includes(this.searchTerm)
      );
    }

    // Filter by agency
    if (this.selectedAgency !== 'all') {
      filtered = filtered.filter(title => title.agency === this.selectedAgency);
    }

    // Sort
    filtered.sort((a, b) => {
      let aVal = a[this.sortBy as keyof CFRTitle];
      let bVal = b[this.sortBy as keyof CFRTitle];
      
      // Handle undefined/null values
      if (aVal == null && bVal == null) return 0;
      if (aVal == null) return this.sortDirection === 'asc' ? -1 : 1;
      if (bVal == null) return this.sortDirection === 'asc' ? 1 : -1;
      
      if (typeof aVal === 'string') aVal = aVal.toLowerCase();
      if (typeof bVal === 'string') bVal = bVal.toLowerCase();
      
      if (this.sortDirection === 'asc') {
        return aVal < bVal ? -1 : aVal > bVal ? 1 : 0;
      } else {
        return aVal > bVal ? -1 : aVal < bVal ? 1 : 0;
      }
    });

    return filtered;
  }

  sortTable(column: string) {
    if (this.sortBy === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = column;
      this.sortDirection = 'asc';
    }
  }

  // =======================
  // DEVELOPMENT ONLY - Remove in production
  // =======================
  
  generateMockDataForAll() {
    if (!this.isDevelopmentMode) {
      console.warn('Mock data generation is disabled in production mode');
      return;
    }
    
    this.generatingMockData = true;
    this.error = null;
    
    this.regulationService.generateMockDataWithRelationships(5).subscribe({
      next: (response: any) => {
        console.log('✅ Generated mock data for 5 CFR titles:', response);
        this.generatingMockData = false;
        this.loadCFRTitles(); // Refresh the data
      },
      error: (error: any) => {
        console.error('❌ Error generating mock data:', error);
        this.error = 'Failed to generate mock data. Please check that the backend is running.';
        this.generatingMockData = false;
      }
    });
  }

  // =======================
  // READ-ONLY OPERATIONS (Production Ready)
  // =======================

  viewTitleDetails(titleNumber: number) {
    // Navigate to title detail view
    console.log(`Navigating to CFR Title ${titleNumber} details`);
    this.router.navigate(['/title', titleNumber]);
  }

  refreshData() {
    this.loadCFRTitles();
  }

  // Utility methods
  getTitleStatusClass(title: CFRTitle): string {
    if (title.conflictCount > 0) return 'has-conflicts';
    if (title.regulationCount > 0) return 'has-data';
    return 'no-data';
  }

  getTitleStatusText(title: CFRTitle): string {
    if (title.conflictCount > 0) return `${title.conflictCount} Conflicts`;
    if (title.regulationCount > 0) return 'No Conflicts';
    return 'No Data';
  }
}
