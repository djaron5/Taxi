package dispatchingService.model.impl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Order {
    private int targetId;
    private int dispatchedId;
    private Document document;

    public Order(Document document) {
        this.document = document;

        Element target = (Element) document.getElementsByTagName("target").item(0);
        targetId = Integer.parseInt(target.getAttribute("id"));
    }

    public void injectDispatchedId(int dispatchedId) {
        Element dispatched = document.createElement("dispatched");
        dispatched.setAttribute("id", Integer.toString(dispatchedId));

        Element message = (Element) document.getElementsByTagName("message").item(0);
        message.appendChild(dispatched);
        this.dispatchedId = dispatchedId;
    }

    public int getTargetId() {
        return targetId;
    }

    public Document getDocument() {
        return document;
    }

    public int getDispatchedId() {
        return dispatchedId;
    }
}
