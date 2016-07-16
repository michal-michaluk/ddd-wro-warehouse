package warehouse.picklist;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import lombok.Data;
import org.assertj.core.api.Assertions;
import tools.FunkyDateHolder;
import warehouse.LabelGenerator;
import warehouse.locations.Location;

import java.util.List;

/**
 * Created by michal on 02.07.2016.
 */
public class PickingSteps {

    private PickList pickList;

    @Data
    public static class StockEntry {
        private String refNo;
        private Location location;
        private int amount;
        private String produced;
    }

    private final LabelGenerator generator = new LabelGenerator();
    private final FunkyDateHolder prodDate = new FunkyDateHolder();
    private final PickListBuilder builder = PickListBuilder.fifo();
    private final Order.OrderBuilder order = Order.builder();

    @Given("^stock of:$")
    public void stockOf(List<StockEntry> stock) throws Throwable {
        for (StockEntry entry : stock) {
            prodDate.set(entry.getProduced());
            palettesOfStoredAtLocation(entry.amount, entry.refNo,
                    entry.produced, entry.location);
        }
    }

    @Given("^(\\d+) palettes of (\\d+) produced (.+) stored at (.+) location$")
    public void palettesOfStoredAtLocation(int count, String refNo, String day, Location location) throws Throwable {
        prodDate.set(day);
        for (int i = 0; i < count; i++) {
            builder.newPalette(generator.palette(refNo), prodDate.get(), location);
        }
    }

    @When("^need to deliver (\\d+) palettes of (.+) to customer$")
    public void needToDeliverPalettesOf(int amount, String refNo) throws Throwable {
        order.add(refNo, amount);
    }

    @And("^pick list is requested$")
    public void pickListIsRequested() throws Throwable {
        Fifo fifo = builder.get();
        pickList = fifo.pickList(order.build());
    }

    @Then("^(\\d+) picks of (.+) from location (.+) are suggested")
    public void picksFromLocationAreSuggested(int amount, String refNo, Location location) throws Throwable {
        Assertions.assertThat(pickList.getPicks().stream()
                .filter(pick -> refNo.equals(pick.getPaletteLabel().getRefNo())
                        && location.equals(pick.getLocation()))
                .count()).as("%s palettes amount to pick from location %s", refNo, location)
                .isEqualTo(amount)
        ;
    }
}
