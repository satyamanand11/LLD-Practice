import com.lld.ticketservice.decorator.DefaultSeatComponent;
import com.lld.ticketservice.decorator.ReclinerDecorator;
import com.lld.ticketservice.decorator.SeatComponent;
import com.lld.ticketservice.decorator.SofaDecorator;
import com.lld.ticketservice.domain.booking.Booking;
import com.lld.ticketservice.domain.event.Movie;
import com.lld.ticketservice.domain.seat.Seat;
import com.lld.ticketservice.domain.seat.SeatType;
import com.lld.ticketservice.domain.show.Show;
import com.lld.ticketservice.domain.ticket.Ticket;
import com.lld.ticketservice.facade.TicketSystem;
import com.lld.ticketservice.locking.InMemorySeatLockProvider;
import com.lld.ticketservice.locking.SeatLockProvider;
import com.lld.ticketservice.managers.booking.BookingManager;
import com.lld.ticketservice.managers.booking.BookingManagerImpl;
import com.lld.ticketservice.managers.event.EventManager;
import com.lld.ticketservice.managers.event.EventManagerImpl;
import com.lld.ticketservice.managers.seat.SeatManager;
import com.lld.ticketservice.managers.seat.SeatManagerImpl;
import com.lld.ticketservice.managers.show.ShowManager;
import com.lld.ticketservice.managers.show.ShowManagerImpl;
import com.lld.ticketservice.managers.ticket.TicketManager;
import com.lld.ticketservice.managers.ticket.TicketManagerImpl;
import com.lld.ticketservice.pricing.DemandTierPricingStrategy;
import com.lld.ticketservice.pricing.PricingStrategy;
import com.lld.ticketservice.services.BookingService;
import com.lld.ticketservice.services.PricingService;
import com.lld.ticketservice.services.TicketService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        EventManager eventMgr = new EventManagerImpl();
        ShowManager showMgr = new ShowManagerImpl();
        SeatManager seatMgr = new SeatManagerImpl();
        BookingManager bookingMgr = new BookingManagerImpl();
        TicketManager ticketMgr = new TicketManagerImpl();

        Movie movie = new Movie(101, "Dune: Part Two", "Sci-Fi", "English");
        eventMgr.addEvent(movie);

        int venueId = 1, screenNo = 1;
        List<Seat> layout = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            SeatComponent base = new DefaultSeatComponent(200, "A" + i);
            SeatComponent decorated = (i <= 2)
                    ? new ReclinerDecorator(new SofaDecorator(base))
                    : base;
            layout.add(new Seat(i, (i <= 2) ? SeatType.VIP : SeatType.REGULAR, decorated));
        }
        seatMgr.registerSeats(venueId, screenNo, layout);

        Show show = new Show(1001, venueId, screenNo, movie.getId(), layout.size());
        showMgr.addShow(show);

        SeatLockProvider locks = new InMemorySeatLockProvider(showMgr.getShowMap());
        PricingStrategy strategy = new DemandTierPricingStrategy();
        PricingService pricing = new PricingService(strategy, seatMgr, showMgr);
        BookingService booking = new BookingService(showMgr, locks, bookingMgr);
        TicketService ticketService = new TicketService(ticketMgr);

        // Singleton init (thread safe)
        TicketSystem.init(eventMgr, showMgr, pricing, booking, ticketService);
        TicketSystem ticketSystem = TicketSystem.getInstance();

        Map<Integer, Integer> quote = ticketSystem.quoteSeats(show.getShowId(), List.of(1, 2));
        System.out.println("Quote for A1,A2: " + quote);

        // ==== Concurrency demo: no double booking ====
        ExecutorService exec = Executors.newFixedThreadPool(2);
        Runnable user1 = () -> {
            try {
                Booking b = ticketSystem.reserve("User1", show.getShowId(), List.of(1, 2), 4000);
                sleep(250);
                Ticket t = ticketSystem.confirmAndIssue(b, "User1");
                System.out.println("User1 OK: ticket=" + t.getTicketId() + " price=" + t.getPrice());
            } catch (Exception e) {
                System.out.println("User1 FAILED: " + e.getMessage());
            }
        };
        Runnable user2 = () -> {
            try {
                Booking b = ticketSystem.reserve("User2", show.getShowId(), List.of(1, 2), 4000);
                Ticket t = ticketSystem.confirmAndIssue(b, "User2");
                System.out.println("User2 OK: ticket=" + t.getTicketId() + " price=" + t.getPrice());
            } catch (Exception e) {
                System.out.println("User2 FAILED: " + e.getMessage());
            }
        };
        exec.submit(user1);
        exec.submit(user2);
        exec.shutdown();
        exec.awaitTermination(5, TimeUnit.SECONDS);

        ticketSystem.printShowState(show.getShowId());
    }


    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }

}