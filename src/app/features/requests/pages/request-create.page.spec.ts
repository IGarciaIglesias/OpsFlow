import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { RequestCreatePage } from './request-create.page';
import { RequestService } from '../services/request.service';

describe('RequestCreatePage', () => {
  let component: RequestCreatePage;
  let fixture: ComponentFixture<RequestCreatePage>;
  let requestServiceMock: jasmine.SpyObj<RequestService>;
  let routerMock: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    requestServiceMock = jasmine.createSpyObj('RequestService', ['create']);
    routerMock = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [RequestCreatePage],
      providers: [
        { provide: RequestService, useValue: requestServiceMock },
        { provide: Router, useValue: routerMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RequestCreatePage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not call create if title is empty', () => {
    component.title = '   ';
    component.description = 'Descripción válida';

    component.create();

    expect(requestServiceMock.create).not.toHaveBeenCalled();
    expect(component.loading).toBeFalse();
  });

  it('should not call create if description is empty', () => {
    component.title = 'Título válido';
    component.description = '   ';

    component.create();

    expect(requestServiceMock.create).not.toHaveBeenCalled();
    expect(component.loading).toBeFalse();
  });

  it('should call create service and navigate on success', () => {
    component.title = 'Nueva request';
    component.description = 'Nueva descripción';
    requestServiceMock.create.and.returnValue(of(void 0));

    component.create();

    expect(component.loading).toBeTrue();
    expect(requestServiceMock.create).toHaveBeenCalledWith({
      title: 'Nueva request',
      description: 'Nueva descripción',
    });
    expect(routerMock.navigate).toHaveBeenCalledWith(['/requests']);
  });

  it('should set loading to false on create error', () => {
    const error = new Error('create failed');
    spyOn(console, 'error');
    component.title = 'Nueva request';
    component.description = 'Nueva descripción';
    requestServiceMock.create.and.returnValue(
      throwError(() => error)
    );

    component.create();

    expect(requestServiceMock.create).toHaveBeenCalled();
    expect(console.error).toHaveBeenCalledWith('Error creando request', error);
    expect(component.loading).toBeFalse();
  });

  it('cancel should navigate to /requests', () => {
    component.cancel();

    expect(routerMock.navigate).toHaveBeenCalledWith(['/requests']);
  });
});