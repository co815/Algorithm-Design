import { Component, signal } from '@angular/core';
import { TripListComponent } from './components/trip-list/trip-list.component';
import { LoginComponent } from './components/login/login.component';
import { ReservationListComponent } from './components/reservation-list/reservation-list.component';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [TripListComponent, LoginComponent, ReservationListComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  tab = signal<'trips' | 'reservations'>('trips');

  constructor(public auth: AuthService) {}
}
