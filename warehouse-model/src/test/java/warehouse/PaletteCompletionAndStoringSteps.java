package warehouse;

import cucumber.api.PendingException;
import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import warehouse.locations.Location;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;

/**
 * Created by michal on 08.06.2016.
 */
public class PaletteCompletionAndStoringSteps {

    private PaletteLabel paletteLabel;
    private List<BoxScan> scannedBoxes = new ArrayList<>();

    private ProductStock object;
    @Mock
    private PreferredLocationPicker locationsPicker;
    private EventsAssert events = new EventsAssert();
    private Clock clock = Clock.systemDefaultZone();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.when(
                locationsPicker.suggestLocationFor(any(PaletteLabel.class))
        ).thenReturn(new Location("A-32-3"));
        object = new ProductStock("900300", events, locationsPicker, clock);
    }

    @When("^label for new palette is printed$")
    public void commandToPrintPaletteLabel() throws Throwable {
        paletteLabel = new PaletteLabel("", "900300");
    }

    @When("^box is scanned$")
    public void boxIsScanned() throws Throwable {
        scannedBoxes.add(new BoxScan("900300", 25, "1"));
    }

    @When("^palette label is scanned$")
    public void paletteLabelIsScanned() throws Throwable {
        object.completeNewPalette(
                new CompleteNewPalette(paletteLabel, scannedBoxes)
        );
    }

    @Then("^new palette is ready to store$")
    public void newPaletteIsReadyToStore() throws Throwable {
        events.assertFirst(NewPaletteReadyToStore.class)
                .isInstanceOf(NewPaletteReadyToStore.class)
                .extracting(NewPaletteReadyToStore::getLabel)
                .containsOnly(paletteLabel);
    }

    @Then("^preferred location is proposed$")
    public void preferredLocationIsProposed() throws Throwable {
        events.assertFirst(NewPaletteReadyToStore.class)
                .isInstanceOf(NewPaletteReadyToStore.class)
                .extracting(NewPaletteReadyToStore::getPreferredLocation)
                .containsOnly(new Location("A-32-3"));
    }

    @When("^box amount (\\d+)$")
    public void boxAmount(int amount) throws Throwable {
        if (scannedBoxes.isEmpty()) {
            throw new IllegalStateException("No box scanned till now, can't set amount " + amount);
        }
        BoxScan last = scannedBoxes.get(scannedBoxes.size() - 1);
        scannedBoxes.addAll(Collections.nCopies(amount, last));
    }

    @When("^partial box is scanned$")
    public void partialBoxIsScanned() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("^label of preferred location is scanned and palette is stored at that location$")
    public void labelOfPreferredLocationIsScannedAndPaletteIsStoredAtThatLocation() throws Throwable {
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

    @When("^location is chosen palette dropped at that location$")
    public void locationIsChosenPaletteDroppedAtThatLocation() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("^palette is on location (.+)$")
    public void paletteIsOnLocationA(Location location) throws Throwable {
        events.assertLast(Stored.class)
                .extracting(Stored::getPaletteLabel, Stored::getLocation)
                .containsExactly(paletteLabel, location);
    }

    @Then("^palette is on the move$")
    public void paletteIsOnTheMove() throws Throwable {
        events.assertLast(Picked.class)
                .extracting(Picked::getPaletteLabel)
                .containsExactly(paletteLabel);
    }
}
