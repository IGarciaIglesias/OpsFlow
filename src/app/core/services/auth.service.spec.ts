/// <reference types="jasmine" />

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { AuthService } from './auth.service';
import { LoginRequest, LoginResponse } from '../../features/auth/models/login.model';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const LOGIN_URL = 'http://localhost:8080/auth/login';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    sessionStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    sessionStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should login with POST to /auth/login', () => {
    const loginRequest: LoginRequest = {
      username: 'test',
      password: 'password',
    };

    const mockResponse: LoginResponse = {
      token: 'mock-jwt-token',
    };

    service.login(loginRequest).subscribe((response) => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(LOGIN_URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(loginRequest);

    req.flush(mockResponse);
  });

  it('should save token to sessionStorage', () => {
    const token = 'mock-jwt-token';

    service.saveToken(token);

    expect(sessionStorage.getItem('token')).toBe(token);
  });

  it('should get token from sessionStorage', () => {
    const token = 'mock-jwt-token';
    sessionStorage.setItem('token', token);

    const result = service.getToken();

    expect(result).toBe(token);
  });

  it('should return null if no token exists in sessionStorage', () => {
    const result = service.getToken();

    expect(result).toBeNull();
  });

  it('should remove token from sessionStorage on logout', () => {
    sessionStorage.setItem('token', 'some-token');

    service.logout();

    expect(sessionStorage.getItem('token')).toBeNull();
  });
});