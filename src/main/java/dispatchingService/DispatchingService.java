package dispatchingService;

import dispatchingService.model.Dispatcher;
import dispatchingService.model.OrderExecutor;
import dispatchingService.model.impl.Driver;
import dispatchingService.model.impl.Order;
import dispatchingService.model.impl.PhoneDispatcher;
import org.w3c.dom.Document;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class DispatchingService {
    private AtomicInteger messageCounter = new AtomicInteger(0);
    private ExecutorService executorService;
    private ExecutorService cachedThreadPool;
    private Thread driverTaskProcessor;

    private ConcurrentLinkedQueue<Dispatcher> dispatchers = new ConcurrentLinkedQueue<>();
    private ConcurrentHashMap<Integer, OrderExecutor> freeExecutors = new ConcurrentHashMap();
    private ConcurrentHashMap<Integer, OrderExecutor> usedExecutors = new ConcurrentHashMap();
    private ConcurrentLinkedQueue<Order> orders = new ConcurrentLinkedQueue();

    public int acceptOrder(Document message) {
        //свободный диспетчер принимает заказ, возвращает присвоенный id и становится в конец очереди на принятие заказа
        Future<Integer> result = executorService.submit(() -> {
            Dispatcher dispatcher = dispatchers.remove();
            Order order = dispatcher.acceptOrder(message, messageCounter.incrementAndGet());
            dispatchers.add(dispatcher);
            orders.add(order);
            return order.getDispatchedId();
        });

        startDriverTaskProcessorIfNecessary();

        try {
            return result.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public DispatchingService(int countOfDispatchers, int countOfDrivers) {
        cachedThreadPool = Executors.newCachedThreadPool();
        executorService = Executors.newFixedThreadPool(countOfDispatchers);

        for (int i = 0; i < countOfDispatchers; i++) {
            dispatchers.add(new PhoneDispatcher(i));
        }

        for (int i = 0; i < countOfDrivers; i++) {
            freeExecutors.put(i, new Driver(i));
        }

        driverTaskProcessor = new Thread(getDriverTaskProcessor());
        driverTaskProcessor.start();
    }

    private Runnable getDriverTaskProcessor() {
        return () -> {
            while (!orders.isEmpty()) {
                try {
                    cachedThreadPool.submit(() -> {

                        Order order = orders.remove();
                        OrderExecutor executor = freeExecutors.remove(order.getTargetId());
                        if (executor != null) {
                            usedExecutors.put(executor.getId(), executor);
                            executor.executeOrder(order);
                        } else {
                            orders.add(order);
                            freeExecutors.putAll(usedExecutors.entrySet().stream()
                                    .filter(x -> x.getValue().isFree())
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private void startDriverTaskProcessorIfNecessary() {
        if (!driverTaskProcessor.isAlive() && !orders.isEmpty()) {
            driverTaskProcessor = new Thread(getDriverTaskProcessor());
            driverTaskProcessor.start();
        }
    }
}
