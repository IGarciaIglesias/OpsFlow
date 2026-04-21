/// <reference types="jasmine" />

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';

import { RequestListPage } from './request-list.page';
import { RequestService } from '../services/request.service';
import { CatalogService } from '../services/catalog.service';
import { Request } from '../models/request.model';

describe('RequestListPage', () => {
  let component: RequestListPage;
  let fixture: ComponentFixture<RequestListPage>;
  let requestServiceMock: jasmine.SpyObj<RequestService>;
  let catalogServiceMock: jasmine.SpyObj<CatalogService>;
  let router: Router;

  const mockRequests: Request[] = [
    {
      id: 2,
      code: 'REQ-002',
      title: 'Request 2',
      description: 'Desc 2',
      creator: 'user2',
      assignee: 'manager2',
      priority: 'MEDIUM' as any,
      type: 'SUPPORT' as any,
      status: 'PENDING_VALIDATION' as any,
      createdAt: '2026-04-20T09:00:00',
    },
    {
      id: 1,
      code: 'REQ-001',
      title: 'Request 1',
      description: 'Desc 1',
      creator: 'user1',
      assignee: 'manager1',
      priority: 'HIGH' as any,
      type: 'INCIDENT' as any,
      status: 'APPROVED' as any,
      createdAt: '2026-04-20T08:00:00',
    },
  ];

  const pagedResponse = {
    content: mockRequests,
    page: 0,
    size: 10,
    totalElements: 2,
    totalPages: 1,
    first: true,
    last: true,
  };

  const statusCatalog = [
    { id: 1, code: 'DRAFT', category: 'REQUEST_STATUS', description: 'Borrador', active: true },
    { id: 2, code: 'PENDING_VALIDATION', category: 'REQUEST_STATUS', description: 'Pendiente de validación', active: true },
    { id: 3, code: 'VALIDATED', category: 'REQUEST_STATUS', description: 'Validada', active: true },
    { id: 4, code: 'REJECTED', category: 'REQUEST_STATUS', description: 'Rechazada', active: true },
    { id: 5, code: 'APPROVED', category: 'REQUEST_STATUS', description: 'Aprobada', active: true },
    { id: 6, code: 'IN_PROGRESS', category: 'REQUEST_STATUS', description: 'En progreso', active: true },
    { id: 7, code: 'COMPLETED', category: 'REQUEST_STATUS', description: 'Completada', active: true },
    { id: 8, code: 'FAILED', category: 'REQUEST_STATUS', description: 'Fallida', active: true },
    { id: 9, code: 'CANCELLED', category: 'REQUEST_STATUS', description: 'Cancelada', active: true },
  ];

  beforeEach(async () => {
    requestServiceMock = jasmine.createSpyObj('RequestService', [
      'getAll',
      'submit',
      'approve',
      'reject',
      'retry',
      'cancel',
    ]);

    catalogServiceMock = jasmine.createSpyObj('CatalogService', ['getActiveByCategory']);

    requestServiceMock.getAll.and.returnValue(of(pagedResponse as any));
    requestServiceMock.submit.and.returnValue(of(mockRequests[1]));
    requestServiceMock.approve.and.returnValue(of(mockRequests[1]));
    requestServiceMock.reject.and.returnValue(of(mockRequests[1]));
    requestServiceMock.retry.and.returnValue(of(mockRequests[1]));
    requestServiceMock.cancel.and.returnValue(of(mockRequests[1]));

    catalogServiceMock.getActiveByCategory.and.callFake((category: string) => {
      if (category === 'REQUEST_STATUS') {
        return of(statusCatalog as any);
      }
      return of([] as any);
    });

    await TestBed.configureTestingModule({
      imports: [RequestListPage, RouterTestingModule, NoopAnimationsModule],
      providers: [
        { provide: RequestService, useValue: requestServiceMock },
        { provide: CatalogService, useValue: catalogServiceMock },
      ],
    }).compileComponents();

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

  it('should call getAll on init with default pagination and no status', () => {
    expect(requestServiceMock.getAll).toHaveBeenCalledWith(0, 10, undefined);
  });

  it('should load paged requests on init', () => {
    expect(component.requests).toEqual(mockRequests);
    expect(component.totalPages).toBe(1);
    expect(component.totalElements).toBe(2);
  });

  it('should load status catalog on init', () => {
    expect(catalogServiceMock.getActiveByCategory).toHaveBeenCalledWith('REQUEST_STATUS');
    expect(component.availableStatuses).toEqual(statusCatalog as any);
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

  it('canReject should be true for MANAGER', () => {
    component.role = 'MANAGER';
    expect(component.canReject()).toBeTrue();
  });

  it('canReject should be false for VIEWER', () => {
    component.role = 'VIEWER';
    expect(component.canReject()).toBeFalse();
  });

  it('goToCreate should navigate to /requests/new', () => {
    component.goToCreate();
    expect(router.navigate).toHaveBeenCalledWith(['/requests/new']);
  });

  it('onStatusChange should update filter, reset page and reload', () => {
    requestServiceMock.getAll.calls.reset();

    component.page = 3;
    component.onStatusChange('APPROVED');

    expect(component.selectedStatus).toBe('APPROVED' as any);
    expect(component.page).toBe(0);
    expect(requestServiceMock.getAll).toHaveBeenCalledWith(0, 10, 'APPROVED');
  });

  it('clearFilters should clear status, reset page and reload', () => {
    requestServiceMock.getAll.calls.reset();

    component.selectedStatus = 'APPROVED' as any;
    component.page = 2;
    component.clearFilters();

    expect(component.selectedStatus).toBe('');
    expect(component.page).toBe(0);
    expect(requestServiceMock.getAll).toHaveBeenCalledWith(0, 10, undefined);
  });

  it('submit should call submit service and reload', () => {
    const reloadSpy = spyOn(component, 'reload');

    component.submit(1);

    expect(requestServiceMock.submit).toHaveBeenCalledWith(1);
    expect(reloadSpy).toHaveBeenCalled();
    expect(component.actionBusyId).toBeNull();
  });

  it('approve should call approve service and reload', () => {
    const reloadSpy = spyOn(component, 'reload');

    component.approve(1);

    expect(requestServiceMock.approve).toHaveBeenCalledWith(1);
    expect(reloadSpy).toHaveBeenCalled();
    expect(component.actionBusyId).toBeNull();
  });

  it('reject should call reject service and reload', () => {
    const reloadSpy = spyOn(component, 'reload');

    component.reject(1);

    expect(requestServiceMock.reject).toHaveBeenCalledWith(1);
    expect(reloadSpy).toHaveBeenCalled();
    expect(component.actionBusyId).toBeNull();
  });

  it('retry should call retry service and reload', () => {
    const reloadSpy = spyOn(component, 'reload');

    component.retry(1);

    expect(requestServiceMock.retry).toHaveBeenCalledWith(1);
    expect(reloadSpy).toHaveBeenCalled();
    expect(component.actionBusyId).toBeNull();
  });

  it('cancel should call cancel service and reload', () => {
    const reloadSpy = spyOn(component, 'reload');

    component.cancel(1);

    expect(requestServiceMock.cancel).toHaveBeenCalledWith(1);
    expect(reloadSpy).toHaveBeenCalled();
    expect(component.actionBusyId).toBeNull();
  });

  it('ngOnDestroy should unsubscribe pollSub', () => {
    const unsubscribeSpy = jasmine.createSpy('unsubscribe');
    (component as any).pollSub = { unsubscribe: unsubscribeSpy };
    component.ngOnDestroy();
    expect(unsubscribeSpy).toHaveBeenCalled();
  });
});