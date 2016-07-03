package tools;

import lombok.Getter;

import java.time.LocalDate;
import java.time.Month;

/**
 * Created by michal on 02.07.2016.
 */
public class FunkyDateHolder {

    private LocalDate current;

    public LocalDate get() {
        return current;
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
