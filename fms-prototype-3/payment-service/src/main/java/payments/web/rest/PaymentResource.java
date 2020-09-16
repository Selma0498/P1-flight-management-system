package payments.web.rest;

import payments.domain.CreditCard;
import payments.domain.Payment;
import payments.repository.PaymentRepository;
import payments.security.SecurityUtils;
import payments.service.PaymentKafkaProducer;
import payments.web.rest.errors.BadRequestAlertException;

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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link payments.domain.Payment}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class PaymentResource {

    private final Logger log = LoggerFactory.getLogger(PaymentResource.class);

    private static final String ENTITY_NAME = "paymentsPayment";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PaymentRepository paymentRepository;
    private final PaymentKafkaProducer paymentKafkaProducer;

    public PaymentResource(PaymentRepository paymentRepository, PaymentKafkaProducer paymentKafkaProducer) {
        this.paymentRepository = paymentRepository;
        this.paymentKafkaProducer = paymentKafkaProducer;
    }

    /**
     * {@code POST  /payments} : Create a new payment.
     *
     * @param payment the payment to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new payment, or with status {@code 400 (Bad Request)} if the payment has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/payments")
    public ResponseEntity<Payment> createPayment(@Valid @RequestBody Payment payment) throws URISyntaxException {
        log.debug("REST request to save Payment : {}", payment);
        if (payment.getId() != null) {
            throw new BadRequestAlertException("A new payment cannot already have an ID", ENTITY_NAME, "idexists");
        }

        try{
            checkPayment(payment.getToPay());
            creditCardValidityCheck(payment.getCreditCard());
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        Payment result = paymentRepository.save(payment);
        paymentKafkaProducer.sendPaymentSuccess(result);

        return ResponseEntity.created(new URI("/api/payments/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /payments} : get all the payments.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of payments in body.
     */
    @GetMapping("/payments")
    public List<Payment> getAllPayments() {
        log.debug("REST request to get all Payments");
        List<Payment> resultingPayments = new ArrayList<>();
        // Return 404 if the entity is not owned by the connected user
        Optional<String> userLogin = SecurityUtils.getCurrentUserLogin();

        for(Payment p: paymentRepository.findAll()) {
            if(userLogin.isPresent() && userLogin.get().equals(p.getPassengerId())) {
                resultingPayments.add(p);
            }
        }

        return resultingPayments;
    }

    /**
     * {@code GET  /payments/:id} : get the "id" payment.
     *
     * @param id the id of the payment to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the payment, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/payments/{id}")
    public ResponseEntity<Payment> getPayment(@PathVariable Long id) {
        log.debug("REST request to get Payment : {}", id);
        Optional<Payment> payment = paymentRepository.findById(id);
        // Return 404 if the entity is not owned by the connected user
        Optional<String> userLogin = SecurityUtils.getCurrentUserLogin();
        if(payment.isPresent() &&
            userLogin.isPresent() &&
            userLogin.get().equals(payment.get().getPassengerId())) {
            return ResponseUtil.wrapOrNotFound(payment);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * {@code DELETE  /payments/:id} : delete the "id" payment.
     *
     * @param id the id of the payment to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/payments/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        log.debug("REST request to delete Payment : {}", id);
        Optional<Payment> payment = paymentRepository.findById(id);

        // Return 404 if the entity is not owned by the connected user
        Optional<String> userLogin = SecurityUtils.getCurrentUserLogin();
        if(payment.isPresent() &&
            userLogin.isPresent() &&
            userLogin.get().equals(payment.get().getPassengerId())) {
            paymentRepository.deleteById(id);
        } else {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build();
   }

   private void creditCardValidityCheck(CreditCard card) throws Exception {
        if(card.getValidityDate().isBefore(LocalDate.now()) || card.getCardNumber() < 0 || card.getCvc() <0) {
            throw new Exception("Credit Card Data is not correct");
        }
   }

   private void checkPayment(Double toPay) throws Exception {
        if(toPay == null || toPay < 0) {
            throw new Exception("Invalid amount to pay");
        }
   }
}
