package gololang.concurrent.workers;

import org.testng.annotations.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class PortTest {

  @Test
  public void make_a_sum() throws InterruptedException {

    final AtomicInteger counter = new AtomicInteger(0);
    final int MAX = 1000;
    final ExecutorService executor = Executors.newCachedThreadPool();

    final Port receiver = new Port(executor, new WorkerFunction() {
      @Override
      public void apply(Object message) {
        counter.addAndGet((Integer) message);
      }
    });

    final Port sender = new Port(executor, new WorkerFunction() {
      @Override
      public void apply(Object message) {
        receiver.send(message);
      }
    });

    while (counter.get() < MAX) {
      sender.send(1);
    }
    executor.shutdown();
  }
}
