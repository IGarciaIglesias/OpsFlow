import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Request } from '../models/request.model';

@Injectable({
  providedIn: 'root',
})
export class RequestService {

  private readonly API_URL = 'http://localhost:8080/requests';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Request[]> {
    return this.http.get<Request[]>(this.API_URL);
  }

  getById(id: number): Observable<Request> {
    return this.http.get<Request>(`${this.API_URL}/${id}`);
  }

  // ✅ HISTÓRICO
  getHistory(id: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/${id}/history`);
  }

  // ✅ ACCIONES
  approve(id: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/${id}/approve`, {});
  }

  reject(id: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/${id}/reject`, {});
  }

  retry(id: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/${id}/retry`, {});
  }

  submit(id: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/${id}/submit`, {});
  }

  create(data: { title: string; description: string }): Observable<any> {
    return this.http.post(this.API_URL, data);
  }
}
