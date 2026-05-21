import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Trip, TripFilters } from '../models/trip.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class TripService {
  private readonly url = `${environment.apiUrl}/trips`;

  constructor(private http: HttpClient) {}

  getTrips(filters?: TripFilters): Observable<Trip[]> {
    let params = new HttpParams();
    if (filters?.attraction) params = params.set('attraction', filters.attraction);
    if (filters?.company) params = params.set('company', filters.company);
    if (filters?.minSeats != null) params = params.set('minSeats', String(filters.minSeats));
    if (filters?.maxPrice != null) params = params.set('maxPrice', String(filters.maxPrice));
    return this.http.get<Trip[]>(this.url, { params });
  }

  createTrip(trip: Omit<Trip, 'id'>): Observable<Trip> {
    return this.http.post<Trip>(this.url, trip);
  }

  updateTrip(id: number, trip: Omit<Trip, 'id'>): Observable<Trip> {
    return this.http.put<Trip>(`${this.url}/${id}`, trip);
  }

  deleteTrip(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  bookTrip(id: number, agencyId: number, customerName: string, customerPhone: string, seats: number): Observable<any> {
    return this.http.post(`${this.url}/${id}/book`, { agencyId, customerName, customerPhone, seats });
  }
}
