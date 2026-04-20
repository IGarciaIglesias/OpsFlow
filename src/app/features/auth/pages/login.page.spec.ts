import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { LoginPage } from './login.page';
import { AuthService } from '../services/auth.service';

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
        { provide: AuthService, useValue: authServiceMock },
        { provide: Router, useValue: routerMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call authService.login, saveToken and navigate on success', () => {
    authServiceMock.login.and.returnValue(
      of({ token: 'fake-token' } as any)
    );

    component.username = 'ADMIN';
    component.password = 'ADMIN';

    component.login();

    expect(authServiceMock.login).toHaveBeenCalledWith({
      username: 'ADMIN',
      password: 'ADMIN',
    });
    expect(authServiceMock.saveToken).toHaveBeenCalledWith('fake-token');
    expect(routerMock.navigate).toHaveBeenCalled();
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
    expect(console.error).toHaveBeenCalled();
  });
});