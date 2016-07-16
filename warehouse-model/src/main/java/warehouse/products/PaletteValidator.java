package warehouse.products;

import warehouse.BoxLabel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by michal on 16.07.2016.
 */
public class PaletteValidator {
    public Set<String> validate(RegisterNew registerNew) {
        boolean notEmpty = !registerNew.getScannedBoxes().isEmpty();
        BoxLabel first = registerNew.getScannedBoxes().get(0);
        boolean sameRefAndBox = registerNew.getScannedBoxes().stream().allMatch(box ->
                first.getRefNo().equals(box.getRefNo())
                        && first.getBoxType().equals(box.getBoxType())
        );
        boolean matchingLabel = registerNew.getPaletteLabel().getRefNo().equals(first.getRefNo());
        if (notEmpty && sameRefAndBox && matchingLabel) {
            return Collections.emptySet();
        } else {
            HashSet<String> errors = new HashSet<>();
            if (!notEmpty) errors.add("Palette without boxes is cannot be registered");
            if (!sameRefAndBox) errors.add("Not all boxes have matching product");
            if (!matchingLabel) errors.add("Palette label not match box label");
            return errors;

        }
    }
}
