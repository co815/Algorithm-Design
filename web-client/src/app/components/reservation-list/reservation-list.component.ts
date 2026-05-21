import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { ReservationService } from '../../services/reservation.service';
import { Reservation } from '../../models/reservation.model';

@Component({
  selector: 'app-reservation-list',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './reservation-list.component.html',
  styleUrls: ['./reservation-list.component.css']
})
export class ReservationListComponent implements OnInit {
  reservations = signal<Reservation[]>([]);
  error = signal<string | null>(null);
  editingId = signal<number | null>(null);

  editName = '';
  editPhone = '';
  editTickets = 1;

  constructor(
    private reservationService: ReservationService,
    private auth: AuthService
  ) {}

  ngOnInit(): void {
    this.loadReservations();
  }

  loadReservations(): void {
    const agencyId = this.auth.agency()!.id;
    this.reservationService.getByAgency(agencyId).subscribe({
      next: (list) => this.reservations.set(list),
      error: (err) => this.error.set(err.message)
    });
  }

  startEdit(r: Reservation): void {
    this.editingId.set(r.id);
    this.editName = r.customerName;
    this.editPhone = r.customerPhone;
    this.editTickets = r.numberOfTickets;
    this.error.set(null);
  }

  cancelEdit(): void {
    this.editingId.set(null);
  }

  confirmEdit(): void {
    if (this.editingId() == null) return;
    if (!this.editName.trim() || !this.editPhone.trim()) {
      this.error.set('Numele și telefonul sunt obligatorii.');
      return;
    }
    this.reservationService
      .update(this.editingId()!, {
        customerName: this.editName.trim(),
        customerPhone: this.editPhone.trim(),
        numberOfTickets: this.editTickets
      })
      .subscribe({
        next: () => {
          this.editingId.set(null);
          this.loadReservations();
        },
        error: (err) => this.error.set(err.error ?? err.message)
      });
  }

  deleteReservation(r: Reservation): void {
    if (!confirm(`Ștergi rezervarea pentru ${r.customerName}?`)) return;
    this.reservationService.delete(r.id).subscribe({
      next: () => this.loadReservations(),
      error: (err) => this.error.set(err.message)
    });
  }
}
