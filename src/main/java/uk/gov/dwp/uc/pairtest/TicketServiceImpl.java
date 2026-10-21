package uk.gov.dwp.uc.pairtest;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import java.util.Objects;

public class TicketServiceImpl implements TicketService {
    /**
     * Should only have private methods other than the one below.
     */
    private final TicketPaymentService paymentService;
    private final SeatReservationService seatReservationService;

    public TicketServiceImpl(
            TicketPaymentService paymentService,
            SeatReservationService seatReservationService) {
        this.paymentService = Objects.requireNonNull(
                paymentService, "Payment service must not be null");
        this.seatReservationService = Objects.requireNonNull(
                seatReservationService, "Seat reservation service must not be null");
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests)
            throws InvalidPurchaseException {
        if (accountId == null || accountId <= 0) {
            throw new InvalidPurchaseException();
        }
        if (ticketTypeRequests == null || ticketTypeRequests.length == 0) {
            throw new InvalidPurchaseException();
        }
        int totalTickets = 0;
        int adultTickets = 0;
        int totalAmountToPay = 0;
        int totalSeatsToAllocate = 0;

        for (TicketTypeRequest request : ticketTypeRequests) {
            if (request == null
                    || request.getTicketType() == null
                    || request.getNoOfTickets() <= 0) {
                throw new InvalidPurchaseException();
            }

            totalTickets += request.getNoOfTickets();

            if (totalTickets > 25) {
                throw new InvalidPurchaseException();
            }
            if (request.getTicketType() == TicketTypeRequest.Type.ADULT) {
                adultTickets += request.getNoOfTickets();
            }
            switch (request.getTicketType()) {
                case ADULT:
                    totalAmountToPay += request.getNoOfTickets() * 25;
                    totalSeatsToAllocate += request.getNoOfTickets();
                    break;

                case CHILD:
                    totalAmountToPay += request.getNoOfTickets() * 15;
                    totalSeatsToAllocate += request.getNoOfTickets();
                    break;

                case INFANT:
                    break;
            }
        }
        if (adultTickets == 0) {
            throw new InvalidPurchaseException();
        }
        paymentService.makePayment(accountId, totalAmountToPay);

        seatReservationService.reserveSeat(accountId, totalSeatsToAllocate);
    }

}
