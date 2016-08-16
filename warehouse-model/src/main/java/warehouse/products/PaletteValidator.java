package warehouse.products;

import lombok.Value;
import warehouse.BoxLabel;

import java.beans.ConstructorProperties;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by michal on 16.07.2016.
 */
public class PaletteValidator {

    public ValidationResult isValid(RegisterNew registerNew) {
        boolean notEmpty = !registerNew.getScannedBoxes().isEmpty();
        BoxLabel first = registerNew.getScannedBoxes().get(0);
        boolean sameRefAndBox = registerNew.getScannedBoxes().stream().allMatch(box ->
                first.getRefNo().equals(box.getRefNo())
                        && first.getBoxType().equals(box.getBoxType())
        );
        boolean matchingLabel = registerNew.getPaletteLabel().getRefNo().equals(first.getRefNo());

        return new Checker()
                .check(notEmpty, "Palette without boxes is cannot be registered")
                .check(sameRefAndBox, "Not all boxes have matching product")
                .check(matchingLabel, "Palette label not match box label")
                .build();
    }

    @Value
    public static class ValidationResult {
        private final Set<String> violations;

        public static ValidationResult valid() {
            return new ValidationResult(Collections.emptySet());
        }

        public boolean isValid() {
            return violations.isEmpty();
        }

        @ConstructorProperties({"violations"})
        private ValidationResult(Set<String> violations) {
            this.violations = Collections.unmodifiableSet(violations);
        }
    }

    private static class Checker {
        private final Set<String> violations = new HashSet<>();

        public Checker check(boolean passed, String message) {
            if (!passed) violations.add(message);
            return this;
        }

        public ValidationResult build() {
            return new ValidationResult(violations);
        }
    }
}
