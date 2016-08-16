package warehouse.picklist;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import lombok.Data;
import org.assertj.core.api.Assertions;
import tools.FunkyDateHolder;
import warehouse.Labels;
import warehouse.PaletteLabel;
import warehouse.TLabelsFormats;
import warehouse.locations.Location;

import java.util.List;

/**
 * Created by michal on 02.07.2016.
 */
public class FifoPickingSteps {

    private PickList pickList;

    @Data
    public static class StockEntry {
        private String refNo;
        private Location location;
        private int amount;
        private String produced;
    }

    @Data
    public static class Palette {
        private String label;
        private Location location;
        private String produced;
    }

    private final Labels generator = new TLabelsFormats(0);
    private final FunkyDateHolder prodDate = new FunkyDateHolder();
    private final FifoBuilder.History builder = FifoBuilder.builder().history();
    private final Order.OrderBuilder order = Order.builder();

    @Given("^stock of:$")
    public void stockOf(List<StockEntry> stock) throws Throwable {
        for (StockEntry entry : stock) {
            palettesOfStoredAtLocation(entry.amount, entry.refNo,
                    entry.produced, entry.location);
        }
    }

    @Given("^(\\d+) palettes of (\\d+) produced (.+) stored at (.+) location$")
    public void palettesOfStoredAtLocation(int count, String refNo, String day, Location location) throws Throwable {
        prodDate.set(day);
        for (int i = 0; i < count; i++) {
            builder.newPalette(generator.newPalette(refNo), prodDate.get(), location);
        }
    }

    @Given("^palettes:$")
    public void palettes(List<Palette> palettes) throws Throwable {
        for (Palette entry : palettes) {
            paletteStoredAtLocation(generator.scanPalette(entry.label),
                    entry.produced, entry.location);
        }
    }

    @Given("^palette (\\d+) produced (.+) stored at (.+) location$")
    public void paletteStoredAtLocation(PaletteLabel label, String day, Location location) {
        prodDate.set(day);
        builder.newPalette(label, prodDate.get(), location);
    }

    @Given("^palette (.+) is delivered$")
    public void paletteFromAIsDelivered(String paletteLabel) throws Throwable {
        builder.delivered(generator.scanPalette(paletteLabel));
    }

    @Given("^palette (.+) is locked$")
    public void paletteFromAIsLocked(String paletteLabel) throws Throwable {
        builder.locked(generator.scanPalette(paletteLabel));
    }

    @Given("^palette (.+) is unlocked$")
    public void paletteFromAIsUnlocked(String paletteLabel) throws Throwable {
        builder.unlocked(generator.scanPalette(paletteLabel));
    }

    @Given("^palette (.+) is destroyed$")
    public void paletteFromAIsDestroyed(String paletteLabel) throws Throwable {
        builder.destroyed(generator.scanPalette(paletteLabel));
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
