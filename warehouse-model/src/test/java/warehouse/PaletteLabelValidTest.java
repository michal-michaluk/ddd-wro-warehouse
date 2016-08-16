package warehouse;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by michal on 16.07.2016.
 */
@RunWith(Parameterized.class)
public class PaletteLabelValidTest {

    private Labels labels = new TLabelsFormats(0);

    @Parameterized.Parameters(name = "label {0} parsed")
    public static Collection<Object> data() {
        return Arrays.asList(
                "P-900300B-8A3", "P-900300-0", "P-900300B-00000", "P-900300B-XXXX", "P-9003-8A3", "P-*&^%$#-&%$"
        );
    }

    private String toScan;

    public PaletteLabelValidTest(String toScan) {
        this.toScan = toScan;
    }

    @Test
    public void shouldByValid() throws Exception {
        PaletteLabel paletteLabel = labels.scanPalette(toScan);
        Assertions.assertThat(paletteLabel.getRefNo()).isNotEmpty();
        Assertions.assertThat(paletteLabel.getId())
                .isEqualTo(toScan)
                .startsWith("P-" + paletteLabel.getRefNo() + "-")
                .doesNotEndWith("-");
    }
}