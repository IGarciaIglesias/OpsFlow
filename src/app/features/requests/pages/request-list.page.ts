import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { finalize, interval, Subscription } from 'rxjs';

import { getUserRole } from '../../../core/utils/auth.utils';
import { RequestService } from '../services/request.service';
import { Request } from '../models/request.model';
import { HeaderComponent } from '../../../core/layout/header.component';

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

  private pollSub!: Subscription;

  constructor(
    private requestService: RequestService,
    private cdr: ChangeDetectorRef,
    private router: Router
  ) {}

  // =====================
  // Lifecycle
  // =====================

  ngOnInit(): void {
    this.role = getUserRole();

    // 📥 Carga inicial
    this.reload();

    // 🔁 Polling cada 2 segundos
    this.pollSub = interval(2000).subscribe(() => {
      this.reloadSilently();
    });
  }

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
  }

  // =====================
  // Permisos UI
  // =====================

  canCreate(): boolean {
    return ['ADMIN', 'MANAGER', 'OPERATOR'].includes(this.role ?? '');
  }

  canApprove(): boolean {
    return ['ADMIN', 'MANAGER'].includes(this.role ?? '');
  }

  // =====================
  // Navegación
  // =====================

  goToCreate(): void {
    this.router.navigate(['/requests/new']);
  }

  // =====================
  // Carga de datos
  // =====================

  /** Carga con loader (acciones manuales) */
  reload(): void {
    this.loading = true;

    this.requestService.getAll()
      .pipe(finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: data => {
          this.requests = [...data].sort((a, b) => a.id - b.id);
          this.cdr.detectChanges();
        },
        error: err => console.error('Error cargando requests', err),
      });
  }

  /** Carga silenciosa (polling) */
  private reloadSilently(): void {
    this.requestService.getAll().subscribe({
      next: data => {
        this.requests = [...data].sort((a, b) => a.id - b.id);
        this.cdr.detectChanges();
      },
      error: () => {
        // En polling no spameamos logs
      }
    });
  }

  // =====================
  // Acciones
  // =====================

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
}