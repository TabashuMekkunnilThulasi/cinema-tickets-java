package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import static org.mockito.Mockito.mock;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

public class TicketServiceImplTest {
    private TicketServiceImpl createService() {
        TicketPaymentService paymentService = mock(TicketPaymentService.class);

        SeatReservationService seatReservationService = mock(SeatReservationService.class);

        return new TicketServiceImpl(
                paymentService,
                seatReservationService);
    }

    @Test
    void shouldRejectZeroAccountId() {

        TicketServiceImpl service = createService();

        assertThrows(
                InvalidPurchaseException.class,
                () -> service.purchaseTickets(0L));
    }

    @Test
    void shouldRejectNegativeAccountId() {
        TicketServiceImpl service = createService();

        assertThrows(
                InvalidPurchaseException.class,
                () -> service.purchaseTickets(-1L));
    }

    @Test
    void shouldRejectNullAccountId() {
        TicketServiceImpl service = createService();

        assertThrows(
                InvalidPurchaseException.class,
                () -> service.purchaseTickets(null));
    }

    @Test
    void shouldRejectEmptyTicketRequests() {
        TicketServiceImpl service = createService();

        assertThrows(
                InvalidPurchaseException.class,
                () -> service.purchaseTickets(1L));
    }

    @Test
    void shouldRejectNullTicketRequests() {
        TicketServiceImpl service = createService();

        assertThrows(
                InvalidPurchaseException.class,
                () -> service.purchaseTickets(
                        1L,
                        (TicketTypeRequest[]) null));
    }

    @Test
    void shouldRejectNullTicketRequestElement() {
        TicketServiceImpl service = createService();

        assertThrows(
                InvalidPurchaseException.class,
                () -> service.purchaseTickets(
                        1L,
                        (TicketTypeRequest) null));
    }

    @Test
    void shouldRejectZeroTicketQuantity() {
        TicketServiceImpl service = createService();

        TicketTypeRequest request = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT, 0);

