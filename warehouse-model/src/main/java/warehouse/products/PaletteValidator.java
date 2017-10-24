package warehouse.products;

import warehouse.BoxLabel;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by michal on 23.10.2017.
 */
public class PaletteValidator implements StorageUnitValidator {

    @Override
    public ValidationResult validate(RegisterNew registerNew) {
        Set<String> violations = new HashSet<>();
        BoxLabel first = registerNew.getScannedBoxes().get(0);

        if (registerNew.getScannedBoxes().isEmpty()) {
            violations.add("Palette without boxes is cannot be registered");
        }
        if (!registerNew.getScannedBoxes().stream().allMatch(box ->
                first.getRefNo().equals(box.getRefNo())
                        && first.getBoxType().equals(box.getBoxType()))) {
            violations.add("Not all boxes have matching product");
        }
        if (!registerNew.getPaletteLabel().getRefNo().equals(first.getRefNo())) {
            violations.add("Palette label not match box label");
        }
        return new ValidationResult(violations);
    }
}
