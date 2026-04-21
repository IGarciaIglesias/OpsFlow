/// <reference types="jasmine" />

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { DashboardPage } from './dashboard.page';
import { DashboardService } from '../services/dashboard.service';
import { provideRouter } from '@angular/router';
import { DashboardSummary } from '../models/dashboard-summary.model';

describe('DashboardPage', () => {
  let component: DashboardPage;
  let fixture: ComponentFixture<DashboardPage>;
  let dashboardServiceMock: jasmine.SpyObj<DashboardService>;

  const mockRawSummary = {
    total: 25,
    draft: 3,
    pending: 4,
    validated: 5,
    approved: 6,
    inProgress: 2,
    completed: 3,
    failed: 1,
    rejected: 1,
    cancelled: 0,
  };

  beforeEach(async () => {
    dashboardServiceMock = jasmine.createSpyObj('DashboardService', [
      'getSummary',
    ]);

    dashboardServiceMock.getSummary.and.returnValue(of(mockRawSummary as any));

    await TestBed.configureTestingModule({
      imports: [DashboardPage],
      providers: [
        provideRouter([]),
        { provide: DashboardService, useValue: dashboardServiceMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardPage);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    sessionStorage.clear();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default summary values', () => {
    expect(component.summary).toEqual({
      totalRequests: 0,
      draft: 0,
      pendingValidation: 0,
      validated: 0,
      approved: 0,
      inProgress: 0,
      completed: 0,
      failed: 0,
      rejected: 0,
      cancelled: 0,
    });
    expect(component.cards).toEqual([]);
    expect(component.loading).toBeFalse();
  });

  it('should call loadSummary on init', () => {
    const loadSummarySpy = spyOn(component, 'loadSummary').and.callThrough();

    component.ngOnInit();

    expect(loadSummarySpy).toHaveBeenCalled();
    expect(dashboardServiceMock.getSummary).toHaveBeenCalled();
  });

  it('should load summary and map raw response correctly', () => {
    component.loadSummary();

    expect(dashboardServiceMock.getSummary).toHaveBeenCalled();

    expect(component.summary).toEqual({
      totalRequests: 25,
      draft: 3,
      pendingValidation: 4,
      validated: 5,
      approved: 6,
      inProgress: 2,
      completed: 3,
      failed: 1,
      rejected: 1,
      cancelled: 0,
    });

    expect(component.cards).toEqual([
      { label: 'Total', value: 25, className: 'total' },
      { label: 'Draft', value: 3, className: 'draft' },
      { label: 'Pending validation', value: 4, className: 'pending' },
      { label: 'Validated', value: 5, className: 'validated' },
      { label: 'Approved', value: 6, className: 'approved' },
      { label: 'In progress', value: 2, className: 'progress' },
      { label: 'Completed', value: 3, className: 'completed' },
      { label: 'Failed', value: 1, className: 'failed' },
      { label: 'Rejected', value: 1, className: 'rejected' },
      { label: 'Cancelled', value: 0, className: 'cancelled' },
    ]);

    expect(component.loading).toBeFalse();
  });

  it('should use 0 as fallback for missing fields', () => {
    dashboardServiceMock.getSummary.and.returnValue(
      of({
        total: 10,
        draft: 0,
        pending: 0,
        validated: 0,
        approved: 2,
        inProgress: 0,
        completed: 0,
        failed: 0,
        rejected: 0,
        cancelled: 0,
      } as any)
    );

    component.loadSummary();

    expect(component.summary).toEqual({
      totalRequests: 10,
      draft: 0,
      pendingValidation: 0,
      validated: 0,
      approved: 2,
      inProgress: 0,
      completed: 0,
      failed: 0,
      rejected: 0,
      cancelled: 0,
    });

    expect(component.cards).toEqual([
      { label: 'Total', value: 10, className: 'total' },
      { label: 'Draft', value: 0, className: 'draft' },
      { label: 'Pending validation', value: 0, className: 'pending' },
      { label: 'Validated', value: 0, className: 'validated' },
      { label: 'Approved', value: 2, className: 'approved' },
      { label: 'In progress', value: 0, className: 'progress' },
      { label: 'Completed', value: 0, className: 'completed' },
      { label: 'Failed', value: 0, className: 'failed' },
      { label: 'Rejected', value: 0, className: 'rejected' },
      { label: 'Cancelled', value: 0, className: 'cancelled' },
    ]);

    expect(component.loading).toBeFalse();
  });

  it('should set cards to empty and log error when service fails', () => {
    const error = new Error('dashboard failed');
    spyOn(console, 'error');
    dashboardServiceMock.getSummary.and.returnValue(throwError(() => error));

    component.cards = [
      { label: 'Old', value: 99, className: 'old' },
    ];

    component.loadSummary();

    expect(dashboardServiceMock.getSummary).toHaveBeenCalled();
    expect(console.error).toHaveBeenCalledWith('Error cargando dashboard', error);
    expect(component.cards).toEqual([]);
    expect(component.loading).toBeFalse();
  });

  it('should create 10 cards after successful load', () => {
    component.loadSummary();

    expect(component.cards.length).toBe(10);
  });

  it('should keep card labels in expected order', () => {
    component.loadSummary();

    expect(component.cards.map(card => card.label)).toEqual([
      'Total',
      'Draft',
      'Pending validation',
      'Validated',
      'Approved',
      'In progress',
      'Completed',
      'Failed',
      'Rejected',
      'Cancelled',
    ]);
  });
});