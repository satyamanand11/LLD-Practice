import com.lld.bms.domain.Booking;
import com.lld.bms.domain.City;
import com.lld.bms.domain.Movie;
import com.lld.bms.domain.Show;
import com.lld.bms.domain.ShowSeat;
import com.lld.bms.domain.User;
import com.lld.bms.domain.Screen;
import com.lld.bms.domain.ScreenType;
import com.lld.bms.domain.Seat;
import com.lld.bms.domain.SeatType;
import com.lld.bms.domain.Venue;
import com.lld.bms.facade.BookMyShowSystem;
import com.lld.bms.facade.BookMyShowSystemImpl;
import com.lld.bms.service.selection.AddOn;
import com.lld.bms.service.selection.LargeCola;
import com.lld.bms.service.selection.PopcornCombo;
import com.lld.bms.service.selection.RecliningUpgrade;
import com.lld.bms.service.selection.SeatSelection;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        BookMyShowSystem bms = BookMyShowSystemImpl.getInstance();

        User alice = bms.registerUser("Alice", "alice@example.com");
        User bob = bms.registerUser("Bob", "bob@example.com");
        System.out.println("Registered users: " + alice.getName() + ", " + bob.getName());

        City blr = bms.addCity("Bengaluru", "Karnataka");
        Venue pvr = bms.addVenue(blr.getId(), "PVR Orion", "Orion Mall, Rajajinagar");
        Screen audi1 = bms.addScreen(pvr.getId(), "Audi 1", ScreenType.IMAX, sampleSeats(2, 5));
        System.out.println("Created venue '" + pvr.getName() + "' with screen '" + audi1.getName()
                + "' (" + audi1.getSeats().size() + " seats)");

        Movie movie = bms.addMovie("Inception", 148, "Sci-Fi", "English");
        LocalDateTime saturdayEvening = LocalDateTime.now()
                .with(TemporalAdjusters.next(DayOfWeek.SATURDAY))
                .withHour(19).withMinute(0).withSecond(0).withNano(0);
        Show show = bms.createShow(movie.getId(), audi1.getId(),
                saturdayEvening, saturdayEvening.plusMinutes(movie.getDurationMinutes()));
        System.out.println("Created show " + show.getId() + " at " + saturdayEvening
                + " (" + saturdayEvening.getDayOfWeek() + ") for '" + movie.getTitle() + "'");

        List<ShowSeat> available = bms.listAvailableSeats(show.getId());
        System.out.println("Available seats for show: " + available.size());
        ShowSeat seat0 = available.get(0);
        ShowSeat seat1 = available.get(1);
        System.out.println("  - " + describe(seat0));
        System.out.println("  - " + describe(seat1));
        System.out.println("  (Note: tier price stored on ShowSeat; weekend +20% and surge applied at booking by PricingService)");

        List<AddOn> seat0AddOns = List.of(new LargeCola(), new PopcornCombo());
        List<AddOn> seat1AddOns = List.of(new RecliningUpgrade());
        SeatSelection sel0 = new SeatSelection(seat0.getId(), seat0AddOns);
        SeatSelection sel1 = new SeatSelection(seat1.getId(), seat1AddOns);
        List<SeatSelection> selections = List.of(sel0, sel1);

        System.out.println("Alice's selections (price computed centrally by PricingService):");
        for (SeatSelection s : selections) {
            System.out.println("  * " + s.description());
        }

        Booking aliceBooking = bms.bookSelections(alice.getId(), show.getId(), selections);
        System.out.println("Alice booking confirmed: id=" + aliceBooking.getConfirmationId()
                + ", total=" + aliceBooking.getTotalAmount()
                + ", seats=" + aliceBooking.getShowSeatIds().size());

        try {
            bms.bookSeats(bob.getId(), show.getId(),
                    List.of(seat0.getId(), seat1.getId()));
            System.out.println("ERROR: double-booking should have failed");
        } catch (IllegalStateException e) {
            System.out.println("Bob's double-booking correctly rejected: " + e.getMessage());
        }

        Booking cancelled = bms.cancelBooking(aliceBooking.getConfirmationId());
        System.out.println("Cancelled Alice's booking " + cancelled.getConfirmationId()
                + " at " + cancelled.getCancelledAt());
        System.out.println("Available seats after cancellation: " + bms.listAvailableSeats(show.getId()).size());

        Booking bobBooking = bms.bookSeats(bob.getId(), show.getId(),
                List.of(seat0.getId(), seat1.getId()));
        System.out.println("Rebooked by Bob (no add-ons): id=" + bobBooking.getConfirmationId()
                + ", total=" + bobBooking.getTotalAmount());
    }

    private static String describe(ShowSeat ss) {
        return "ShowSeat[" + ss.getId().substring(0, 8) + "...] basePrice=" + ss.getBasePrice();
    }

    private static List<Seat> sampleSeats(int rows, int cols) {
        List<Seat> seats = new ArrayList<>();
        for (int r = 1; r <= rows; r++) {
            for (int c = 1; c <= cols; c++) {
                SeatType type = (r == 1) ? SeatType.PLATINUM : SeatType.GOLD;
                seats.add(new Seat(UUID.randomUUID().toString(), r, c, type));
            }
        }
        return seats;
    }
}