        assertThrows(
                InvalidPurchaseException.class,
                () -> service.purchaseTickets(1L, request));
    }

    @Test
    void shouldRejectNegativeTicketQuantity() {
        TicketServiceImpl service = createService();

        TicketTypeRequest request = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT, -1);

        assertThrows(
                InvalidPurchaseException.class,
                () -> service.purchaseTickets(1L, request));
    }

    @Test
    void shouldRejectMoreThan25Tickets() {
        TicketServiceImpl service = createService();

        TicketTypeRequest request = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT, 26);

        assertThrows(
                InvalidPurchaseException.class,
                () -> service.purchaseTickets(1L, request));
    }

    @Test
    void shouldRejectMoreThan25TicketsAcrossMultipleRequests() {
        TicketServiceImpl service = createService();

        TicketTypeRequest adults = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT, 15);

        TicketTypeRequest children = new TicketTypeRequest(
                TicketTypeRequest.Type.CHILD, 11);

        assertThrows(
                InvalidPurchaseException.class,
                () -> service.purchaseTickets(1L, adults, children));
    }

    @Test
    void shouldRejectChildTicketsWithoutAdult() {
        TicketServiceImpl service = createService();

        TicketTypeRequest children = new TicketTypeRequest(
                TicketTypeRequest.Type.CHILD, 2);

        assertThrows(
                InvalidPurchaseException.class,
                () -> service.purchaseTickets(1L, children));
    }

    @Test
    void shouldAllowChildrenBeforeAdults() {
        TicketServiceImpl service = createService();

        TicketTypeRequest children = new TicketTypeRequest(
                TicketTypeRequest.Type.CHILD, 2);

        TicketTypeRequest adults = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT, 1);

        assertDoesNotThrow(
                () -> service.purchaseTickets(1L, children, adults));
    }

    @Test
    void shouldRejectNullTicketType() {
        TicketServiceImpl service = createService();

        TicketTypeRequest adult = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT, 1);

        TicketTypeRequest invalid = new TicketTypeRequest(
                null, 1);

        assertThrows(
                InvalidPurchaseException.class,
                () -> service.purchaseTickets(1L, adult, invalid));
    }

    @Test
    void shouldRejectInfantTicketsWithoutAdult() {
        TicketServiceImpl service = createService();

        TicketTypeRequest infant = new TicketTypeRequest(
                TicketTypeRequest.Type.INFANT, 1);

        assertThrows(
                InvalidPurchaseException.class,
                () -> service.purchaseTickets(1L, infant));
    }

    @Test
    void shouldCalculatePaymentForTwoAdults() {
        TicketPaymentService paymentService = mock(TicketPaymentService.class);

        SeatReservationService seatReservationService = mock(SeatReservationService.class);

        TicketServiceImpl service = new TicketServiceImpl(
                paymentService,
                seatReservationService);

        TicketTypeRequest adults = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT, 2);

        service.purchaseTickets(1L, adults);

        verify(paymentService).makePayment(1L, 50);
    }

    @Test
    void shouldCalculatePaymentForMixedTickets() {
        TicketPaymentService paymentService = mock(TicketPaymentService.class);

        SeatReservationService seatReservationService = mock(SeatReservationService.class);

        TicketServiceImpl service = new TicketServiceImpl(
                paymentService,
                seatReservationService);

        TicketTypeRequest adults = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT, 2);

        TicketTypeRequest children = new TicketTypeRequest(
                TicketTypeRequest.Type.CHILD, 3);

        TicketTypeRequest infants = new TicketTypeRequest(
                TicketTypeRequest.Type.INFANT, 1);

        service.purchaseTickets(1L, adults, children, infants);

        verify(paymentService).makePayment(1L, 95);
    }

    @Test
    void shouldReserveSeatsExcludingInfants() {
        TicketPaymentService paymentService = mock(TicketPaymentService.class);

        SeatReservationService seatReservationService = mock(SeatReservationService.class);

        TicketServiceImpl service = new TicketServiceImpl(
                paymentService,
                seatReservationService);

        TicketTypeRequest adults = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT, 2);

        TicketTypeRequest children = new TicketTypeRequest(
                TicketTypeRequest.Type.CHILD, 3);

        TicketTypeRequest infants = new TicketTypeRequest(
                TicketTypeRequest.Type.INFANT, 1);

        service.purchaseTickets(1L, adults, children, infants);

        verify(seatReservationService).reserveSeat(1L, 5);
    }

    @Test
    void shouldAllowExactly25Tickets() {
        TicketPaymentService paymentService = mock(TicketPaymentService.class);

        SeatReservationService seatReservationService = mock(SeatReservationService.class);

        TicketServiceImpl service = new TicketServiceImpl(
                paymentService,
                seatReservationService);

        TicketTypeRequest adults = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT, 10);

        TicketTypeRequest children = new TicketTypeRequest(
                TicketTypeRequest.Type.CHILD, 15);

        assertDoesNotThrow(
                () -> service.purchaseTickets(1L, adults, children));

        verify(paymentService).makePayment(1L, 475);
        verify(seatReservationService).reserveSeat(1L, 25);
    }

    @Test
    void shouldNotProcessPaymentOrReserveSeatsForInvalidPurchase() {
        TicketPaymentService paymentService = mock(TicketPaymentService.class);

        SeatReservationService seatReservationService = mock(SeatReservationService.class);

        TicketServiceImpl service = new TicketServiceImpl(
                paymentService,
                seatReservationService);

        TicketTypeRequest children = new TicketTypeRequest(
                TicketTypeRequest.Type.CHILD, 2);

        assertThrows(
                InvalidPurchaseException.class,
                () -> service.purchaseTickets(1L, children));

        verifyNoInteractions(paymentService, seatReservationService);
    }

    @Test
    void shouldRejectNullPaymentService() {
        SeatReservationService seatReservationService = mock(SeatReservationService.class);

        assertThrows(
                NullPointerException.class,
                () -> new TicketServiceImpl(null, seatReservationService));
    }

    @Test
    void shouldRejectNullSeatReservationService() {
        TicketPaymentService paymentService = mock(TicketPaymentService.class);

        assertThrows(
                NullPointerException.class,
                () -> new TicketServiceImpl(paymentService, null));
    }

    @Test
    void shouldRejectMoreThan25TicketsIncludingInfants() {
        TicketServiceImpl service = createService();

        TicketTypeRequest adults = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT, 1);

        TicketTypeRequest infants = new TicketTypeRequest(
                TicketTypeRequest.Type.INFANT, 25);

        assertThrows(
                InvalidPurchaseException.class,
                () -> service.purchaseTickets(1L, adults, infants));
    }
}
