package warehouse.products;

/**
 * Created by michal on 23.10.2017.
 */
public class StorageUnitValidatorFactory {

    public StorageUnitValidator pickValidator(String refNo) {
        // in case decision based on business rule
        // od it that way
        return new PaletteValidator();
    }
}
