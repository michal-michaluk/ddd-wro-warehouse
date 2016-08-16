package rest.api;

import rest.api.v1.ProductStocks;
import warehouse.EventMappings;
import warehouse.Repository;
import warehouse.TLabelsFormats;

/**
 * Created by michal on 13.07.2016.
 */
public class Main {

    public static void main(String[] args) {
        new ProductStocks(new Repository(new EventMappings()), new TLabelsFormats(0)).exposeApi();
    }
}
