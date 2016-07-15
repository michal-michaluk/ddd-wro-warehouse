package warehouse;

import cucumber.api.PendingException;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import lombok.Data;
import tools.FunkyDateHolder;

import java.util.List;

/**
 * Created by michal on 02.07.2016.
 */
public class PickingSteps {

    @Data
    public static class StockEntry {
        private String refNo;
        private String location;
        private int amount;
        private String produced;
    }

    private final FunkyDateHolder prodDate = new FunkyDateHolder();

    @Given("^stock of:$")
    public void stockOf(List<StockEntry> stock) throws Throwable {
        for (StockEntry entry : stock) {
            prodDate.set(entry.getProduced());
        }
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Given("^(\\d+) palettes of (\\d+) produced (.+) stored at (.+) location$")
    public void palettesOfStoredAtLocation(int count, String refNo, String day, String location) throws Throwable {
        prodDate.set(day);
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("^need to deliver (\\d+) palettes of (.+) to customer$")
    public void needToDeliverPalettesOf(int count, String refNo) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("^(\\d+) picks of (.+) from location (.+) are suggested")
    public void picksFromLocationAreSuggested(int count, String refNo, String location) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }
}
