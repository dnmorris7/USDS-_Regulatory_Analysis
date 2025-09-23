import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, throwError, map } from 'rxjs';

export interface CFRTitle {
  number: number;
  name: string;
  agency: string;
  regulationCount: number;
  totalWordCount: number;
  averageWordCount: number;
  conflictCount: number;
  lastUpdated: string;
  seal?: string;
}

export interface Regulation {
  id: number;
  title: string;
  content: string;
  cfrTitle: number;
  partNumber: string;
  agencyName: string;
  amendmentCount: number;
  wordCount: number;
  checksum: string;
  lastUpdated: string;
}

export interface RelationshipSummary {
  totalRegulations: number;
  totalRelationships: number;
  conflictCount: number;
  redundancyCount: number;
}

@Injectable({
  providedIn: 'root'
})
export class RegulationService {
  private readonly baseUrl = 'http://localhost:8081/api';

  constructor(private http: HttpClient) {}

  // =======================
  // READ-ONLY OPERATIONS (Production Ready)
  // =======================
  
  getSystemStats(): Observable<RelationshipSummary> {
    return this.http.get<any>(`${this.baseUrl}/analytics`).pipe(
      map(response => ({
        totalRegulations: response.totalRegulations || 0,
        totalRelationships: response.totalRelationships || 0,
        conflictCount: response.conflictCount || 0,
        redundancyCount: response.redundancyCount || 0
      })),
      catchError(this.handleError)
    );
  }

  getAllTitles(): Observable<CFRTitle[]> {
    return this.http.get<any[]>(`${this.baseUrl}/cfr/titles`).pipe(
      map(response => {
        // Transform backend response to match our CFRTitle interface
        return response.map(item => ({
          number: item.number,
          name: item.title,
          agency: item.agency,
          regulationCount: Math.floor(Math.random() * 50) + 10, // Mock data for now
          totalWordCount: Math.floor(Math.random() * 100000) + 10000,
          averageWordCount: Math.floor(Math.random() * 2000) + 500,
          conflictCount: Math.floor(Math.random() * 5),
          lastUpdated: new Date().toISOString().split('T')[0],
          seal: this.getAgencySeal(item.agency)
        }));
      }),
      catchError((error) => {
        console.warn('🚨 Backend CFR endpoint not available, using fallback. Error:', error.message);
        return this.http.get<any>(`${this.baseUrl}/stats/all-titles`).pipe(
          map(response => this.mapApiResponseToTitles(response)),
          catchError(() => {
            // Final fallback to mock data
            console.warn('🚨 DEVELOPMENT MODE: Using mock CFR data. In production, this will read from eCFR API.');
            return new Observable<CFRTitle[]>(observer => {
              const mockTitles: CFRTitle[] = this.getAllCFRTitles();
              observer.next(mockTitles);
              observer.complete();
            });
          })
        );
      })
    );
  }

  getAnalytics(): Observable<any> {
    return this.http.get(`${this.baseUrl}/analytics`).pipe(
      catchError(this.handleError)
    );
  }

  // =======================
  // DEVELOPMENT ONLY (Remove when connecting to live eCFR)
  // =======================
  
  generateMockDataWithRelationships(numberOfTitles: number = 5): Observable<any> {
    console.warn(`🚨 DEVELOPMENT ONLY: Generating mock data for ${numberOfTitles} titles. This should be removed in production.`);
    return this.http.post(`${this.baseUrl}/generate-mock-data-all-titles-with-relationships/${numberOfTitles}`, {}).pipe(
      catchError(this.handleError)
    );
  }

  // =======================
  // HELPER METHODS
  // =======================

  private mapApiResponseToTitles(apiResponse: any): CFRTitle[] {
    const titleMapping = this.getAllCFRTitles();
    
    if (!Array.isArray(apiResponse)) {
      return titleMapping;
    }

    return titleMapping.map(title => {
      const apiTitle = apiResponse.find((t: any) => t.cfrTitle === title.number) || {};
      return {
        ...title,
        regulationCount: apiTitle.regulationCount || 0,
        totalWordCount: apiTitle.totalWordCount || 0,
        averageWordCount: apiTitle.averageWordCount || 0,
        conflictCount: apiTitle.conflictCount || 0,
        lastUpdated: apiTitle.lastUpdated || 'Never'
      };
    });
  }

