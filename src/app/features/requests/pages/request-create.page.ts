import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';

import { HeaderComponent } from '../../../core/layout/header.component';
import { RequestService } from '../services/request.service';
import { RequestPriority, RequestType } from '../models/request.model';
import { CatalogService } from '../services/catalog.service';
import { CatalogItem } from '../models/catalog.model';

@Component({
  selector: 'app-request-create',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderComponent],
  templateUrl: './request-create.page.html',
  styleUrls: ['./request-create.page.css'],
})
export class RequestCreatePage implements OnInit {
  title = '';
  description = '';
  creator = '';
  assignee = '';
  priority: RequestPriority | '' = 'MEDIUM';
  type: RequestType | '' = 'SUPPORT';

  priorityOptions: CatalogItem[] = [];
  typeOptions: CatalogItem[] = [];

  loading = false;
  errorMessage = '';

  constructor(
    private requestService: RequestService,
    private catalogService: CatalogService,
    private router: Router
  ) {}

  ngOnInit(): void {
    forkJoin({
      priorities: this.catalogService.getActiveByCategory('REQUEST_PRIORITY'),
      types: this.catalogService.getActiveByCategory('REQUEST_TYPE'),
    }).subscribe({
      next: ({ priorities, types }) => {
        this.priorityOptions = priorities;
        this.typeOptions = types;

        if (!this.priority && priorities.length > 0) {
          this.priority = priorities[0].code as RequestPriority;
        }

        if (!this.type && types.length > 0) {
          this.type = types[0].code as RequestType;
        }
      },
      error: (err: unknown) => {
        console.error('Error cargando catálogos', err);
        this.errorMessage = 'No se pudieron cargar los catálogos';
      }
    });
  }

  create(): void {
    this.errorMessage = '';

    const title = this.title.trim();
    const description = this.description.trim();
    const creator = this.creator.trim();
    const assignee = this.assignee.trim();

    if (!title || !description || !creator || !this.priority || !this.type) {
      this.errorMessage = 'Completa los campos obligatorios antes de continuar';
      return;
    }

    this.loading = true;

    this.requestService.create({
      title,
      description,
      creator,
      assignee: assignee || null,
      priority: this.priority,
      type: this.type,
    }).subscribe({
      next: () => {
        this.router.navigate(['/requests']);
      },
      error: (err: unknown) => {
        console.error('Error creando request', err);
        this.errorMessage = 'No se pudo crear la solicitud';
        this.loading = false;
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/requests']);
  }
}