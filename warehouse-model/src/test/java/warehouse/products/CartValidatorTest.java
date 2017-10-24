package warehouse.products;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import warehouse.BoxLabel;
import warehouse.PaletteLabel;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by michal on 23.10.2017.
 */
public class CartValidatorTest {

    private final String cartRefNo = "900900";
    private final PaletteLabel cartLabel = new PaletteLabel("C-900900-0001", cartRefNo);

    private CartValidator validator;

    @Test
    public void noViolationsForValidCart() throws Exception {
        requiredProductsInCart("900901", "900902", "900903", "900904");
        RegisterNew command = givenCartRegistrationCommand("900901", "900902", "900903", "900904");

        ValidationResult result = validator.validate(command);

        Assertions.assertThat(result.getViolations()).isEmpty();
    }

    @Test
    public void singleProductMissingInCart() throws Exception {
        requiredProductsInCart("900901", "900902", "900903", "900904");
        RegisterNew command = givenCartRegistrationCommand("900902", "900903", "900904");

        ValidationResult result = validator.validate(command);

        Assertions.assertThat(result.getViolations())
                .contains("refNo 900901 expected in pocket 1, but not found");
    }

    @Test
    public void multipleProductMissingInCart() throws Exception {
        requiredProductsInCart("900901", "900902", "900903", "900904");
        RegisterNew command = givenCartRegistrationCommand("900901", "900903");

        ValidationResult result = validator.validate(command);

        Assertions.assertThat(result.getViolations())
                .contains(
                        "refNo 900904 expected in pocket 4, but not found",
                        "refNo 900902 expected in pocket 2, but not found");
    }

    @Test
    public void notExpectedProductInCart() throws Exception {
        requiredProductsInCart("900901", "900902", "900903", "900904");
        RegisterNew command = givenCartRegistrationCommand("900901", "9009XXXX", "900902", "900903", "900904");

        ValidationResult result = validator.validate(command);

        Assertions.assertThat(result.getViolations())
                .contains("not expected refNo 9009XXXX");
    }

    @Test
    public void notExpectedMultipleProductsInCart() throws Exception {
        requiredProductsInCart("900901", "900902", "900903", "900904");
        RegisterNew command = givenCartRegistrationCommand("900901", "9009XXXX", "900902", "900903", "900904", "9009YYYY");

        ValidationResult result = validator.validate(command);

        Assertions.assertThat(result.getViolations())
                .contains(
                        "not expected refNo 9009XXXX",
                        "not expected refNo 9009YYYY");
    }

    @Test
    public void someProductMissingSomeProductNotExpected() throws Exception {
        requiredProductsInCart("900901", "900902", "900903", "900904");
        RegisterNew command = givenCartRegistrationCommand("9009XXXX", "900902", "900903", "900904");

        ValidationResult result = validator.validate(command);

        Assertions.assertThat(result.getViolations())
                .contains(
                        "not expected refNo 9009XXXX",
                        "refNo 900901 expected in pocket 1, but not found");
    }

    @Test
    public void someProductExpectedInOtherPocket() throws Exception {
        requiredProductsInCart("900901", "900902", "900903", "900904");
        RegisterNew command = givenCartRegistrationCommand("900902", "900901", "900903", "900904");

        ValidationResult result = validator.validate(command);

        Assertions.assertThat(result.getViolations())
                .contains(
                        "refNo 900901 expected in pocket 1, but found in 2",
                        "refNo 900902 expected in pocket 2, but found in 1");

    }

    private RegisterNew givenCartRegistrationCommand(String... labels) {
        return new RegisterNew(cartLabel, Stream.of(labels)
                .map(l -> new BoxLabel(l, 1, "POCKET"))
                .collect(Collectors.toList())
        );
    }

    private void requiredProductsInCart(String... refNos) {
        validator = new CartValidator(cartRefNo, Arrays.asList(refNos));
    }
}
