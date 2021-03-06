package payments.service.dto;

import payments.domain.Payment;

public class PaymentDTO {

    private String id;
    private String bookingNumber;
    private String toPay;

    public PaymentDTO(Payment payment) {
        this.id = payment.getId().toString();
        this.bookingNumber = payment.getBookingNumber().toString();
        this.toPay = payment.getToPay().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBookingNumber() {
        return bookingNumber;
    }

    public void setBookingNumber(String bookingNumber) {
        this.bookingNumber = bookingNumber;
    }

    public String getToPay() {
        return toPay;
    }

    public void setToPay(String toPay) {
        this.toPay = toPay;
    }
}