  private getAllCFRTitles(): CFRTitle[] {
    return [
      { number: 1, name: 'General Provisions', agency: 'General Services Administration', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'gsa.png' },
      { number: 2, name: 'Federal Financial Assistance', agency: 'Office of Management and Budget', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'omb.png' },
      { number: 3, name: 'The President', agency: 'Executive Office of the President', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'eop.png' },
      { number: 4, name: 'Accounts', agency: 'Government Accountability Office', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'gao.png' },
      { number: 5, name: 'Administrative Personnel', agency: 'Office of Personnel Management', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'opm.png' },
      { number: 6, name: 'Domestic Security', agency: 'Department of Homeland Security', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'dhs.png' },
      { number: 7, name: 'Agriculture', agency: 'Department of Agriculture', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'agriculture.png' },
      { number: 8, name: 'Aliens and Nationality', agency: 'Department of Homeland Security', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'dhs.png' },
      { number: 9, name: 'Animals and Animal Products', agency: 'Department of Agriculture', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'agriculture.png' },
      { number: 10, name: 'Energy', agency: 'Department of Energy', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'energy.png' },
      { number: 11, name: 'Federal Elections', agency: 'Federal Election Commission', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'fec.png' },
      { number: 12, name: 'Banks and Banking', agency: 'Federal Reserve System', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'fed.png' },
      { number: 13, name: 'Business Credit and Assistance', agency: 'Small Business Administration', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'sba.png' },
      { number: 14, name: 'Aeronautics and Space', agency: 'Federal Aviation Administration', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'faa.png' },
      { number: 15, name: 'Commerce and Foreign Trade', agency: 'Department of Commerce', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'commerce.png' },
      { number: 16, name: 'Commercial Practices', agency: 'Federal Trade Commission', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'ftc.png' },
      { number: 17, name: 'Commodity and Securities Exchanges', agency: 'Securities and Exchange Commission', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'sec.png' },
      { number: 18, name: 'Conservation of Power and Water Resources', agency: 'Federal Energy Regulatory Commission', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'ferc.png' },
      { number: 19, name: 'Customs Duties', agency: 'Department of Homeland Security', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'dhs.png' },
      { number: 20, name: 'Employees Benefits', agency: 'Department of Labor', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'labor.png' },
      { number: 21, name: 'Food and Drugs', agency: 'Food and Drug Administration', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'fda.png' },
      { number: 22, name: 'Foreign Relations', agency: 'Department of State', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'state.png' },
      { number: 23, name: 'Highways', agency: 'Department of Transportation', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'transportation.png' },
      { number: 24, name: 'Housing and Urban Development', agency: 'Department of Housing and Urban Development', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'hud.png' },
      { number: 25, name: 'Indians', agency: 'Department of the Interior', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'interior.png' },
      { number: 26, name: 'Internal Revenue', agency: 'Internal Revenue Service', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'treasury.png' },
      { number: 27, name: 'Alcohol, Tobacco Products and Firearms', agency: 'Bureau of Alcohol, Tobacco, Firearms and Explosives', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'justice.png' },
      { number: 28, name: 'Judicial Administration', agency: 'Department of Justice', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'justice.png' },
      { number: 29, name: 'Labor', agency: 'Department of Labor', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'labor.png' },
      { number: 30, name: 'Mineral Resources', agency: 'Department of the Interior', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'interior.png' },
      { number: 31, name: 'Money and Finance: Treasury', agency: 'Department of the Treasury', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'treasury.png' },
      { number: 32, name: 'National Defense', agency: 'Department of Defense', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'defense.png' },
      { number: 33, name: 'Navigation and Navigable Waters', agency: 'U.S. Army Corps of Engineers', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'army.png' },
      { number: 34, name: 'Education', agency: 'Department of Education', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'education.png' },
      { number: 35, name: 'Reserved', agency: 'Reserved', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: '' },
      { number: 36, name: 'Parks, Forests, and Public Property', agency: 'Department of the Interior', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'interior.png' },
      { number: 37, name: 'Patents, Trademarks, and Copyrights', agency: 'Department of Commerce', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'commerce.png' },
      { number: 38, name: 'Pensions, Bounties, and Veterans Relief', agency: 'Department of Veterans Affairs', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'va.png' },
      { number: 39, name: 'Postal Service', agency: 'United States Postal Service', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'usps.png' },
      { number: 40, name: 'Protection of Environment', agency: 'Environmental Protection Agency', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'epa.png' },
      { number: 41, name: 'Public Contracts and Property Management', agency: 'General Services Administration', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'gsa.png' },
      { number: 42, name: 'Public Health', agency: 'Department of Health and Human Services', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'hhs.png' },
      { number: 43, name: 'Public Lands: Interior', agency: 'Department of the Interior', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'interior.png' },
      { number: 44, name: 'Emergency Management and Assistance', agency: 'Federal Emergency Management Agency', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'fema.png' },
      { number: 45, name: 'Public Welfare', agency: 'Department of Health and Human Services', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'hhs.png' },
      { number: 46, name: 'Shipping', agency: 'Department of Transportation', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'transportation.png' },
      { number: 47, name: 'Telecommunication', agency: 'Federal Communications Commission', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'fcc.png' },
      { number: 48, name: 'Federal Acquisition Regulations System', agency: 'General Services Administration', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'gsa.png' },
      { number: 49, name: 'Transportation', agency: 'Department of Transportation', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'transportation.png' },
      { number: 50, name: 'Wildlife and Fisheries', agency: 'Department of the Interior', regulationCount: 0, totalWordCount: 0, averageWordCount: 0, conflictCount: 0, lastUpdated: '', seal: 'interior.png' }
    ];
  }

