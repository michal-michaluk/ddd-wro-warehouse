package warehouse.products;

import cucumber.api.PendingException;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.assertj.core.api.Assertions;
import warehouse.BoxLabel;
import warehouse.PaletteLabel;
import warehouse.TLabelsFormats;
import warehouse.locations.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static warehouse.products.ProductStockBuilder.l;

/**
 * Created by michal on 08.06.2016.
 */
public class RegistrationAndStoringSteps {

    private PaletteLabel paletteLabel;
    private List<BoxLabel> scannedBoxes = new ArrayList<>();

    private ProductStock object;
    private EventsAssert events = new EventsAssert();
    private TLabelsFormats generator = new TLabelsFormats(0);

    @Before
    public void setUp() throws Exception {
        object = ProductStockBuilder.forRefNo("900300")
                .validator(new PaletteValidator())
                .locationsPicker(l("900300", new Location("A-32-3")))
                .events(events)
                .build();
    }

    @Given("^label for new palette is printed$")
    public void commandToPrintPaletteLabel() throws Throwable {
        paletteLabel = generator.newPalette("900300");
    }

    @Given("^box is scanned$")
    public void boxIsScanned() throws Throwable {
        scannedBoxes.add(new BoxLabel("900300", 25, "1"));
    }

    @When("^palette label is scanned$")
    public void paletteLabelIsScanned() throws Throwable {
        object.registerNew(
                new RegisterNew(paletteLabel, scannedBoxes)
        );
    }

    @Then("^new palette is ready to store$")
    public void newPaletteIsReadyToStore() throws Throwable {
        events.assertFirst(ReadyToStore.class)
                .isInstanceOf(ReadyToStore.class)
                .extracting(ReadyToStore::getPaletteLabel)
                .containsOnly(paletteLabel);
    }

    @Then("^preferred location is proposed$")
    public void preferredLocationIsProposed() throws Throwable {
        events.assertFirst(ReadyToStore.class)
                .isInstanceOf(ReadyToStore.class)
                .extracting(ReadyToStore::getPreferredLocation)
                .containsOnly(new Location("A-32-3"));
    }

    @When("^box amount (\\d+)$")
    public void boxAmount(int amount) throws Throwable {
        if (scannedBoxes.isEmpty()) {
            throw new IllegalStateException("No box scanned till now, can't set amount " + amount);
        }
        BoxLabel last = scannedBoxes.get(scannedBoxes.size() - 1);
        scannedBoxes.addAll(Collections.nCopies(amount, last));
    }

    @Given("^partial box is scanned$")
    public void partialBoxIsScanned() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("^label of location (.+) is scanned and palette is stored at that location$")
    public void labelOfLocationAIsScannedAndPaletteIsStoredAtThatLocation(Location location) throws Throwable {
        object.store(new Store(paletteLabel, location));
    }

    @When("^palette is picked by user (.+)$")
    public void paletteIsPickedByUser(String user) throws Throwable {
        object.pick(new Pick(paletteLabel, user));
    }

    @Then("^palette is on location (.+)$")
    public void paletteIsOnLocationA(Location location) throws Throwable {
        events.assertLast(Stored.class)
                .extracting(Stored::getPaletteLabel, Stored::getLocation)
                .containsExactly(paletteLabel, location);
        Assertions.assertThat(object.getLocation(paletteLabel))
                .isEqualTo(location);
    }

    @Then("^palette is on the move$")
    public void paletteIsOnTheMove() throws Throwable {
        events.assertLast(Picked.class)
                .extracting(Picked::getPaletteLabel)
                .containsExactly(paletteLabel);
        Assertions.assertThat(object.getLocation(paletteLabel).getLocation())
                .startsWith("picked");
    }
}
