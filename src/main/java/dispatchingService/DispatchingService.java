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
    private ExecutorService driverTaskProcessorExecutor;

    private Runnable driverTaskProcessor;

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

        try {
            int id = result.get();

            startDriverTaskProcessor();
            return id;
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

        driverTaskProcessorExecutor = Executors.newSingleThreadExecutor();
    }

    private Runnable getDriverTaskProcessor() {
        if (driverTaskProcessor == null)
            driverTaskProcessor = () -> {
                while (!orders.isEmpty()) {
                    try {
                        Order order = orders.remove();
                        OrderExecutor executor = freeExecutors.remove(order.getTargetId());
                        if (executor != null) {
                            usedExecutors.put(executor.getId(), executor);
                            //если нашли подходящего водителя, то отправляем его работать в отдельном потоке
                            cachedThreadPool.submit(() -> {
                                executor.executeOrder(order);
                            });
                        } else {
                            orders.add(order);
                            freeExecutors.putAll(usedExecutors.entrySet().stream()
                                    .filter(x -> x.getValue().isFree())
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

        return driverTaskProcessor;
    }

    private void startDriverTaskProcessor() {
        driverTaskProcessorExecutor.submit(getDriverTaskProcessor());
    }
}
