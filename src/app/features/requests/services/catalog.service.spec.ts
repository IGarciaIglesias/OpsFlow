/// <reference types="jasmine" />

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { CatalogService } from './catalog.service';
import { CatalogItem } from '../models/catalog.model';

describe('CatalogService', () => {
  let service: CatalogService;
  let httpMock: HttpTestingController;

  const mockCatalog: CatalogItem[] = [
    {
      id: 1,
      code: 'DRAFT',
      category: 'REQUEST_STATUS',
      description: 'Borrador',
      active: true,
    },
    {
      id: 2,
      code: 'APPROVED',
      category: 'REQUEST_STATUS',
      description: 'Aprobada',
      active: true,
    },
    {
      id: 3,
      code: 'CANCELLED',
      category: 'REQUEST_STATUS',
      description: 'Cancelada',
      active: false,
    },
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        CatalogService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    service = TestBed.inject(CatalogService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    sessionStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call GET /catalogs/{category} on first getByCategory', () => {
    let response: CatalogItem[] | undefined;

    service.getByCategory('REQUEST_STATUS').subscribe(data => {
      response = data;
    });

    const req = httpMock.expectOne('http://localhost:8080/catalogs/REQUEST_STATUS');
    expect(req.request.method).toBe('GET');
    req.flush(mockCatalog);

    expect(response).toEqual(mockCatalog);
  });

  it('should cache category results and avoid second HTTP call', () => {
    let firstResponse: CatalogItem[] | undefined;
    let secondResponse: CatalogItem[] | undefined;

    service.getByCategory('REQUEST_STATUS').subscribe(data => {
      firstResponse = data;
    });

    const req = httpMock.expectOne('http://localhost:8080/catalogs/REQUEST_STATUS');
    req.flush(mockCatalog);

    service.getByCategory('REQUEST_STATUS').subscribe(data => {
      secondResponse = data;
    });

    httpMock.expectNone('http://localhost:8080/catalogs/REQUEST_STATUS');
    expect(firstResponse).toEqual(mockCatalog);
    expect(secondResponse).toEqual(mockCatalog);
  });

  it('should filter only active items in getActiveByCategory', () => {
    let response: CatalogItem[] | undefined;

    service.getActiveByCategory('REQUEST_STATUS').subscribe(data => {
      response = data;
    });

    const req = httpMock.expectOne('http://localhost:8080/catalogs/REQUEST_STATUS');
    expect(req.request.method).toBe('GET');
    req.flush(mockCatalog);

    expect(response).toEqual([
      {
        id: 1,
        code: 'DRAFT',
        category: 'REQUEST_STATUS',
        description: 'Borrador',
        active: true,
      },
      {
        id: 2,
        code: 'APPROVED',
        category: 'REQUEST_STATUS',
        description: 'Aprobada',
        active: true,
      },
    ]);
  });

  it('should propagate error in getActiveByCategory', () => {
    let receivedError: unknown;

    service.getActiveByCategory('REQUEST_STATUS').subscribe({
      next: () => fail('Expected error'),
      error: err => {
        receivedError = err;
      },
    });

    const req = httpMock.expectOne('http://localhost:8080/catalogs/REQUEST_STATUS');
    req.flush('error', { status: 500, statusText: 'Server Error' });

    expect(receivedError).toBeTruthy();
  });
});