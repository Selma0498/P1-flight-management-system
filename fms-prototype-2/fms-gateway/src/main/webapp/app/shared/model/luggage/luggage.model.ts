import { ELuggageType } from 'app/shared/model/enumerations/e-luggage-type.model';

export interface ILuggage {
  id?: number;
  luggageType?: ELuggageType;
  luggageNumber?: number;
  flightNumber?: string;
  bookingNumber?: number;
  passengerId?: string;
  weightCategory?: number;
  rfidTag?: string;
}

export class Luggage implements ILuggage {
  constructor(
    public id?: number,
    public luggageType?: ELuggageType,
    public luggageNumber?: number,
    public flightNumber?: string,
    public bookingNumber?: number,
    public passengerId?: string,
    public weightCategory?: number,
    public rfidTag?: string
  ) {}
}
