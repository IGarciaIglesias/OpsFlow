import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { RequestDetailPage } from './request-detail.page';
import { RequestService } from '../services/request.service';

describe('RequestDetailPage', () => {
  let component: RequestDetailPage;
  let fixture: ComponentFixture<RequestDetailPage>;
  let requestServiceMock: jasmine.SpyObj<RequestService>;
  let routerMock: jasmine.SpyObj<Router>;

  const mockRequest = {
    id: 1,
    title: 'Request 1',
    description: 'Descripción',
    status: 'PENDING' as any,
    createdAt: '2026-04-20T09:00:00',
  };

  const mockHistory = [
    {
      fromStatus: 'PENDING',
      toStatus: 'APPROVED',
      changedAt: '2026-04-20T10:00:00',
    },
  ];

  beforeEach(async () => {
    requestServiceMock = jasmine.createSpyObj('RequestService', [
      'getById',
      'getHistory',
    ]);

    routerMock = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [RequestDetailPage],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: () => '1',
              },
            },
          },
        },
        { provide: RequestService, useValue: requestServiceMock },
        { provide: Router, useValue: routerMock },
      ],
    }).compileComponents();
  });

  it('should create', () => {
    requestServiceMock.getById.and.returnValue(of(mockRequest));
    requestServiceMock.getHistory.and.returnValue(of(mockHistory));

    fixture = TestBed.createComponent(RequestDetailPage);
    component = fixture.componentInstance;

    expect(component).toBeTruthy();
  });

  it('should load request and history on init', () => {
    requestServiceMock.getById.and.returnValue(of(mockRequest));
    requestServiceMock.getHistory.and.returnValue(of(mockHistory));

    fixture = TestBed.createComponent(RequestDetailPage);
    component = fixture.componentInstance;

    const detectChangesSpy = spyOn((component as any).cdr, 'detectChanges').and.callThrough();

    component.ngOnInit();

    expect(requestServiceMock.getById).toHaveBeenCalledWith(1);
    expect(requestServiceMock.getHistory).toHaveBeenCalledWith(1);
    expect(component.request).toEqual(mockRequest);
    expect(component.history).toEqual(mockHistory);
    expect(detectChangesSpy).toHaveBeenCalled();
  });

  it('should handle getById error', () => {
    spyOn(console, 'error');
    requestServiceMock.getById.and.returnValue(throwError(() => new Error('detail error')));
    requestServiceMock.getHistory.and.returnValue(of(mockHistory));

    fixture = TestBed.createComponent(RequestDetailPage);
    component = fixture.componentInstance;

    component.ngOnInit();

    expect(console.error).toHaveBeenCalled();
    expect(requestServiceMock.getHistory).toHaveBeenCalledWith(1);
  });

  it('should handle getHistory error', () => {
    spyOn(console, 'error');
    requestServiceMock.getById.and.returnValue(of(mockRequest));
    requestServiceMock.getHistory.and.returnValue(throwError(() => new Error('history error')));

    fixture = TestBed.createComponent(RequestDetailPage);
    component = fixture.componentInstance;

    component.ngOnInit();

    expect(console.error).toHaveBeenCalled();
    expect(requestServiceMock.getById).toHaveBeenCalledWith(1);
  });

  it('back should navigate to /requests', () => {
    requestServiceMock.getById.and.returnValue(of(mockRequest));
    requestServiceMock.getHistory.and.returnValue(of(mockHistory));

    fixture = TestBed.createComponent(RequestDetailPage);
    component = fixture.componentInstance;

    component.back();

    expect(routerMock.navigate).toHaveBeenCalledWith(['/requests']);
  });
});