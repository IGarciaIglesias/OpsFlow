/// <reference types="jasmine" />

import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { errorInterceptor } from './error.interceptor';

describe('errorInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
        {
          provide: Router,
          useValue: jasmine.createSpyObj('Router', ['navigate']),
        },
      ],
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpMock.verify();
    sessionStorage.clear();
  });

  it('should remove token and navigate to /login on 401', () => {
    sessionStorage.setItem('token', 'abc');

    http.get('/test').subscribe({
      next: () => fail('Expected error'),
      error: () => {},
    });

    const req = httpMock.expectOne('/test');
    req.flush('unauthorized', { status: 401, statusText: 'Unauthorized' });

    expect(sessionStorage.getItem('token')).toBeNull();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should log access denied on 403', () => {
    spyOn(console, 'error');

    http.get('/test').subscribe({
      next: () => fail('Expected error'),
      error: () => {},
    });

    const req = httpMock.expectOne('/test');
    req.flush('forbidden', { status: 403, statusText: 'Forbidden' });

    expect(console.error).toHaveBeenCalledWith('Acceso denegado');
  });

  it('should log internal server error on 500', () => {
    spyOn(console, 'error');

    http.get('/test').subscribe({
      next: () => fail('Expected error'),
      error: () => {},
    });

    const req = httpMock.expectOne('/test');
    req.flush('server error', { status: 500, statusText: 'Server Error' });

    expect(console.error).toHaveBeenCalledWith('Error interno del servidor');
  });

  it('should rethrow the original error', () => {
    let receivedError: any;

    http.get('/test').subscribe({
      next: () => fail('Expected error'),
      error: err => {
        receivedError = err;
      },
    });

    const req = httpMock.expectOne('/test');
    req.flush('server error', { status: 500, statusText: 'Server Error' });

    expect(receivedError.status).toBe(500);
  });
});