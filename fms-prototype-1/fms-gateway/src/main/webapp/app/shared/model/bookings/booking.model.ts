
export interface IBooking {
  id?: number;
  bookingNumber?: number;
  flightNumber?: string;
  passengerId?: string;
}

export class Booking implements IBooking {
  constructor(public id?: number, public bookingNumber?: number, public flightNumber?: string, public passengerId?: string) {}
}
