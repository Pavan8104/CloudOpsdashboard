import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ResourceUsage } from '../../shared/models/resource-usage.model';

/**
 * Resource Usage API Service - metrics aur chart data ke liye HTTP calls.
 *
 * Time range queries hain yahan - chart components ko historical data chahiye.
 * Date formatting backend ke ISO format ke according karna important hai.
 */
@Injectable({
  providedIn: 'root'
})
export class ResourceUsageService {

  private apiUrl = `${environment.apiUrl}/resources`;

  constructor(private http: HttpClient) {}

  /**
   * Sabhi services ki latest metrics - dashboard overview widget ke liye.
   */
  getLatestAll(): Observable<ResourceUsage[]> {
    return this.http.get<ResourceUsage[]>(`${this.apiUrl}/latest`);
  }

  /**
   * Specific service ki latest metrics - service detail view ke liye.
   */
  getLatestForService(serviceName: string): Observable<ResourceUsage[]> {
    return this.http.get<ResourceUsage[]>(`${this.apiUrl}/service/${serviceName}/latest`);
  }

  /**
   * Time range ke saath metrics - line chart data ke liye.
   * resourceType optional hai - null doge toh sab types aayenge.
   */
  getHistory(
    serviceName: string,
    start: Date,
    end: Date,
    resourceType?: string
  ): Observable<ResourceUsage[]> {
    let params = new HttpParams()
      .set('start', start.toISOString())
      .set('end', end.toISOString());

    if (resourceType) {
      params = params.set('resourceType', resourceType);
    }

    return this.http.get<ResourceUsage[]>(
      `${this.apiUrl}/service/${serviceName}/history`,
      { params }
    );
  }

  /**
   * Alert threshold cross kiye hue resources - alert panel ke liye.
   */
  getAlerts(): Observable<ResourceUsage[]> {
    return this.http.get<ResourceUsage[]>(`${this.apiUrl}/alerts`);
  }

  /**
   * Naya metric record karo - external systems call karte hain isse.
   */
  recordMetric(metric: Partial<ResourceUsage>): Observable<ResourceUsage> {
    return this.http.post<ResourceUsage>(`${this.apiUrl}/record`, metric);
  }
}
