import { HttpRequest, HttpHandlerFn, HttpEvent } from '@angular/common/http';
import { of } from 'rxjs';

import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  beforeEach(() => {
    sessionStorage.clear();
  });

  it('should not add Authorization header for login requests', (done) => {
    sessionStorage.setItem('token', 'mock-token');

    const req = new HttpRequest('POST', 'http://localhost:8080/auth/login', {
      username: 'user',
      password: 'pass',
    });

    const next: HttpHandlerFn = (request) => {
      expect(request.headers.has('Authorization')).toBe(false);
      return of({} as HttpEvent<unknown>);
    };

    authInterceptor(req, next).subscribe(() => {
      done();
    });
  });

  it('should not add Authorization header when token does not exist', (done) => {
    const req = new HttpRequest('GET', 'http://localhost:8080/requests');

    const next: HttpHandlerFn = (request) => {
      expect(request.headers.has('Authorization')).toBe(false);
      return of({} as HttpEvent<unknown>);
    };

    authInterceptor(req, next).subscribe(() => {
      done();
    });
  });

  it('should add Authorization header when token exists and request is not login', (done) => {
    sessionStorage.setItem('token', 'mock-token');

    const req = new HttpRequest('GET', 'http://localhost:8080/requests');

    const next: HttpHandlerFn = (request) => {
      expect(request.headers.get('Authorization')).toBe('Bearer mock-token');
      return of({} as HttpEvent<unknown>);
    };

    authInterceptor(req, next).subscribe(() => {
      done();
    });
  });
});