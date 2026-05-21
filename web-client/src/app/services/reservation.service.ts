import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Reservation, ReservationUpdate } from '../models/reservation.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ReservationService {
  private readonly url = `${environment.apiUrl}/reservations`;

  constructor(private http: HttpClient) {}

  getByAgency(agencyId: number): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(this.url, { params: { agencyId: String(agencyId) } });
  }

  update(id: number, patch: ReservationUpdate): Observable<Reservation> {
    return this.http.put<Reservation>(`${this.url}/${id}`, patch);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
