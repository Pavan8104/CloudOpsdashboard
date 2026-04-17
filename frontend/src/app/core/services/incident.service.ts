import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Incident } from '../../shared/models/incident.model';

/**
 * Incident API Service - incident management ke saare HTTP calls yahan.
 *
 * Active incidents aur critical incidents separate methods hain -
 * dashboard ke alag widgets yeh use karte hain.
 */
@Injectable({
  providedIn: 'root'
})
export class IncidentService {

  private apiUrl = `${environment.apiUrl}/incidents`;

  constructor(private http: HttpClient) {}

  /**
   * Sabhi incidents - incident list page ke liye.
   */
  getAll(): Observable<Incident[]> {
    return this.http.get<Incident[]>(this.apiUrl);
  }

  /**
   * Active incidents - on-call dashboard ka primary view.
   */
  getActive(): Observable<Incident[]> {
    return this.http.get<Incident[]>(`${this.apiUrl}/active`);
  }

  /**
   * Critical incidents (SEV1, SEV2) - alert banner ke liye.
   */
  getCritical(): Observable<Incident[]> {
    return this.http.get<Incident[]>(`${this.apiUrl}/critical`);
  }

  /**
   * ID se specific incident details.
   */
  getById(id: number): Observable<Incident> {
    return this.http.get<Incident>(`${this.apiUrl}/${id}`);
  }

  /**
   * Naya incident create karo.
   */
  create(incident: Partial<Incident>): Observable<Incident> {
    return this.http.post<Incident>(this.apiUrl, incident);
  }

  /**
   * Incident update karo - status change, assignment etc.
   */
  update(id: number, incident: Partial<Incident>): Observable<Incident> {
    return this.http.put<Incident>(`${this.apiUrl}/${id}`, incident);
  }

  /**
   * Incident resolve karo - resolution notes de do.
   */
  resolve(id: number, resolutionNotes: string): Observable<Incident> {
    return this.http.post<Incident>(`${this.apiUrl}/${id}/resolve`, { resolutionNotes });
  }

  /**
   * Incident delete karo - sirf ADMIN permission chahiye.
   */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
