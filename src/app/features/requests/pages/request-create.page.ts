import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RequestService } from '../services/request.service';

@Component({
  selector: 'app-request-create',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './request-create.page.html',
  styleUrls: ['./request-create.page.css'],
})
export class RequestCreatePage {

  title = '';
  description = '';
  loading = false;

  constructor(
    private requestService: RequestService,
    private router: Router
  ) {}

  create(): void {
    if (!this.title.trim() || !this.description.trim()) {
      return;
    }

    this.loading = true;

    this.requestService.create({
      title: this.title,
      description: this.description,
    }).subscribe({
      next: () => {
        this.router.navigate(['/requests']);
      },
      error: err => {
        console.error('Error creando request', err);
        this.loading = false;
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/requests']);
  }
}