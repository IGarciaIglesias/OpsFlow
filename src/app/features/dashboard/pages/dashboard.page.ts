import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { finalize } from 'rxjs';
import { provideRouter } from '@angular/router';
import { HeaderComponent } from '../../../core/layout/header.component';
import { DashboardService } from '../services/dashboard.service';
import { DashboardSummary } from '../models/dashboard-summary.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, HeaderComponent],
  templateUrl: './dashboard.page.html',
  styleUrls: ['./dashboard.page.css'],
})
export class DashboardPage implements OnInit {
  loading = false;

  summary: DashboardSummary = {
    totalRequests: 0,
    draft: 0,
    pendingValidation: 0,
    validated: 0,
    approved: 0,
    inProgress: 0,
    completed: 0,
    failed: 0,
    rejected: 0,
    cancelled: 0,
  };

  cards: { label: string; value: number; className: string }[] = [];

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.loadSummary();
  }

  loadSummary(): void {
    this.loading = true;

    this.dashboardService.getSummary()
      .pipe(finalize(() => {
        this.loading = false;
      }))
      .subscribe({
        next: (raw: any) => {

          const data: DashboardSummary = {
            totalRequests: raw.total ?? 0,
            draft: raw.draft ?? 0,
            pendingValidation: raw.pending ?? 0,
            validated: raw.validated ?? 0,
            approved: raw.approved ?? 0,
            inProgress: raw.inProgress ?? 0,
            completed: raw.completed ?? 0,
            failed: raw.failed ?? 0,
            rejected: raw.rejected ?? 0,
            cancelled: raw.cancelled ?? 0,
          };

          this.summary = data;
          this.cards = [
            { label: 'Total', value: data.totalRequests, className: 'total' },
            { label: 'Draft', value: data.draft, className: 'draft' },
            { label: 'Pending validation', value: data.pendingValidation, className: 'pending' },
            { label: 'Validated', value: data.validated, className: 'validated' },
            { label: 'Approved', value: data.approved, className: 'approved' },
            { label: 'In progress', value: data.inProgress, className: 'progress' },
            { label: 'Completed', value: data.completed, className: 'completed' },
            { label: 'Failed', value: data.failed, className: 'failed' },
            { label: 'Rejected', value: data.rejected, className: 'rejected' },
            { label: 'Cancelled', value: data.cancelled, className: 'cancelled' },
          ];
        },
        error: (err: unknown) => {
          console.error('Error cargando dashboard', err);
          this.cards = [];
        },
      });
  }
}