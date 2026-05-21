export interface Trip {
  id: number;
  touristAttraction: string;
  transportCompany: string;
  departureTime: string;
  price: number;
  availableSeats: number;
}

export interface TripFilters {
  attraction?: string;
  company?: string;
  minSeats?: number;
  maxPrice?: number;
}
