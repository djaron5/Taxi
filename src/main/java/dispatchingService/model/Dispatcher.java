package dispatchingService.model;

import dispatchingService.model.impl.Order;
import org.w3c.dom.Document;

public interface Dispatcher {
    Order acceptOrder(Document order, Integer messaguId);
}
