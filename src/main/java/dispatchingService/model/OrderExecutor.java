package dispatchingService.model;

import dispatchingService.model.impl.Order;

public interface OrderExecutor {
    void executeOrder(Order order);

    int getId();

    boolean isFree();
}
