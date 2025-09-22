import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { RegulationService, CFRTitle } from '../services/regulation';

export interface TitleDetailMetrics {
  wordCount: {
    total: number;
    vsAverage: number;
    percentageDifference: number;
  };
  historicalChanges: {
    count: number;
    period: string;
    status: 'Stable' | 'Growing' | 'Declining';
  };
  redundancyScore: {
    score: number;
    maxScore: number;
    level: 'Low' | 'Medium' | 'High';
  };
  deregulationPotential: {
    opportunities: number;
    redundantSections: number;
    conflicts: number;
  };
}

export interface HistoricalChange {
  date: string;
  type: 'Addition' | 'Modification' | 'Removal';
  wordImpact: number;
  description: string;
}

export interface AgencyComparison {
  regulatoryBurdenRank: number;
  totalTitles: number;
  wordCountPercentile: number;
  changeFrequency: string;
}

@Component({
  selector: 'app-title-detail',
  imports: [CommonModule, RouterModule],
  templateUrl: './title-detail.html',
  styleUrl: './title-detail.css'
})
export class TitleDetailComponent implements OnInit {
  titleNumber: number = 1;
  cfrTitle: CFRTitle | null = null;
  metrics: TitleDetailMetrics | null = null;
  historicalChanges: HistoricalChange[] = [];
  agencyComparison: AgencyComparison | null = null;
  
  loading = true;
  error: string | null = null;
  activeTab = 'historical-trends';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private regulationService: RegulationService
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.titleNumber = +params['id'];
      this.loadTitleDetails();
    });
  }

  loadTitleDetails() {
    this.loading = true;
    this.error = null;

    // Load the basic CFR title information
    this.regulationService.getAllTitles().subscribe({
      next: (titles) => {
        this.cfrTitle = titles.find(t => t.number === this.titleNumber) || null;
        if (this.cfrTitle) {
          this.loadDetailedMetrics();
        } else {
          this.error = `CFR Title ${this.titleNumber} not found`;
          this.loading = false;
        }
      },
      error: (error) => {
        this.error = 'Failed to load title details';
        this.loading = false;
      }
    });
  }

  loadDetailedMetrics() {
    // Generate mock detailed metrics based on the title
    // In production, this would call backend APIs for real relationship data
    this.metrics = this.generateMockMetrics();
    this.historicalChanges = this.generateMockHistoricalChanges();
    this.agencyComparison = this.generateMockAgencyComparison();
    this.loading = false;
  }

  private generateMockMetrics(): TitleDetailMetrics {
    const baseWordCount = 30000 + (this.titleNumber * 1200);
    const avgWordCount = 42000;
    
    return {
      wordCount: {
        total: baseWordCount,
        vsAverage: avgWordCount,
        percentageDifference: Number(((baseWordCount - avgWordCount) / avgWordCount * 100).toFixed(1))
      },
      historicalChanges: {
        count: Math.floor(Math.random() * 10) + 1,
        period: 'Last 2 Years',
        status: this.titleNumber % 3 === 0 ? 'Growing' : this.titleNumber % 2 === 0 ? 'Declining' : 'Stable'
      },
      redundancyScore: {
        score: Math.floor(Math.random() * 8) + 1,
        maxScore: 10,
        level: this.titleNumber % 4 === 0 ? 'High' : this.titleNumber % 3 === 0 ? 'Medium' : 'Low'
      },
      deregulationPotential: {
        opportunities: Math.floor(Math.random() * 5) + 1,
        redundantSections: Math.floor(Math.random() * 15) + 3,
        conflicts: Math.floor(Math.random() * 10) + 2
      }
    };
  }

  private generateMockHistoricalChanges(): HistoricalChange[] {
    const changes: HistoricalChange[] = [];
    const types: Array<'Addition' | 'Modification' | 'Removal'> = ['Addition', 'Modification', 'Removal'];
    const descriptions = [
      'Removed outdated technology references',
      'Streamlined administrative procedures', 
      'Clarified regulatory definitions',
      'Enhanced enforcement mechanisms',
      'Added new safety protocols',
      'Updated environmental compliance standards'
    ];

    for (let i = 0; i < 7; i++) {
      changes.push({
        date: new Date(2022 + Math.floor(i/3), Math.floor(Math.random() * 12), Math.floor(Math.random() * 28) + 1).toLocaleDateString(),
        type: types[Math.floor(Math.random() * types.length)],
        wordImpact: (Math.random() - 0.5) * 2000,
        description: descriptions[Math.floor(Math.random() * descriptions.length)]
      });
    }

    return changes.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
  }

  private generateMockAgencyComparison(): AgencyComparison {
    return {
      regulatoryBurdenRank: Math.floor(Math.random() * 50) + 1,
      totalTitles: 50,
      wordCountPercentile: Math.floor(Math.random() * 100) + 1,
      changeFrequency: this.titleNumber % 3 === 0 ? 'High' : this.titleNumber % 2 === 0 ? 'Medium' : 'Low'
    };
  }

  goBack() {
    this.router.navigate(['/dashboard']);
  }

  setActiveTab(tab: string) {
    this.activeTab = tab;
  }

  getWordImpactClass(impact: number): string {
    return impact > 0 ? 'positive' : 'negative';
  }

  getChangeTypeClass(type: string): string {
    return type.toLowerCase();
  }

  getRedundancyBarWidth(): number {
    if (!this.metrics) return 0;
    return (this.metrics.redundancyScore.score / this.metrics.redundancyScore.maxScore) * 100;
  }

  getRedundancyColorClass(): string {
    if (!this.metrics) return '';
    const level = this.metrics.redundancyScore.level;
    return level === 'Low' ? 'low-redundancy' : level === 'Medium' ? 'medium-redundancy' : 'high-redundancy';
  }
}
