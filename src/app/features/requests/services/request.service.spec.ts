/// <reference types="jasmine" />

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import {
  PageResponseDto,
  RequestHistoryItem,
  RequestPayload,
  RequestService,
} from './request.service';
import { Request } from '../models/request.model';

describe('RequestService', () => {
  let service: RequestService;
  let httpMock: HttpTestingController;

  const API_URL = 'http://localhost:8080/requests';

  const mockRequest: Request = {
    id: 1,
    code: 'REQ-001',
    title: 'Request 1',
    description: 'Descripción 1',
    creator: 'iago',
    assignee: 'manager1',
    priority: 'MEDIUM' as any,
    type: 'SUPPORT' as any,
    status: 'PENDING_VALIDATION' as any,
    createdAt: '2026-04-20T08:00:00',
  };

  const mockRequest2: Request = {
    id: 2,
    code: 'REQ-002',
    title: 'Request 2',
    description: 'Descripción 2',
    creator: 'iago',
    assignee: null as any,
    priority: 'HIGH' as any,
    type: 'INCIDENT' as any,
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

  it('should call getAll with default query params', () => {
    const mockData: PageResponseDto<Request> = {
      content: [mockRequest, mockRequest2],
      page: 0,
      size: 10,
      totalElements: 2,
      totalPages: 1,
      last: true,
    };

    service.getAll().subscribe((data) => {
      expect(data).toEqual(mockData);
    });

    const req = httpMock.expectOne(r =>
      r.method === 'GET' && r.url === API_URL
    );

    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('10');
    expect(req.request.params.get('sort')).toBe('id,asc');
    expect(req.request.params.has('status')).toBeFalse();

    req.flush(mockData);
  });

  it('should call getAll with status query param when provided', () => {
    const mockData: PageResponseDto<Request> = {
      content: [mockRequest],
      page: 1,
      size: 5,
      totalElements: 1,
      totalPages: 1,
      last: true,
    };

    service.getAll(1, 5, 'APPROVED' as any).subscribe((data) => {
      expect(data).toEqual(mockData);
    });

    const req = httpMock.expectOne(r =>
      r.method === 'GET' && r.url === API_URL
    );

    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('size')).toBe('5');
    expect(req.request.params.get('sort')).toBe('id,asc');
    expect(req.request.params.get('status')).toBe('APPROVED');

    req.flush(mockData);
  });

  it('should call getById with GET', () => {
    service.getById(1).subscribe((data) => {
      expect(data).toEqual(mockRequest);
    });

    const req = httpMock.expectOne(`${API_URL}/1`);
    expect(req.request.method).toBe('GET');

    req.flush(mockRequest);
  });

  it('should call getHistory with GET', () => {
    const mockHistory: RequestHistoryItem[] = [
      {
        fromStatus: 'PENDING_VALIDATION',
        toStatus: 'APPROVED',
        changedAt: '2026-04-20T10:00:00',
        changedBy: 'manager1',
        comment: 'Aprobada',
      },
    ];

    service.getHistory(1).subscribe((data) => {
      expect(data).toEqual(mockHistory);
    });

    const req = httpMock.expectOne(`${API_URL}/1/history`);
    expect(req.request.method).toBe('GET');

    req.flush(mockHistory);
  });

  it('should call create with POST and payload', () => {
    const payload: RequestPayload = {
      title: 'Nueva request',
      description: 'Descripción nueva',
      creator: 'iago',
      assignee: null,
      priority: 'MEDIUM' as any,
      type: 'SUPPORT' as any,
    };

    service.create(payload).subscribe((data) => {
      expect(data).toEqual(mockRequest);
    });

    const req = httpMock.expectOne(API_URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);

    req.flush(mockRequest);
  });

  it('should call update with PUT and payload', () => {
    const payload: RequestPayload = {
      title: 'Request editada',
      description: 'Descripción editada',
      creator: 'iago',
      assignee: 'manager1',
      priority: 'HIGH' as any,
      type: 'INCIDENT' as any,
    };

    service.update(1, payload).subscribe((data) => {
      expect(data).toEqual(mockRequest);
    });

    const req = httpMock.expectOne(`${API_URL}/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(payload);

    req.flush(mockRequest);
  });

  it('should call submit with POST', () => {
    service.submit(1).subscribe((data) => {
      expect(data).toEqual(mockRequest);
    });

    const req = httpMock.expectOne(`${API_URL}/1/submit`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({});

    req.flush(mockRequest);
  });

  it('should call approve with POST', () => {
    service.approve(1).subscribe((data) => {
      expect(data).toEqual(mockRequest);
    });

    const req = httpMock.expectOne(`${API_URL}/1/approve`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({});

    req.flush(mockRequest);
  });

  it('should call reject with POST', () => {
    service.reject(1).subscribe((data) => {
      expect(data).toEqual(mockRequest);
    });

    const req = httpMock.expectOne(`${API_URL}/1/reject`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({});

    req.flush(mockRequest);
  });

  it('should call retry with POST', () => {
    service.retry(1).subscribe((data) => {
      expect(data).toEqual(mockRequest);
    });

    const req = httpMock.expectOne(`${API_URL}/1/retry`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({});

    req.flush(mockRequest);
  });

  it('should call cancel with POST', () => {
    service.cancel(1).subscribe((data) => {
      expect(data).toEqual(mockRequest);
    });

    const req = httpMock.expectOne(`${API_URL}/1/cancel`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({});

    req.flush(mockRequest);
  });
});