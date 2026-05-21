import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { debounceTime } from 'rxjs/operators';
import { Trip } from '../../models/trip.model';
import { TripService } from '../../services/trip.service';
import { TripUpdatesService } from '../../services/trip-updates.service';
import { AuthService } from '../../services/auth.service';
import { NotificationBadgeComponent } from '../notification-badge/notification-badge.component';

@Component({
  selector: 'app-trip-list',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, NotificationBadgeComponent],
  templateUrl: './trip-list.component.html',
  styleUrls: ['./trip-list.component.css']
})
export class TripListComponent implements OnInit, OnDestroy {
  trips = signal<Trip[]>([]);
  error = signal<string | null>(null);
  bookingTripId = signal<number | null>(null);
  editingTripId = signal<number | null>(null);
  updateCount = signal(0);

  editAttraction = '';
  editCompany = '';
  editDeparture = '';
  editPrice = 0;
  editSeats = 1;

  bookingSeats = 1;
  bookingCustomerName = '';
  bookingCustomerPhone = '';

  filterForm: FormGroup;
  private subs = new Subscription();

  constructor(
    private tripService: TripService,
    private updatesService: TripUpdatesService,
    private auth: AuthService,
    private fb: FormBuilder
  ) {
    this.filterForm = this.fb.group({
      attraction: [''],
      company: [''],
      minSeats: [null],
      maxPrice: [null]
    });
  }

  get connected(): boolean {
    return this.updatesService.connected;
  }

  ngOnInit(): void {
    this.updatesService.connect();

    this.subs.add(
      this.filterForm.valueChanges
        .pipe(debounceTime(300))
        .subscribe(() => this.loadTrips())
    );

    this.subs.add(
      this.updatesService.updates$.subscribe(() => {
        this.updateCount.update(c => c + 1);
        this.loadTrips();
      })
    );

    this.loadTrips();
  }

  loadTrips(): void {
    const f = this.filterForm.value;
    const filters = {
      attraction: f.attraction || undefined,
      company: f.company || undefined,
      minSeats: f.minSeats ?? undefined,
      maxPrice: f.maxPrice ?? undefined
    };
    this.tripService.getTrips(filters).subscribe({
      next: (trips) => this.trips.set(trips),
      error: (err) => this.error.set(err.message)
    });
  }

  addTrip(): void {
    this.editingTripId.set(-1);
    this.editAttraction = '';
    this.editCompany = '';
    this.editDeparture = '';
    this.editPrice = 0;
    this.editSeats = 1;
    this.bookingTripId.set(null);
    this.error.set(null);
  }

  editTrip(trip: Trip): void {
    this.editingTripId.set(trip.id);
    this.editAttraction = trip.touristAttraction;
    this.editCompany = trip.transportCompany;
    this.editDeparture = trip.departureTime;
    this.editPrice = trip.price;
    this.editSeats = trip.availableSeats;
    this.bookingTripId.set(null);
    this.error.set(null);
  }

  cancelEdit(): void {
    this.editingTripId.set(null);
  }

  saveTrip(): void {
    if (!this.editAttraction.trim() || !this.editCompany.trim() || !this.editDeparture.trim()) {
      this.error.set('Destinația, compania de transport și ora plecării sunt obligatorii.');
      return;
    }
    if (!/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$/.test(this.editDeparture.trim())) {
      this.error.set('Format plecare invalid. Folosiți: YYYY-MM-DD HH:mm');
      return;
    }
    if (this.editPrice <= 0) {
      this.error.set('Prețul trebuie să fie mai mare decât 0.');
      return;
    }
    if (this.editSeats < 1) {
      this.error.set('Trebuie cel puțin 1 loc disponibil.');
      return;
    }
    const data = {
      touristAttraction: this.editAttraction.trim(),
      transportCompany: this.editCompany.trim(),
      departureTime: this.editDeparture.trim(),
      price: this.editPrice,
      availableSeats: this.editSeats
    };
    const id = this.editingTripId()!;
    const action = id === -1
      ? this.tripService.createTrip(data)
      : this.tripService.updateTrip(id, data);
    action.subscribe({
      next: () => { this.cancelEdit(); this.loadTrips(); },
      error: (err) => this.error.set(err.error ?? err.message)
    });
  }

  deleteTrip(trip: Trip): void {
    if (!confirm(`Ștergi excursia spre ${trip.touristAttraction}?`)) return;
    this.tripService.deleteTrip(trip.id).subscribe({
      next: () => this.loadTrips(),
      error: (err) => this.error.set(err.message)
    });
  }

  startBooking(trip: Trip): void {
    this.bookingTripId.set(trip.id);
    this.bookingSeats = 1;
    this.bookingCustomerName = '';
    this.bookingCustomerPhone = '';
    this.editingTripId.set(null);
    this.error.set(null);
  }

  confirmBooking(): void {
    if (this.bookingTripId() == null) return;
    if (!this.bookingCustomerName.trim() || !this.bookingCustomerPhone.trim()) {
      this.error.set('Numele și telefonul clientului sunt obligatorii.');
      return;
    }
    const agencyId = this.auth.agency()!.id;
    this.tripService
      .bookTrip(
        this.bookingTripId()!, agencyId,
        this.bookingCustomerName.trim(), this.bookingCustomerPhone.trim(),
        this.bookingSeats
      )
      .subscribe({
        next: () => {
          this.bookingTripId.set(null);
          this.bookingCustomerName = '';
          this.bookingCustomerPhone = '';
          this.loadTrips();
        },
        error: (err) => this.error.set(err.error ?? err.message)
      });
  }

  cancelBooking(): void {
    this.bookingTripId.set(null);
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }
}
