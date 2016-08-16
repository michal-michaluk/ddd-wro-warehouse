package warehouse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by michal on 16.07.2016.
 */
@RunWith(Parameterized.class)
public class PaletteLabelInvalidTest {

    private Labels labels = new TLabelsFormats(0);

    @Parameterized.Parameters(name = "label {0} parsed")
    public static Collection<Object> data() {
        return Arrays.asList(
                "", "P--0", "P-900300B-", "B-900300B-8A3", "P-900300B|8A3"
        );
    }

    private String toScan;

    public PaletteLabelInvalidTest(String toScan) {
        this.toScan = toScan;
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldByInvalid() throws Exception {
        labels.scanPalette(toScan);
    }
}
