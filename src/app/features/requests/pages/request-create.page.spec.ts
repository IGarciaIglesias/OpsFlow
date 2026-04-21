/// <reference types="jasmine" />

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { RequestCreatePage } from './request-create.page';
import { RequestService } from '../services/request.service';
import { CatalogService } from '../services/catalog.service';

describe('RequestCreatePage', () => {
  let component: RequestCreatePage;
  let fixture: ComponentFixture<RequestCreatePage>;
  let requestServiceMock: jasmine.SpyObj<RequestService>;
  let catalogServiceMock: jasmine.SpyObj<CatalogService>;
  let router: Router;

  const priorityCatalog = [
    { id: 1, code: 'LOW', category: 'REQUEST_PRIORITY', description: 'Baja', active: true },
    { id: 2, code: 'MEDIUM', category: 'REQUEST_PRIORITY', description: 'Media', active: true },
    { id: 3, code: 'HIGH', category: 'REQUEST_PRIORITY', description: 'Alta', active: true },
  ];

  const typeCatalog = [
    { id: 10, code: 'ACCESS', category: 'REQUEST_TYPE', description: 'Acceso', active: true },
    { id: 11, code: 'INCIDENT', category: 'REQUEST_TYPE', description: 'Incidente', active: true },
    { id: 12, code: 'CHANGE', category: 'REQUEST_TYPE', description: 'Cambio', active: true },
    { id: 13, code: 'SUPPORT', category: 'REQUEST_TYPE', description: 'Soporte', active: true },
  ];

  beforeEach(async () => {
    requestServiceMock = jasmine.createSpyObj('RequestService', ['create']);
    catalogServiceMock = jasmine.createSpyObj('CatalogService', ['getActiveByCategory']);

    requestServiceMock.create.and.returnValue(of({} as any));
    catalogServiceMock.getActiveByCategory.and.callFake((category: string) => {
      if (category === 'REQUEST_PRIORITY') {
        return of(priorityCatalog as any);
      }
      if (category === 'REQUEST_TYPE') {
        return of(typeCatalog as any);
      }
      return of([] as any);
    });

    await TestBed.configureTestingModule({
      imports: [RequestCreatePage],
      providers: [
        provideRouter([]),
        { provide: RequestService, useValue: requestServiceMock },
        { provide: CatalogService, useValue: catalogServiceMock },
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, 'navigate');

    fixture = TestBed.createComponent(RequestCreatePage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    sessionStorage.clear();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load catalog options on init', () => {
    expect(catalogServiceMock.getActiveByCategory).toHaveBeenCalledWith('REQUEST_PRIORITY');
    expect(catalogServiceMock.getActiveByCategory).toHaveBeenCalledWith('REQUEST_TYPE');
    expect(component.priorityOptions).toEqual(priorityCatalog as any);
    expect(component.typeOptions).toEqual(typeCatalog as any);
  });

  it('should not call create if required fields are missing', () => {
    component.title = '   ';
    component.description = 'Descripción válida';
    component.creator = '';
    component.priority = 'MEDIUM';
    component.type = 'SUPPORT';

    component.create();

    expect(requestServiceMock.create).not.toHaveBeenCalled();
    expect(component.loading).toBeFalse();
    expect(component.errorMessage).toBe('Completa los campos obligatorios antes de continuar');
  });

  it('should not call create if description is empty', () => {
    component.title = 'Título válido';
    component.description = '   ';
    component.creator = 'iago';
    component.priority = 'MEDIUM';
    component.type = 'SUPPORT';

    component.create();

    expect(requestServiceMock.create).not.toHaveBeenCalled();
    expect(component.loading).toBeFalse();
    expect(component.errorMessage).toBe('Completa los campos obligatorios antes de continuar');
  });

  it('should not call create if creator is empty', () => {
    component.title = 'Título válido';
    component.description = 'Descripción válida';
    component.creator = '   ';
    component.priority = 'MEDIUM';
    component.type = 'SUPPORT';

    component.create();

    expect(requestServiceMock.create).not.toHaveBeenCalled();
    expect(component.loading).toBeFalse();
    expect(component.errorMessage).toBe('Completa los campos obligatorios antes de continuar');
  });

  it('should call create service with trimmed values and navigate on success', () => {
    requestServiceMock.create.and.returnValue(
      of({
        id: 1,
        code: 'REQ-001',
        title: 'Nueva request',
        description: 'Nueva descripción',
        creator: 'iago',
        assignee: null,
        priority: 'MEDIUM',
        type: 'SUPPORT',
        status: 'DRAFT',
        createdAt: '2026-04-21T10:00:00',
      } as any)
    );

    component.title = ' Nueva request ';
    component.description = ' Nueva descripción ';
    component.creator = ' iago ';
    component.assignee = '   ';
    component.priority = 'MEDIUM';
    component.type = 'SUPPORT';

    component.create();

    expect(component.loading).toBeTrue();
    expect(component.errorMessage).toBe('');
    expect(requestServiceMock.create).toHaveBeenCalledWith({
      title: 'Nueva request',
      description: 'Nueva descripción',
      creator: 'iago',
      assignee: null,
      priority: 'MEDIUM',
      type: 'SUPPORT',
    });
    expect(router.navigate).toHaveBeenCalledWith(['/requests']);
  });

  it('should send assignee when provided', () => {
    requestServiceMock.create.and.returnValue(of({} as any));

    component.title = 'Nueva request';
    component.description = 'Nueva descripción';
    component.creator = 'iago';
    component.assignee = 'manager1';
    component.priority = 'HIGH';
    component.type = 'INCIDENT';

    component.create();

    expect(requestServiceMock.create).toHaveBeenCalledWith({
      title: 'Nueva request',
      description: 'Nueva descripción',
      creator: 'iago',
      assignee: 'manager1',
      priority: 'HIGH',
      type: 'INCIDENT',
    });
  });

  it('should set error message and loading false on create error', () => {
    const error = new Error('create failed');
    spyOn(console, 'error');

    requestServiceMock.create.and.returnValue(throwError(() => error));

    component.title = 'Nueva request';
    component.description = 'Nueva descripción';
    component.creator = 'iago';
    component.assignee = '';
    component.priority = 'MEDIUM';
    component.type = 'SUPPORT';

    component.create();

    expect(requestServiceMock.create).toHaveBeenCalled();
    expect(console.error).toHaveBeenCalledWith('Error creando request', error);
    expect(component.errorMessage).toBe('No se pudo crear la solicitud');
    expect(component.loading).toBeFalse();
  });

  it('cancel should navigate to /requests', () => {
    component.cancel();
    expect(router.navigate).toHaveBeenCalledWith(['/requests']);
  });
});