import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';

export const routes: Routes = [
  { 
    path: '', 
    component: HomeComponent,
    title: 'USDS Regulatory Analysis - Home'
  },
  { 
    path: 'dashboard', 
    loadComponent: () => import('./dashboard/dashboard').then(m => m.DashboardComponent),
    title: 'CFR Titles Dashboard - USDS Regulatory Analysis'
  },
  { 
    path: 'title/:id', 
    loadComponent: () => import('./title-detail/title-detail').then(m => m.TitleDetailComponent),
    title: 'CFR Title Details - USDS Regulatory Analysis'
  },
  { 
    path: 'about', 
    loadComponent: () => import('./analytics/analytics').then(m => m.AnalyticsComponent),
    title: 'About - USDS Regulatory Analysis'
  },
  { 
    path: '**', 
    redirectTo: '',
    pathMatch: 'full'
  }
];
