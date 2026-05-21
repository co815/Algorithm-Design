import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Agency } from '../models/agency.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private _agency = signal<Agency | null>(this.loadFromStorage());
  readonly agency = this._agency.asReadonly();

  constructor(private http: HttpClient) {}

  private loadFromStorage(): Agency | null {
    const stored = localStorage.getItem('agency');
    return stored ? JSON.parse(stored) : null;
  }

  isLoggedIn(): boolean {
    return this._agency() !== null;
  }

  login(username: string, password: string): Observable<Agency> {
    return this.http.post<Agency>(`${environment.apiUrl}/auth/login`, { username, password }).pipe(
      tap(agency => {
        localStorage.setItem('agency', JSON.stringify(agency));
        this._agency.set(agency);
      })
    );
  }

  logout(): void {
    localStorage.removeItem('agency');
    this._agency.set(null);
  }
}
