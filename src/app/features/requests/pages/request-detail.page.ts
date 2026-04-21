import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { finalize, Observable } from 'rxjs';

import { HeaderComponent } from '../../../core/layout/header.component';
import { getUserRole } from '../../../core/utils/auth.utils';
import { RequestService } from '../services/request.service';
import { Request } from '../models/request.model';

interface RequestHistoryItem {
  fromStatus: string;
  toStatus: string;
  changedAt: string;
  changedBy?: string;
  comment?: string;
}

@Component({
  selector: 'app-request-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, HeaderComponent],
  templateUrl: './request-detail.page.html',
  styleUrls: ['./request-detail.page.css'],
})
export class RequestDetailPage implements OnInit {
  request!: Request;
  history: RequestHistoryItem[] = [];
  loading = false;
  actionLoading = false;
  role: string | null = null;
  id!: number;

  constructor(
    private route: ActivatedRoute,
    private requestService: RequestService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.role = getUserRole();
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.loadData();
  }

  loadData(): void {
    this.loading = true;

    this.requestService.getById(this.id)
      .pipe(finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: (data: Request) => {
          this.request = data;
          this.cdr.detectChanges();
        },
        error: (err: unknown) => {
          console.error('Error cargando request', err);
          this.router.navigate(['/requests']);
        },
      });

    this.requestService.getHistory(this.id).subscribe({
      next: (data: RequestHistoryItem[]) => {
        this.history = data;
        this.cdr.detectChanges();
      },
      error: (err: unknown) => console.error('Error cargando histórico', err),
    });
  }

  submit(): void {
    this.runAction(this.requestService.submit(this.id));
  }

  approve(): void {
    this.runAction(this.requestService.approve(this.id));
  }

  reject(): void {
    this.runAction(this.requestService.reject(this.id));
  }

  retry(): void {
    this.runAction(this.requestService.retry(this.id));
  }

  cancel(): void {
    this.runAction(this.requestService.cancel(this.id));
  }

  private runAction(action$: Observable<unknown>): void {
    this.actionLoading = true;

    action$
      .pipe(finalize(() => {
        this.actionLoading = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: () => this.loadData(),
        error: (err: unknown) => console.error('Error ejecutando acción', err),
      });
  }

  trackHistory(index: number, item: RequestHistoryItem): string | number {
    return item.changedAt ?? index;
  }

  canSubmit(): boolean {
    return ['ADMIN', 'MANAGER', 'OPERATOR'].includes(this.role ?? '')
      && this.request?.status === 'DRAFT';
  }

  canApprove(): boolean {
    return ['ADMIN', 'MANAGER'].includes(this.role ?? '')
      && this.request?.status === 'VALIDATED';
  }

  canReject(): boolean {
    return ['ADMIN', 'MANAGER'].includes(this.role ?? '')
      && ['PENDING_VALIDATION', 'VALIDATED'].includes(this.request?.status ?? '');
  }

  canRetry(): boolean {
    return ['ADMIN', 'MANAGER', 'OPERATOR'].includes(this.role ?? '')
      && ['REJECTED', 'FAILED'].includes(this.request?.status ?? '');
  }

  canCancel(): boolean {
    return ['ADMIN', 'MANAGER'].includes(this.role ?? '')
      && !['COMPLETED', 'CANCELLED'].includes(this.request?.status ?? '');
  }

  back(): void {
    this.router.navigate(['/requests']);
  }
}