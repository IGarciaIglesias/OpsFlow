import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { RequestService } from '../services/request.service';
import { Request } from '../models/request.model';
import { HeaderComponent } from '../../../core/layout/header.component';


@Component({
  selector: 'app-request-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, HeaderComponent],
  templateUrl: './request-detail.page.html',
  styleUrls: ['./request-detail.page.css'],   
})
export class RequestDetailPage implements OnInit {

  request!: Request;
  history: {
    fromStatus: string;
    toStatus: string;
    changedAt: string;
  }[] = [];

  constructor(
    private route: ActivatedRoute,
    private requestService: RequestService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    // DETALLE
    this.requestService.getById(id).subscribe({
      next: (data: Request) => {
        this.request = data;
        this.cdr.detectChanges();
      },
      error: (err: unknown) => console.error(err),
    });

    // HISTÓRICO
    this.requestService.getHistory(id).subscribe({
      next: (data: any[]) => {
        this.history = data;
        this.cdr.detectChanges();
      },
      error: (err: unknown) => console.error(err),
    });
  }

  back(): void {
    this.router.navigate(['/requests']);
  }
}