/// <reference types="jasmine" />

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { DashboardService } from './dashboard.service';
import { DashboardSummary } from '../models/dashboard-summary.model';

describe('DashboardService', () => {
  let service: DashboardService;
  let httpMock: HttpTestingController;

  const mockSummary: DashboardSummary = {
    totalRequests: 25,
    draft: 3,
    pendingValidation: 4,
    validated: 5,
    approved: 6,
    inProgress: 2,
    completed: 3,
    failed: 1,
    rejected: 1,
    cancelled: 0,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        DashboardService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    service = TestBed.inject(DashboardService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    sessionStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call GET /dashboard/summary', () => {
    let response: DashboardSummary | undefined;

    service.getSummary().subscribe(data => {
      response = data;
    });

    const req = httpMock.expectOne('http://localhost:8080/dashboard/summary');
    expect(req.request.method).toBe('GET');
    req.flush(mockSummary);

    expect(response).toEqual(mockSummary);
  });

  it('should propagate backend error', () => {
    let receivedError: unknown;

    service.getSummary().subscribe({
      next: () => fail('Expected error'),
      error: err => {
        receivedError = err;
      },
    });

    const req = httpMock.expectOne('http://localhost:8080/dashboard/summary');
    req.flush('error', { status: 500, statusText: 'Server Error' });

    expect(receivedError).toBeTruthy();
  });
});