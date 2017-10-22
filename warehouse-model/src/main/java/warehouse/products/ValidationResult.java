package warehouse.products;

import lombok.Value;

import java.beans.ConstructorProperties;
import java.util.Collections;
import java.util.Set;

/**
 * Created by michal on 22.10.2017.
 */
@Value
public class ValidationResult {
    private final Set<String> violations;

    public static ValidationResult valid() {
        return new ValidationResult(Collections.emptySet());
    }

    public boolean isValid() {
        return violations.isEmpty();
    }

    @ConstructorProperties({"violations"})
    ValidationResult(Set<String> violations) {
        this.violations = Collections.unmodifiableSet(violations);
    }
}
