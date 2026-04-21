/// <reference types="jasmine" />

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { RequestDetailPage } from './request-detail.page';
import { RequestService } from '../services/request.service';
import { Request } from '../models/request.model';

describe('RequestDetailPage', () => {
  let component: RequestDetailPage;
  let fixture: ComponentFixture<RequestDetailPage>;
  let requestServiceMock: jasmine.SpyObj<RequestService>;
  let router: Router;

  const mockRequest: Request = {
    id: 1,
    code: 'REQ-001',
    title: 'Request 1',
    description: 'Descripción',
    creator: 'iago',
    assignee: 'manager',
    priority: 'HIGH' as any,
    type: 'INCIDENT' as any,
    status: 'VALIDATED' as any,
    createdAt: '2026-04-20T09:00:00',
  };

  const mockHistory = [
    {
      fromStatus: 'PENDING_VALIDATION',
      toStatus: 'VALIDATED',
      changedAt: '2026-04-20T10:00:00',
      changedBy: 'manager',
      comment: 'Validada correctamente',
    },
  ];

  beforeEach(async () => {
    requestServiceMock = jasmine.createSpyObj('RequestService', [
      'getById',
      'getHistory',
      'submit',
      'approve',
      'reject',
      'retry',
      'cancel',
    ]);

    requestServiceMock.getById.and.returnValue(of(mockRequest));
    requestServiceMock.getHistory.and.returnValue(of(mockHistory));
    requestServiceMock.submit.and.returnValue(of(mockRequest));
    requestServiceMock.approve.and.returnValue(of(mockRequest));
    requestServiceMock.reject.and.returnValue(of(mockRequest));
    requestServiceMock.retry.and.returnValue(of(mockRequest));
    requestServiceMock.cancel.and.returnValue(of(mockRequest));

    await TestBed.configureTestingModule({
      imports: [RequestDetailPage],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: (key: string) => (key === 'id' ? '1' : null),
              },
            },
          },
        },
        { provide: RequestService, useValue: requestServiceMock },
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, 'navigate');

    fixture = TestBed.createComponent(RequestDetailPage);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    sessionStorage.clear();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load request and history on init', () => {
    component.ngOnInit();

    expect(component.id).toBe(1);
    expect(requestServiceMock.getById).toHaveBeenCalledWith(1);
    expect(requestServiceMock.getHistory).toHaveBeenCalledWith(1);
    expect(component.request).toEqual(mockRequest);
    expect(component.history).toEqual(mockHistory);
    expect(component.loading).toBeFalse();
  });

  it('should navigate to /requests when getById fails', () => {
    spyOn(console, 'error');
    requestServiceMock.getById.and.returnValue(throwError(() => new Error('detail error')));

    component.ngOnInit();

    expect(console.error).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/requests']);
    expect(requestServiceMock.getHistory).toHaveBeenCalledWith(1);
  });

  it('should handle getHistory error', () => {
    spyOn(console, 'error');
    requestServiceMock.getHistory.and.returnValue(throwError(() => new Error('history error')));

    component.ngOnInit();

    expect(requestServiceMock.getById).toHaveBeenCalledWith(1);
    expect(console.error).toHaveBeenCalled();
  });

  it('submit should call submit service and reload data', () => {
    const loadDataSpy = spyOn<any>(component, 'loadData');

    component.id = 1;
    component.submit();

    expect(requestServiceMock.submit).toHaveBeenCalledWith(1);
    expect(loadDataSpy).toHaveBeenCalled();
    expect(component.actionLoading).toBeFalse();
  });

  it('approve should call approve service and reload data', () => {
    const loadDataSpy = spyOn<any>(component, 'loadData');

    component.id = 1;
    component.approve();

    expect(requestServiceMock.approve).toHaveBeenCalledWith(1);
    expect(loadDataSpy).toHaveBeenCalled();
    expect(component.actionLoading).toBeFalse();
  });

  it('reject should call reject service and reload data', () => {
    const loadDataSpy = spyOn<any>(component, 'loadData');

    component.id = 1;
    component.reject();

    expect(requestServiceMock.reject).toHaveBeenCalledWith(1);
    expect(loadDataSpy).toHaveBeenCalled();
    expect(component.actionLoading).toBeFalse();
  });

  it('retry should call retry service and reload data', () => {
    const loadDataSpy = spyOn<any>(component, 'loadData');

    component.id = 1;
    component.retry();

    expect(requestServiceMock.retry).toHaveBeenCalledWith(1);
    expect(loadDataSpy).toHaveBeenCalled();
    expect(component.actionLoading).toBeFalse();
  });

  it('cancel should call cancel service and reload data', () => {
    const loadDataSpy = spyOn<any>(component, 'loadData');

    component.id = 1;
    component.cancel();

    expect(requestServiceMock.cancel).toHaveBeenCalledWith(1);
    expect(loadDataSpy).toHaveBeenCalled();
    expect(component.actionLoading).toBeFalse();
  });

  it('trackHistory should return changedAt when available', () => {
    const result = component.trackHistory(0, mockHistory[0]);
    expect(result).toBe('2026-04-20T10:00:00');
  });

  it('canSubmit should be true for OPERATOR with DRAFT', () => {
    component.role = 'OPERATOR';
    component.request = { ...mockRequest, status: 'DRAFT' as any };
    expect(component.canSubmit()).toBeTrue();
  });

  it('canApprove should be true for MANAGER with VALIDATED', () => {
    component.role = 'MANAGER';
    component.request = { ...mockRequest, status: 'VALIDATED' as any };
    expect(component.canApprove()).toBeTrue();
  });

  it('canReject should be true for ADMIN with PENDING_VALIDATION', () => {
    component.role = 'ADMIN';
    component.request = { ...mockRequest, status: 'PENDING_VALIDATION' as any };
    expect(component.canReject()).toBeTrue();
  });

  it('canRetry should be true for OPERATOR with FAILED', () => {
    component.role = 'OPERATOR';
    component.request = { ...mockRequest, status: 'FAILED' as any };
    expect(component.canRetry()).toBeTrue();
  });

  it('canCancel should be false for MANAGER with COMPLETED', () => {
    component.role = 'MANAGER';
    component.request = { ...mockRequest, status: 'COMPLETED' as any };
    expect(component.canCancel()).toBeFalse();
  });

  it('back should navigate to /requests', () => {
    component.back();
    expect(router.navigate).toHaveBeenCalledWith(['/requests']);
  });
});