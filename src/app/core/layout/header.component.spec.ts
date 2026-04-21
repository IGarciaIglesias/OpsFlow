/// <reference types="jasmine" />

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';

import { HeaderComponent } from './header.component';
import { AuthService } from '../services/auth.service';

describe('HeaderComponent', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;
  let authServiceMock: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(async () => {
    authServiceMock = jasmine.createSpyObj('AuthService', ['logout']);

    const payload = btoa(JSON.stringify({ role: 'ROLE_ADMIN' }));
    const fakeToken = `header.${payload}.signature`;
    sessionStorage.setItem('token', fakeToken);

    await TestBed.configureTestingModule({
      imports: [HeaderComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authServiceMock },
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, 'navigate');

    fixture = TestBed.createComponent(HeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    sessionStorage.clear();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize role from token through getUserRole', () => {
    expect(component.role).toBe('ADMIN');
  });

  it('logout should call authService.logout and navigate to /login', () => {
    component.logout();

    expect(authServiceMock.logout).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});