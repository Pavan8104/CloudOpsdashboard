import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ServiceHealth, ServiceHealthSummary } from '../../shared/models/service-health.model';

/**
 * Service Health API Service - backend ke /services endpoints se baat karta hai.
 *
 * All HTTP calls yahan hain - components seedha HttpClient nahi use karte.
 * Centralized error handling future mein add karna easy ho jaayega.
 * Caching bhi baad mein add kar sakte hain - polling ke liye important hai.
 */
@Injectable({
  providedIn: 'root'
})
export class ServiceHealthService {

  private apiUrl = `${environment.apiUrl}/services`;

  constructor(private http: HttpClient) {}

  /**
   * Sabhi services ki list - dashboard main table ke liye.
   */
  getAll(): Observable<ServiceHealth[]> {
    return this.http.get<ServiceHealth[]>(this.apiUrl);
  }

  /**
   * ID se specific service.
   */
  getById(id: number): Observable<ServiceHealth> {
    return this.http.get<ServiceHealth>(`${this.apiUrl}/${id}`);
  }

  /**
   * Status ke basis pe filter - DOWN services ka quick view.
   */
  getByStatus(status: string): Observable<ServiceHealth[]> {
    return this.http.get<ServiceHealth[]>(`${this.apiUrl}/status/${status}`);
  }

  /**
   * Dashboard summary widget ke liye - { UP: 10, DOWN: 2 }
   */
  getSummary(): Observable<ServiceHealthSummary> {
    return this.http.get<ServiceHealthSummary>(`${this.apiUrl}/summary`);
  }

  /**
   * Naya service add karo.
   */
  create(service: Partial<ServiceHealth>): Observable<ServiceHealth> {
    return this.http.post<ServiceHealth>(this.apiUrl, service);
  }

  /**
   * Service update karo.
   */
  update(id: number, service: Partial<ServiceHealth>): Observable<ServiceHealth> {
    return this.http.put<ServiceHealth>(`${this.apiUrl}/${id}`, service);
  }

  /**
   * Sirf status update - quick patch.
   */
  updateStatus(id: number, status: string, message?: string, responseTimeMs?: number): Observable<ServiceHealth> {
    return this.http.patch<ServiceHealth>(`${this.apiUrl}/${id}/status`, {
      status,
      message,
      responseTimeMs
    });
  }

  /**
   * Service delete karo.
   */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
