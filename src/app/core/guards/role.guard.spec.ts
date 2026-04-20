import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';

import { roleGuard } from './role.guard';

describe('roleGuard', () => {
  let routerMock: { navigate: jasmine.Spy };

  beforeEach(() => {
    sessionStorage.clear();

    routerMock = {
      navigate: jasmine.createSpy('navigate'),
    };

    TestBed.configureTestingModule({
      providers: [
        {
          provide: Router,
          useValue: routerMock,
        },
      ],
    });
  });

  function createFakeToken(payload: object): string {
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
    const body = btoa(JSON.stringify(payload));
    const signature = 'fake-signature';
    return `${header}.${body}.${signature}`;
  }

  it('should return true when user role is allowed', () => {
    const token = createFakeToken({ role: 'ROLE_ADMIN' });
    sessionStorage.setItem('token', token);

    const route = {
      data: {
        roles: ['ADMIN', 'MANAGER'],
      },
    } as any;

    const result = TestBed.runInInjectionContext(() =>
      roleGuard(route, {} as any)
    );

    expect(result).toBe(true);
    expect(routerMock.navigate).not.toHaveBeenCalled();
  });

  it('should return false and navigate to /requests when token does not exist', () => {
    const route = {
      data: {
        roles: ['ADMIN'],
      },
    } as any;

    const result = TestBed.runInInjectionContext(() =>
      roleGuard(route, {} as any)
    );

    expect(result).toBe(false);
    expect(routerMock.navigate).toHaveBeenCalledWith(['/requests']);
  });

  it('should return false and navigate to /requests when token is invalid', () => {
    sessionStorage.setItem('token', 'invalid-token');

    const route = {
      data: {
        roles: ['ADMIN'],
      },
    } as any;

    const result = TestBed.runInInjectionContext(() =>
      roleGuard(route, {} as any)
    );

    expect(result).toBe(false);
    expect(routerMock.navigate).toHaveBeenCalledWith(['/requests']);
  });

  it('should return false and navigate to /requests when role is not allowed', () => {
    const token = createFakeToken({ role: 'ROLE_OPERATOR' });
    sessionStorage.setItem('token', token);

    const route = {
      data: {
        roles: ['ADMIN', 'MANAGER'],
      },
    } as any;

    const result = TestBed.runInInjectionContext(() =>
      roleGuard(route, {} as any)
    );

    expect(result).toBe(false);
    expect(routerMock.navigate).toHaveBeenCalledWith(['/requests']);
  });

  it('should return false and navigate to /requests when payload has no role', () => {
    const token = createFakeToken({});
    sessionStorage.setItem('token', token);

    const route = {
      data: {
        roles: ['ADMIN'],
      },
    } as any;

    const result = TestBed.runInInjectionContext(() =>
      roleGuard(route, {} as any)
    );

    expect(result).toBe(false);
    expect(routerMock.navigate).toHaveBeenCalledWith(['/requests']);
  });
});