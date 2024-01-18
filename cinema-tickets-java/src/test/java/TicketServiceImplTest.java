import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class TicketServiceImplTest {

    @Mock
    private TicketPaymentService mockPaymentService;

    @Mock
    private SeatReservationService mockReservationService;

    private TicketServiceImpl ticketService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); 
        ticketService = new TicketServiceImpl(mockPaymentService, mockReservationService);
    }

    @Test
    public void testValidTicketPurchase() {
        TicketTypeRequest[] ticketRequests = {
            new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
            new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1)
        };

        assertDoesNotThrow(() -> ticketService.purchaseTickets(123L, ticketRequests));
        verify(mockPaymentService).makePayment(eq(123L), eq(50));
        verify(mockReservationService).reserveSeat(eq(123L), eq(3));
    }

    @Test
    public void testPurchaseWithNoTickets() {
        TicketTypeRequest[] ticketRequests = {};

        assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(123L, ticketRequests));
    }

    @Test
    public void testPurchaseExceedingMaxTickets() {
        TicketTypeRequest[] ticketRequests = {new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 21)};

        assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(123L, ticketRequests));
    }

    @Test
    public void testPurchaseNoAdultTickets() {
        TicketTypeRequest[] ticketRequests = {new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2)};

        assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(123L, ticketRequests));
    }

    @Test
    public void testPurchaseInfactMaxNoTickets() {
        TicketTypeRequest[] ticketRequests = {new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1),
            new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 3)};

        assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(123L, ticketRequests));
    }

}
