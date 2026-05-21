export interface Reservation {
  id: number;
  customerName: string;
  customerPhone: string;
  numberOfTickets: number;
  tripId: number;
  tripAttraction: string;
  agencyId: number;
}

export interface ReservationUpdate {
  customerName: string;
  customerPhone: string;
  numberOfTickets: number;
}
