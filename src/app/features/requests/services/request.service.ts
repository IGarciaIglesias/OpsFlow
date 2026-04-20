import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Request } from '../models/request.model';
import { RequestStatus } from '../models/request-status.model';

export interface PageResponseDto<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface RequestHistoryItem {
  fromStatus: string;
  toStatus: string;
  changedAt: string;
  changedBy?: string;
  comment?: string;
}

export interface RequestPayload {
  title: string;
  description: string;
}

@Injectable({
  providedIn: 'root',
})
export class RequestService {
  private readonly API_URL = 'http://localhost:8080/requests';

  constructor(private http: HttpClient) {}

  getAll(
    page = 0,
    size = 10,
    status?: RequestStatus
  ): Observable<PageResponseDto<Request>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'id,asc');

    if (status) {
      params = params.set('status', status);
    }

    return this.http.get<PageResponseDto<Request>>(this.API_URL, { params });
  }

  getById(id: number): Observable<Request> {
    return this.http.get<Request>(`${this.API_URL}/${id}`);
  }

  getHistory(id: number): Observable<RequestHistoryItem[]> {
    return this.http.get<RequestHistoryItem[]>(`${this.API_URL}/${id}/history`);
  }

  create(data: RequestPayload): Observable<Request> {
    return this.http.post<Request>(this.API_URL, data);
  }

  update(id: number, data: RequestPayload): Observable<Request> {
    return this.http.put<Request>(`${this.API_URL}/${id}`, data);
  }

  submit(id: number): Observable<Request> {
    return this.http.post<Request>(`${this.API_URL}/${id}/submit`, {});
  }

  approve(id: number): Observable<Request> {
    return this.http.post<Request>(`${this.API_URL}/${id}/approve`, {});
  }

  reject(id: number): Observable<Request> {
    return this.http.post<Request>(`${this.API_URL}/${id}/reject`, {});
  }

  retry(id: number): Observable<Request> {
    return this.http.post<Request>(`${this.API_URL}/${id}/retry`, {});
  }

  cancel(id: number): Observable<Request> {
    return this.http.post<Request>(`${this.API_URL}/${id}/cancel`, {});
  }
}