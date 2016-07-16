package tools;

import java.time.*;

/**
 * Created by michal on 02.07.2016.
 */
public class FunkyDateHolder {

    private LocalDate current = forString("some day");
    private Clock clock;

    public FunkyDateHolder() {
        this.clock = Clock.systemDefaultZone();
    }

    public FunkyDateHolder(Clock clock) {
        this.clock = clock;
    }

    public LocalDateTime get() {
        return current.atTime(LocalTime.now(clock));
    }

    public void set(String day) {
        current = forString(day);
    }

    private LocalDate forString(String day) {
        switch (day) {
            case "some day":
                return LocalDate.of(2016, Month.JANUARY, 8);
            case "same day":
                return current;
            case "next day":
                return current.plusDays(1);
            default:
                return LocalDate.parse(day);
        }
    }
}
