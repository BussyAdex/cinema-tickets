import java.util.Scanner;
import java.util.UUID;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationService;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.TicketService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class App {
    public static void main( String [] args){
        TicketPaymentService paymentService = new TicketPaymentServiceImpl();
        SeatReservationService reservationService = new SeatReservationServiceImpl();
        TicketService ticketService = new TicketServiceImpl(paymentService, reservationService);

        Scanner scanner = new Scanner(System.in);
            try {
                // Display the menu to the user
                System.out.println("========== Ticket Booking System ==========");
                System.out.println("1. Purchase Tickets");
                System.out.println("2. Exit");
                System.out.print("Select an option (1/2): ");
                int option = scanner.nextInt();

                if (option == 1) {
                    System.out.println("========== Ticket Purchase ==========");
                    System.out.println("           Type 0 for none          ");
                    System.out.print("Enter the number of Adult tickets: ");
                    int adultTickets = scanner.nextInt();

                    System.out.print("Enter the number of Child tickets: ");
                    int childTickets = scanner.nextInt();

                    System.out.print("Enter the number of Infant tickets: ");
                    int infantTickets = scanner.nextInt();
                    scanner.close();

                    UUID uuid = UUID.randomUUID();
                    long longValue = uuid.getMostSignificantBits();
                    long accountId = longValue & Long.MAX_VALUE;
                
                    TicketTypeRequest adultTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, adultTickets);
                    TicketTypeRequest childTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, childTickets);
                    TicketTypeRequest infantTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, infantTickets);

                    try {
                        ticketService.purchaseTickets(accountId, adultTicketRequest, childTicketRequest, infantTicketRequest);
                        System.out.println("Tickets purchased successfully!");
                    } 
                    catch (InvalidPurchaseException e) {
                        System.err.println("Error: " + e.getMessage());
                    }

                } else if (option == 2) {
                    System.out.println("Exiting the application.");
                } else {
                    System.out.println("Invalid option. Please select 1 or 2.");
                }
            }
             catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                scanner.nextLine();
            }
            scanner.close(); 
        }
        
    }
     



