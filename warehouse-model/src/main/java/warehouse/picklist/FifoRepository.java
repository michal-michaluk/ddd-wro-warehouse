package warehouse.picklist;

/**
 * Created by michal on 14.08.2016.
 */
public interface FifoRepository {
    Fifo get();

    void handle(String refNo, Object event);
}
