import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { finalize } from 'rxjs';

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
  summary!: DashboardSummary;

  cards: { label: string; value: number; className: string }[] = [];

  constructor(
    private dashboardService: DashboardService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadSummary();
  }

  loadSummary(): void {
    this.loading = true;

    this.dashboardService.getSummary()
      .pipe(finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: (data: DashboardSummary) => {
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
          this.cdr.detectChanges();
        },
        error: (err: unknown) => {
          console.error('Error cargando dashboard', err);
        },
      });
  }
}