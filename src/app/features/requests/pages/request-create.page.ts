import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { HeaderComponent } from '../../../core/layout/header.component';
import { RequestService } from '../services/request.service';

@Component({
  selector: 'app-request-create',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderComponent],
  templateUrl: './request-create.page.html',
  styleUrls: ['./request-create.page.css'],
})
export class RequestCreatePage {
  title = '';
  description = '';
  loading = false;
  errorMessage = '';

  constructor(
    private requestService: RequestService,
    private router: Router
  ) {}

  create(): void {
    this.errorMessage = '';

    const title = this.title.trim();
    const description = this.description.trim();

    if (!title || !description) {
      this.errorMessage = 'Título y descripción son obligatorios';
      return;
    }

    this.loading = true;

    this.requestService.create({
      title,
      description,
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