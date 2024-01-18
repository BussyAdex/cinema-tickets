package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {
    /**
     * Should only have private methods other than the one below.
     */
    private final TicketPaymentService _paymentService;
    private final SeatReservationService _reservationService;
    
    public TicketServiceImpl(TicketPaymentService paymentService, SeatReservationService reservationService) {
        this._paymentService = paymentService;
        this._reservationService = reservationService;
    }
    
    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        if (ticketTypeRequests == null || ticketTypeRequests.length == 0) {
            throw new InvalidPurchaseException("No ticket selected for this request");
        }

        int totalAmountPaymentDue = 0;
        int totalSeatsAllocationDue = 0;
        boolean adultIsPresent = false;

        for (TicketTypeRequest ticket : ticketTypeRequests) {
            Type typeOfTicket = ticket.getTicketType();
            int noOfTicket = ticket.getNoOfTickets();

            if (typeOfTicket == Type.INFANT){
                checkTicketNumberRange(noOfTicket, "Infant");
            }
            else if (typeOfTicket == Type.CHILD){
                if (checkTicketNumberRange(noOfTicket, "Child") && noOfTicket > 0){
                    totalAmountPaymentDue += (noOfTicket * 10);
                    totalSeatsAllocationDue += noOfTicket;
                }
            }
            else if (typeOfTicket == Type.ADULT) {
                if (checkTicketNumberRange(noOfTicket, "Adult") && noOfTicket > 0){
                    totalAmountPaymentDue += (noOfTicket * 20);
                    totalSeatsAllocationDue += noOfTicket;
                    adultIsPresent = true;
                }
            } else {
                throw new InvalidPurchaseException("Invalid ticket - Undefined..");
            }
        }

        checkTicketNumberRange(totalSeatsAllocationDue, "total");

        if (totalSeatsAllocationDue < 1) {
            throw new InvalidPurchaseException("No Valid Ticket.. Number of ticket should be atleast 1");
        }

        if(adultIsPresent){
            _paymentService.makePayment(accountId, totalAmountPaymentDue);
            _reservationService.reserveSeat(accountId, totalSeatsAllocationDue);    
                
        }else{
            throw new InvalidPurchaseException("Infact or Child tickets are required to have atleast one adult");
        }
    }

    private boolean checkTicketNumberRange(int noOfTicket, String name) {
        if (noOfTicket > 20) {
            if (name.equalsIgnoreCase("total")) {
                throw new InvalidPurchaseException("Total Number of Ticket is greater than 20 .. Number should not be greater than 20");
            }
            throw new InvalidPurchaseException("Too Many " + name + " Ticket Number Request .. Number should not be greater than 20");
        }
        return true;
    }
    
}
