import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { GatewayTestModule } from '../../../../test.module';
import { FlightDetailComponent } from 'app/entities/flights/flight/flight-detail.component';
import { Flight } from 'app/shared/model/flights/flight.model';

describe('Component Tests', () => {
  describe('Flight Management Detail Component', () => {
    let comp: FlightDetailComponent;
    let fixture: ComponentFixture<FlightDetailComponent>;
    const route = ({ data: of({ flight: new Flight(123) }) } as any) as ActivatedRoute;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [GatewayTestModule],
        declarations: [FlightDetailComponent],
        providers: [{ provide: ActivatedRoute, useValue: route }],
      })
        .overrideTemplate(FlightDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(FlightDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should load flight on init', () => {
        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.flight).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
