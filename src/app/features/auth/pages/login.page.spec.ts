/// <reference types="jasmine" />

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { provideRouter } from '@angular/router';
import { LoginPage } from './login.page';
import { AuthService } from '../../../core/services/auth.service';

describe('LoginPage', () => {
  let component: LoginPage;
  let fixture: ComponentFixture<LoginPage>;
  let authServiceMock: jasmine.SpyObj<AuthService>;
  let routerMock: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    authServiceMock = jasmine.createSpyObj('AuthService', ['login', 'saveToken']);
    routerMock = jasmine.createSpyObj('Router', ['navigate']);

  await TestBed.configureTestingModule({
    imports: [LoginPage],
    providers: [
      provideRouter([]),
      { provide: AuthService, useValue: authServiceMock },
      { provide: Router, useValue: routerMock },
    ],
  }).compileComponents();

    fixture = TestBed.createComponent(LoginPage);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    sessionStorage.clear();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not call login if username is empty', () => {
    component.username = '   ';
    component.password = 'secret';

    component.login();

    expect(authServiceMock.login).not.toHaveBeenCalled();
    expect(authServiceMock.saveToken).not.toHaveBeenCalled();
    expect(routerMock.navigate).not.toHaveBeenCalled();
    expect(component.loading).toBeFalse();
    expect(component.errorMessage).toBe('Introduce usuario y contraseña');
  });

  it('should not call login if password is empty', () => {
    component.username = 'admin';
    component.password = '';

    component.login();

    expect(authServiceMock.login).not.toHaveBeenCalled();
    expect(authServiceMock.saveToken).not.toHaveBeenCalled();
    expect(routerMock.navigate).not.toHaveBeenCalled();
    expect(component.loading).toBeFalse();
    expect(component.errorMessage).toBe('Introduce usuario y contraseña');
  });

  it('should call authService.login, saveToken and navigate on success', () => {
    authServiceMock.login.and.returnValue(of({ token: 'fake-token' } as any));

    component.username = '  ADMIN  ';
    component.password = 'ADMIN';

    component.login();

    expect(authServiceMock.login).toHaveBeenCalledWith({
      username: 'ADMIN',
      password: 'ADMIN',
    });
    expect(authServiceMock.saveToken).toHaveBeenCalledWith('fake-token');
    expect(routerMock.navigate).toHaveBeenCalledWith(['/requests']);
    expect(component.loading).toBeTrue();
    expect(component.errorMessage).toBe('');
  });

  it('should handle login error', () => {
    spyOn(console, 'error');
    authServiceMock.login.and.returnValue(
      throwError(() => new Error('login failed'))
    );

    component.username = 'admin';
    component.password = 'bad-pass';

    component.login();

    expect(authServiceMock.login).toHaveBeenCalled();
    expect(authServiceMock.saveToken).not.toHaveBeenCalled();
    expect(routerMock.navigate).not.toHaveBeenCalled();
    expect(component.errorMessage).toBe('Credenciales incorrectas');
    expect(component.loading).toBeFalse();
    expect(console.error).not.toHaveBeenCalled();
  });
});