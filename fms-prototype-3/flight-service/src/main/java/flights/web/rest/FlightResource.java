package flights.web.rest;

import flights.domain.Flight;
import flights.domain.enumeration.ETopicType;
import flights.repository.FlightRepository;
import flights.service.FlightKafkaProducer;
import flights.web.rest.errors.BadRequestAlertException;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link flights.domain.Flight}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class FlightResource {

    private final Logger log = LoggerFactory.getLogger(FlightResource.class);

    private static final String ENTITY_NAME = "flightsFlight";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final FlightRepository flightRepository;
    private final FlightKafkaProducer flightKafkaProducer;

    public FlightResource(FlightRepository flightRepository, FlightKafkaProducer flightKafkaProducer) {
        this.flightRepository = flightRepository;
        this.flightKafkaProducer = flightKafkaProducer;
    }

    /**
     * {@code POST  /flights} : Create a new flight.
     *
     * @param flight the flight to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new flight, or with status {@code 400 (Bad Request)} if the flight has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/flights")
    public ResponseEntity<Flight> createFlight(@Valid @RequestBody Flight flight) throws URISyntaxException {
        log.debug("REST request to save Flight : {}", flight);
        if (flight.getId() != null) {
            throw new BadRequestAlertException("A new flight cannot already have an ID", ENTITY_NAME, "idexists");
        }

        Flight result = flightRepository.save(flight);
        flightKafkaProducer.sendFlightEvent(result, ETopicType.SET);

        return ResponseEntity.created(new URI("/api/flights/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /flights} : Updates an existing flight.
     *
     * @param flight the flight to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated flight,
     * or with status {@code 400 (Bad Request)} if the flight is not valid,
     * or with status {@code 500 (Internal Server Error)} if the flight couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/flights")
    public ResponseEntity<Flight> updateFlight(@Valid @RequestBody Flight flight) throws URISyntaxException {
        log.debug("REST request to update Flight : {}", flight);
        if (flight.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }

        Flight result = flightRepository.save(flight);
        flightKafkaProducer.sendFlightEvent(result, ETopicType.UPDATED);

        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, flight.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /flights} : get all the flights.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of flights in body.
     */
    @GetMapping("/flights")
    public List<Flight> getAllFlights() {
        log.debug("REST request to get all Flights");
        return flightRepository.findAll();
    }

    /**
     * {@code GET  /flights/:id} : get the "id" flight.
     *
     * @param id the id of the flight to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the flight, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/flights/{id}")
    public ResponseEntity<Flight> getFlight(@PathVariable Long id) {
        log.debug("REST request to get Flight : {}", id);
        Optional<Flight> flight = flightRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(flight);
    }

    /**
     * {@code DELETE  /flights/:id} : delete the "id" flight.
     *
     * @param id the id of the flight to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/flights/{id}")
    public ResponseEntity<Void> deleteFlight(@PathVariable Long id) {
        log.debug("REST request to delete Flight : {}", id);

        flightRepository.deleteById(id);
        if(flightRepository.findById(id).isPresent()) {
            flightKafkaProducer.sendFlightEvent(flightRepository.findById(id).get(), ETopicType.CANCELLED);
        }

        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build();
    }
}
