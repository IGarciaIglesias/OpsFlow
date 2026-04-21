import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, tap } from 'rxjs';

import { CatalogItem } from '../models/catalog.model';

@Injectable({
  providedIn: 'root',
})
export class CatalogService {
  private readonly API_URL = 'http://localhost:8080/catalogs';

  private cache = new Map<string, CatalogItem[]>();

  constructor(private http: HttpClient) {}

  getByCategory(category: string): Observable<CatalogItem[]> {
    const cached = this.cache.get(category);
    if (cached) {
      return of(cached);
    }

    return this.http.get<CatalogItem[]>(`${this.API_URL}/${category}`).pipe(
      tap(items => this.cache.set(category, items))
    );
  }

  getActiveByCategory(category: string): Observable<CatalogItem[]> {
    return new Observable<CatalogItem[]>(observer => {
      this.getByCategory(category).subscribe({
        next: items => {
          observer.next(items.filter(item => item.active));
          observer.complete();
        },
        error: err => observer.error(err),
      });
    });
  }
}