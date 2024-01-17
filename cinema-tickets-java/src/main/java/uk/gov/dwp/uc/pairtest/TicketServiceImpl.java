package uk.gov.dwp.uc.pairtest;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.UUID;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;

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

            if (noOfTicket < 1) {
                throw new InvalidPurchaseException("No Valid Ticket.. Number of ticket should be atleast 1");
            }

            if (typeOfTicket == Type.INFANT){
                checkTicketNumberRange(noOfTicket);
            }
            else if (typeOfTicket == Type.CHILD){
                if (checkTicketNumberRange(noOfTicket)){
                    totalAmountPaymentDue += (noOfTicket * 10);
                    totalSeatsAllocationDue += noOfTicket;
                }
            }
            else if (typeOfTicket == Type.ADULT) {
                if (checkTicketNumberRange(noOfTicket)){
                    totalAmountPaymentDue += (noOfTicket * 20);
                    totalSeatsAllocationDue += noOfTicket;
                    adultIsPresent = true;
                }
            } else {
                throw new InvalidPurchaseException("Invalid ticket - Undefined..");
            }
        }

        if(adultIsPresent){
            long accountID = generateAccountID();
            _paymentService.makePayment(accountID, totalAmountPaymentDue);
            _reservationService.reserveSeat(accountID, totalSeatsAllocationDue);    
                
        }else{
            throw new InvalidPurchaseException("Infact or Child tickets are required to have atleast one adult");
        }
    }

    public boolean checkTicketNumberRange (int noOfTicket) {
        if (noOfTicket > 20){
            throw new InvalidPurchaseException("Too Many Ticket Number Request .. Number should not be greater than 20");     
        }
        return true;
    }

    public long generateAccountID () {
        UUID uuid = UUID.randomUUID();
        long longValue = uuid.getMostSignificantBits();
        long accountid = longValue & Long.MAX_VALUE;
        return accountid;
    }

}
