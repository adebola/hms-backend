import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';

/**
 * Base API service for making HTTP requests
 * Provides consistent API URL construction and request methods
 */
@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly baseUrl: string;
  private readonly apiVersion: string;

  constructor(private http: HttpClient) {
    this.baseUrl = environment.apiUrl;
    this.apiVersion = environment.apiVersion;
  }

  /**
   * Constructs full API URL
   */
  private getUrl(path: string): string {
    // Remove leading slash if present
    const cleanPath = path.startsWith('/') ? path.substring(1) : path;
    return `${this.baseUrl}/api/${this.apiVersion}/${cleanPath}`;
  }

  /**
   * GET request
   */
  get<T>(path: string, params?: HttpParams | { [param: string]: string | string[] }): Observable<T> {
    return this.http.get<T>(this.getUrl(path), { params });
  }

  /**
   * POST request
   */
  post<T>(path: string, body: any = {}): Observable<T> {
    return this.http.post<T>(this.getUrl(path), body);
  }

  /**
   * PUT request
   */
  put<T>(path: string, body: any = {}): Observable<T> {
    return this.http.put<T>(this.getUrl(path), body);
  }

  /**
   * DELETE request
   */
  delete<T>(path: string): Observable<T> {
    return this.http.delete<T>(this.getUrl(path));
  }

  /**
   * PATCH request
   */
  patch<T>(path: string, body: any = {}): Observable<T> {
    return this.http.patch<T>(this.getUrl(path), body);
  }
}
