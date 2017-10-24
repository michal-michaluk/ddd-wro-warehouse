package warehouse.products;

import lombok.Value;
import warehouse.BoxLabel;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

/**
 * Created by michal on 23.10.2017.
 */
public class CartValidator implements StorageUnitValidator {

    private final String refNo;
    private final Map<String, Integer> requiredPositions;

    public CartValidator(String refNo, List<String> requiredProducts) {
        this.refNo = refNo;
        this.requiredPositions = Collections.unmodifiableMap(
                IntStream.range(0, requiredProducts.size()).boxed()
                        .collect(toMap(requiredProducts::get, Function.identity()))
        );
    }

    @Override
    public ValidationResult validate(RegisterNew registerNew) {
        Stream<String> notExpected = scanned(registerNew)
                .filter(Scanned::isViolated)
                .map(Scanned::violation);

        Stream<String> notFound = notFound(registerNew)
                .map(this::notFoundMessage);

        return new ValidationResult(
                Stream.concat(notFound, notExpected)
                        .collect(Collectors.toSet())
        );
    }

    private Stream<String> notFound(RegisterNew registerNew) {
        Set<String> actual = scannedRefNos(registerNew).collect(Collectors.toSet());
        return expected().filter(expected -> !actual.contains(expected));
    }

    private Stream<String> scannedRefNos(RegisterNew registerNew) {
        return registerNew.getScannedBoxes().stream()
                .map(BoxLabel::getRefNo);
    }

    private Stream<String> expected() {
        return requiredPositions.keySet().stream();
    }

    private Stream<Scanned> scanned(RegisterNew registerNew) {
        return IntStream.range(0, registerNew.getScannedBoxes().size())
                .mapToObj(index -> scannedAtIndex(registerNew, index));
    }

    private Scanned scannedAtIndex(RegisterNew registerNew, int index) {
        String refNo = registerNew.getScannedBoxes().get(index).getRefNo();
        return new Scanned(refNo, index, requiredPositions.getOrDefault(refNo, null));
    }

    @Value
    private static class Scanned {
        String refNo;
        int actualIndex;
        Integer expectedIndex;

        boolean isViolated() {
            return !Objects.equals(actualIndex, expectedIndex);
        }

        String violation() {
            return expectedIndex == null
                    ? notExpectedMessage()
                    : expectedInOtherPocketMessage();
        }

        private String expectedInOtherPocketMessage() {
            return "refNo " + refNo + " expected in pocket " + position(expectedIndex) + ", but found in " + position(actualIndex);
        }

        private String notExpectedMessage() {
            return "not expected refNo " + refNo;
        }
    }

    private String notFoundMessage(String refNo) {
        return "refNo " + refNo + " expected in pocket " + position(requiredPositions.get(refNo)) + ", but not found";
    }

    private static int position(int index) {
        return index + 1;
    }
}
