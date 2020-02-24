package dispatchingService.model.impl;

import dispatchingService.model.OrderExecutor;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Driver implements OrderExecutor {
    private int id;
    private boolean isFree = true;
    private Path pathToFolder; //target/classes/driversFolder/{driverId}

    public Driver(int id) {
        this.id = id;

        try {
            String source = Paths.get(this.getClass().getResource("/driversFolder/")
                    .toURI()).toString();
            pathToFolder = Paths.get(source + "\\" + this.id + "\\");
            new File(pathToFolder.toString()).mkdir();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public synchronized void executeOrder(Order order) {
        isFree = false;
        try {
            doWork(order);
            System.out.println("Driver with id " + id + " execute order " + order.getDispatchedId() + " and sleep");
            Thread.sleep(3000); //какая-то работа
            isFree = true;

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //сохраняет файл по пути driversFolder/{driverId}/{order.dispatchedId}.xml
    private void doWork(Order order) {
        try {
            Path pathToFile = Paths.get(pathToFolder.toString() + "/" + order.getDispatchedId() + ".xml");

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(order.getDocument());
            FileWriter writer = new FileWriter(new File(pathToFile.toString()));
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public int getId() {
        return id;
    }

    public boolean isFree() {
        return isFree;
    }
}