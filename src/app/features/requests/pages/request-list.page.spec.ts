import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { RequestListPage } from './request-list.page';
import { RequestService } from '../services/request.service';
import { Request } from '../models/request.model';

describe('RequestListPage', () => {
  let component: RequestListPage;
  let fixture: ComponentFixture<RequestListPage>;
  let requestServiceMock: jasmine.SpyObj<RequestService>;
  let router: Router;

  const mockRequests: Request[] = [
    {
      id: 2,
      title: 'Request 2',
      description: 'Desc 2',
      status: 'PENDING' as any,
      createdAt: '2026-04-20T09:00:00',
    },
    {
      id: 1,
      title: 'Request 1',
      description: 'Desc 1',
      status: 'APPROVED' as any,
      createdAt: '2026-04-20T08:00:00',
    },
  ];

  beforeEach(async () => {
    requestServiceMock = jasmine.createSpyObj('RequestService', [
      'getAll',
      'approve',
      'reject',
      'retry',
    ]);

    requestServiceMock.getAll.and.returnValue(of(mockRequests));
    requestServiceMock.approve.and.returnValue(of(void 0));
    requestServiceMock.reject.and.returnValue(of(void 0));
    requestServiceMock.retry.and.returnValue(of(void 0));

    await TestBed.configureTestingModule({
      imports: [RequestListPage, RouterTestingModule],
      providers: [
        { provide: RequestService, useValue: requestServiceMock },
      ],
    }).compileComponents();

    // Espiar navigate DESPUÉS de crear el TestBed, sobre el router real
    router = TestBed.inject(Router);
    spyOn(router, 'navigate');

    fixture = TestBed.createComponent(RequestListPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    sessionStorage.clear();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call getAll on init', () => {
    expect(requestServiceMock.getAll).toHaveBeenCalled();
  });

  it('should sort requests by id ascending', () => {
    expect(component.requests[0].id).toBe(1);
    expect(component.requests[1].id).toBe(2);
  });

  it('canCreate should be true for ADMIN', () => {
    component.role = 'ADMIN';
    expect(component.canCreate()).toBeTrue();
  });

  it('canCreate should be false for VIEWER', () => {
    component.role = 'VIEWER';
    expect(component.canCreate()).toBeFalse();
  });

  it('canApprove should be true for ADMIN', () => {
    component.role = 'ADMIN';
    expect(component.canApprove()).toBeTrue();
  });

  it('canApprove should be false for OPERATOR', () => {
    component.role = 'OPERATOR';
    expect(component.canApprove()).toBeFalse();
  });

  it('goToCreate should navigate to /requests/new', () => {
    component.goToCreate();
    expect(router.navigate).toHaveBeenCalledWith(['/requests/new']);
  });

  it('approve should call approve service', () => {
    component.approve(1);
    expect(requestServiceMock.approve).toHaveBeenCalledWith(1);
  });

  it('reject should call reject service', () => {
    component.reject(1);
    expect(requestServiceMock.reject).toHaveBeenCalledWith(1);
  });

  it('retry should call retry service', () => {
    component.retry(1);
    expect(requestServiceMock.retry).toHaveBeenCalledWith(1);
  });

  it('ngOnDestroy should unsubscribe pollSub', () => {
    const unsubscribeSpy = jasmine.createSpy('unsubscribe');
    (component as any).pollSub = { unsubscribe: unsubscribeSpy };
    component.ngOnDestroy();
    expect(unsubscribeSpy).toHaveBeenCalled();
  });
});