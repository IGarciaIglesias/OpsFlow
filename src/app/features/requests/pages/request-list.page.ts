import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { finalize, interval, Subscription } from 'rxjs';

import { getUserRole } from '../../../core/utils/auth.utils';
import { RequestService } from '../services/request.service';
import { Request } from '../models/request.model';
import { HeaderComponent } from '../../../core/layout/header.component';
import { RequestStatus } from '../models/request-status.model';

@Component({
  selector: 'app-request-list',
  standalone: true,
  templateUrl: './request-list.page.html',
  styleUrls: ['./request-list.page.css'],
  imports: [CommonModule, RouterModule, HeaderComponent],
})
export class RequestListPage implements OnInit, OnDestroy {
  role: string | null = null;
  requests: Request[] = [];

  loading = false;
  actionBusyId: number | null = null;

  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;
  selectedStatus: RequestStatus | '' = '';

  readonly availableStatuses: RequestStatus[] = [
    'DRAFT',
    'PENDING_VALIDATION',
    'VALIDATED',
    'REJECTED',
    'APPROVED',
    'IN_PROGRESS',
    'COMPLETED',
    'FAILED',
    'CANCELLED',
  ];

  private pollSub!: Subscription;

  constructor(
    private requestService: RequestService,
    private cdr: ChangeDetectorRef,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.role = getUserRole();
    this.reload();

    this.pollSub = interval(2000).subscribe(() => {
      this.reloadSilently();
    });
  }

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
  }

  canCreate(): boolean {
    return ['ADMIN', 'MANAGER', 'OPERATOR'].includes(this.role ?? '');
  }

  canApprove(): boolean {
    return ['ADMIN', 'MANAGER'].includes(this.role ?? '');
  }

  goToCreate(): void {
    this.router.navigate(['/requests/new']);
  }

  onStatusChange(value: string): void {
    this.selectedStatus = value as RequestStatus | '';
    this.page = 0;
    this.reload();
  }

  clearFilters(): void {
    this.selectedStatus = '';
    this.page = 0;
    this.reload();
  }

  reload(): void {
    this.loading = true;

    this.requestService.getAll(
      this.page,
      this.size,
      this.selectedStatus || undefined
    )
      .pipe(finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: data => {
          this.requests = data.content;
          this.totalPages = data.totalPages;
          this.totalElements = data.totalElements;
          this.cdr.detectChanges();
        },
        error: err => console.error('Error cargando requests', err),
      });
  }

  private reloadSilently(): void {
    this.requestService.getAll(
      this.page,
      this.size,
      this.selectedStatus || undefined
    ).subscribe({
      next: data => {
        this.requests = data.content;
        this.totalPages = data.totalPages;
        this.totalElements = data.totalElements;
        this.cdr.detectChanges();
      },
      error: () => {}
    });
  }

  submit(id: number): void {
    this.actionBusyId = id;

    this.requestService.submit(id)
      .pipe(finalize(() => {
        this.actionBusyId = null;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: () => this.reload(),
        error: err => console.error('Error enviando request', err),
      });
  }

  approve(id: number): void {
    this.actionBusyId = id;

    this.requestService.approve(id)
      .pipe(finalize(() => {
        this.actionBusyId = null;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: () => this.reload(),
        error: err => console.error('Error aprobando', err),
      });
  }

  reject(id: number): void {
    this.actionBusyId = id;

    this.requestService.reject(id)
      .pipe(finalize(() => {
        this.actionBusyId = null;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: () => this.reload(),
        error: err => console.error('Error rechazando', err),
      });
  }

  retry(id: number): void {
    this.actionBusyId = id;

    this.requestService.retry(id)
      .pipe(finalize(() => {
        this.actionBusyId = null;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: () => this.reload(),
        error: err => console.error('Error reintentando', err),
      });
  }

  cancel(id: number): void {
    this.actionBusyId = id;

    this.requestService.cancel(id)
      .pipe(finalize(() => {
        this.actionBusyId = null;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: () => this.reload(),
        error: err => console.error('Error cancelando', err),
      });
  }
}