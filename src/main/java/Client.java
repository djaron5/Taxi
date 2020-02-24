import dispatchingService.DispatchingService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Random;

public class Client {

    public static void main(String[] args) {
        int countOfDispatches = 100;
        int countOfDrivers = 10;

        DispatchingService dispatchingService = new DispatchingService(countOfDispatches, countOfDrivers);

        for (int i = 0; i < 100; i++) {
            new Thread(() -> {
                for (int j = 0; j < 10; j++) {
                    int id = dispatchingService.acceptOrder(getDoc(0, countOfDrivers - 1));
                    if (id == -1) throw new RuntimeException("-1 value");
                }
            }).start();
        }
    }

    private static Document getDoc(int minDriverId, int maxDriverId) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            Element message = document.createElement("message");
            Element target = document.createElement("target");
            target.setAttribute("id", Integer.toString(getRandomNumberInRange(minDriverId, maxDriverId)));
            Element sometags = document.createElement("sometags");
            Element data1 = document.createElement("data");
            Element data2 = document.createElement("data");
            Element data3 = document.createElement("data");

            sometags.appendChild(data1);
            sometags.appendChild(data2);
            sometags.appendChild(data3);


            message.appendChild(target);
            message.appendChild(sometags);

            document.appendChild(message);

            return document;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }
}