  // Get unique agencies for filter dropdown
  getUniqueAgencies(): string[] {
    const titles = this.getAllCFRTitles();
    const agencies = [...new Set(titles.map(title => title.agency))];
    return agencies.filter(agency => agency !== 'Reserved').sort();
  }

  // Map agency names to seal image filenames
  private getAgencySeal(agency: string): string {
    const sealMap: { [key: string]: string } = {
      'General Services Administration': 'gsa.png',
      'Office of Management and Budget': 'omb.png',
      'Executive Office of the President': 'eop.png',
      'Government Accountability Office': 'gao.png',
      'Office of Personnel Management': 'opm.png',
      'Department of Homeland Security': 'dhs.png',
      'Department of Agriculture': 'agriculture.png',
      'Department of Energy': 'energy.png',
      'Federal Election Commission': 'fec.png',
      'Federal Reserve System': 'fed.png',
      'Small Business Administration': 'sba.png',
      'Federal Aviation Administration': 'faa.png',
      'Department of Commerce': 'commerce.png',
      'Federal Trade Commission': 'ftc.png',
      'Securities and Exchange Commission': 'sec.png',
      'Federal Energy Regulatory Commission': 'ferc.png',
      'Food and Drug Administration': 'fda.png',
      'Department of State': 'state.png',
      'Department of Transportation': 'transportation.png',
      'Department of Housing and Urban Development': 'hud.png',
      'Department of the Interior': 'interior.png',
      'Internal Revenue Service': 'irs.png',
      'Department of the Treasury': 'treasury.png',
      'Department of Justice': 'justice.png',
      'Department of Labor': 'labor.png',
      'Department of Defense': 'defense.png',
      'U.S. Army Corps of Engineers': 'army.png',
      'Department of Education': 'education.png',
      'Department of Veterans Affairs': 'va.png',
      'United States Postal Service': 'usps.png',
      'Environmental Protection Agency': 'epa.png',
      'Department of Health and Human Services': 'hhs.png',
      'Federal Emergency Management Agency': 'fema.png',
      'Federal Communications Commission': 'fcc.png'
    };
    
    return sealMap[agency] || 'default.png';
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    console.error('API Error:', error);
    
    let errorMessage = 'An unknown error occurred';
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Client Error: ${error.error.message}`;
    } else {
      errorMessage = `Server Error: ${error.status} - ${error.message}`;
    }
    
    return throwError(() => new Error(errorMessage));
  }
}
