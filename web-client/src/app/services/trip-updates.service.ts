import { Injectable, OnDestroy } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class TripUpdatesService implements OnDestroy {
  private es: EventSource | null = null;
  private subject = new Subject<void>();
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null;
  private _connected = false;

  readonly updates$: Observable<void> = this.subject.asObservable();

  get connected(): boolean {
    return this._connected;
  }

  connect(): void {
    if (this.es) return;
    this.es = new EventSource(`${environment.apiUrl}/trips/updates`);
    this.es.onopen = () => {
      this._connected = true;
    };
    this.es.addEventListener('trip-changed', () => {
      this.subject.next();
    });
    this.es.onerror = () => {
      this._connected = false;
      this.es?.close();
      this.es = null;
      this.reconnectTimer = setTimeout(() => this.connect(), 5000);
    };
  }

  ngOnDestroy(): void {
    if (this.reconnectTimer) clearTimeout(this.reconnectTimer);
    this.es?.close();
    this.subject.complete();
  }
}
