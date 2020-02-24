package dispatchingService.model.impl;

import dispatchingService.model.Dispatcher;
import org.w3c.dom.Document;

public class PhoneDispatcher implements Dispatcher {
    private int id;

    public PhoneDispatcher(int id) {
        this.id = id;
    }

    public Order acceptOrder(Document message, Integer messageId) {
        Order order = new Order(message);
        order.injectDispatchedId(messageId);
        System.out.println("Dispatcher with id " + id + " accepted order with dispatchedId " + order.getDispatchedId());
        return order;
    }
}
