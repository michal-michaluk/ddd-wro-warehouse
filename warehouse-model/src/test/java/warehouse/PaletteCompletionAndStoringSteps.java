package warehouse;

import cucumber.api.PendingException;
import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.assertj.core.api.Assertions;
import org.mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by michal on 08.06.2016.
 */
public class PaletteCompletionAndStoringSteps {

    private PaletteLabel paletteLabel;
    private List<BoxScan> scannedBoxes = new ArrayList<>();

    @InjectMocks
    private BlaBlaBla object;
    @Mock
    private PreferredLocationPicker locationsPicker;
    @Mock
    private BlaBlaBla.Events events;
    @Captor
    private ArgumentCaptor<NewPaletteReadyToStore> captor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
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
        Mockito.verify(events).fire(captor.capture());
        NewPaletteReadyToStore event = captor.getValue();
        Assertions.assertThat(event.getLabel()).isEqualTo(paletteLabel);
    }

    @Then("^preferred location is proposed$")
    public void preferredLocationIsProposed() throws Throwable {
        NewPaletteReadyToStore event = captor.getValue();
        Assertions.assertThat(event.getPreferredLocation()).isNotEmpty();
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
    public void labelOfLocationAIsScannedAndPaletteIsStoredAtThatLocation(int location) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("^new location of palette is known$")
    public void newLocationOfPaletteIsKnown() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("^location is chosen palette dropped at that location$")
    public void locationIsChosenPaletteDroppedAtThatLocation() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }
}

