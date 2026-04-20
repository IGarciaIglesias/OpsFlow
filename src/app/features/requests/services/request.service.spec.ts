import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { RequestService } from './request.service';
import { Request } from '../models/request.model';

describe('RequestService', () => {
  let service: RequestService;
  let httpMock: HttpTestingController;

  const API_URL = 'http://localhost:8080/requests';

  const mockRequest: Request = {
    id: 1,
    title: 'Request 1',
    description: 'Descripción 1',
    status: 'PENDING' as any,
    createdAt: '2026-04-20T08:00:00',
  };

  const mockRequest2: Request = {
    id: 2,
    title: 'Request 2',
    description: 'Descripción 2',
    status: 'APPROVED' as any,
    createdAt: '2026-04-20T09:00:00',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        RequestService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    service = TestBed.inject(RequestService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call getAll with GET', () => {
    const mockData: Request[] = [mockRequest, mockRequest2];

    service.getAll().subscribe((data) => {
      expect(data).toEqual(mockData);
    });

    const req = httpMock.expectOne(API_URL);
    expect(req.request.method).toBe('GET');
    req.flush(mockData);
  });

  it('should call getById with GET', () => {
    const mockData: Request = mockRequest;

    service.getById(1).subscribe((data) => {
      expect(data).toEqual(mockData);
    });

    const req = httpMock.expectOne(`${API_URL}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockData);
  });

  it('should call getHistory with GET', () => {
    const mockData = [
      {
        fromStatus: 'PENDING',
        toStatus: 'APPROVED',
        changedAt: '2026-04-20T10:00:00',
      },
    ];

    service.getHistory(1).subscribe((data) => {
      expect(data).toEqual(mockData);
    });

    const req = httpMock.expectOne(`${API_URL}/1/history`);
    expect(req.request.method).toBe('GET');
    req.flush(mockData);
  });

  it('should call approve with POST', () => {
    service.approve(1).subscribe();

    const req = httpMock.expectOne(`${API_URL}/1/approve`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({});
    req.flush({});
  });

  it('should call reject with POST', () => {
    service.reject(1).subscribe();

    const req = httpMock.expectOne(`${API_URL}/1/reject`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({});
    req.flush({});
  });

  it('should call retry with POST', () => {
    service.retry(1).subscribe();

    const req = httpMock.expectOne(`${API_URL}/1/retry`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({});
    req.flush({});
  });

  it('should call submit with POST', () => {
    service.submit(1).subscribe();

    const req = httpMock.expectOne(`${API_URL}/1/submit`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({});
    req.flush({});
  });

  it('should call create with POST and payload', () => {
    const payload = {
      title: 'Nueva request',
      description: 'Descripción nueva',
    };

    service.create(payload).subscribe();

    const req = httpMock.expectOne(API_URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush({});
  });
});