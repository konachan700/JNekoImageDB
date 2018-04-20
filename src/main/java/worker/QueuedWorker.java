package worker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public abstract class QueuedWorker<T> {
    private final LinkedBlockingDeque<T> queue;
    private final ExecutorService executor;

    public LinkedBlockingDeque<T> getQueue() {
        return queue;
    }

    private final class Worker implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    final T t = getQueue().pollFirst(9999, TimeUnit.DAYS);
                    if (t != null) threadEvent(t);
                } catch (InterruptedException e) {
                    //System.out.println("InterruptedException " + Thread.currentThread().getName());
                    return;
                }
            }
        }
    }

    public abstract void threadEvent(T t);

    public QueuedWorker(int threads, int queueSize) {
        queue = new LinkedBlockingDeque<>(queueSize);
        executor = Executors.newFixedThreadPool(threads);
        for (int i=0; i<threads; i++) {
            final Worker worker = new Worker();
            final Thread thread = new Thread(worker, "worker"+i);
            executor.submit(thread);
        }
    }

    public QueuedWorker(int queueSize) {
        queue = new LinkedBlockingDeque<>(queueSize);

        int threads = Runtime.getRuntime().availableProcessors();
        if (threads <= 0) throw new IllegalStateException("cannot get CPUs count");

        executor = Executors.newFixedThreadPool(threads);
        for (int i=0; i<threads; i++) {
            final Worker worker = new Worker();
            final Thread thread = new Thread(worker, "worker"+i);
            executor.submit(thread);
        }
    }

    public QueuedWorker() {
        queue = new LinkedBlockingDeque<>();

        int threads = Runtime.getRuntime().availableProcessors();
        if (threads <= 0) throw new IllegalStateException("cannot get CPUs count");

        executor = Executors.newFixedThreadPool(threads);
        for (int i=0; i<threads; i++) {
            final Worker worker = new Worker();
            final Thread thread = new Thread(worker, "worker"+i);
            executor.submit(thread);
        }
    }

    public void pushTask(T t) {
        try {
            getQueue().putFirst(t);
        } catch (InterruptedException e) { }
    }

    public void dispose() {
        executor.shutdownNow();
    }
}
