package gololang.concurrent.workers;

import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkersTest {

  @Test
  public void make_a_sum() throws InterruptedException {

    final AtomicInteger counter = new AtomicInteger(0);
    final int MAX = 1000;
    WorkerEnvironment environment = WorkerEnvironment.newWorkerEnvironment();

    final Port receiver = environment.spawnWorker(new WorkerFunction() {
      @Override
      public void apply(Object message) {
        counter.addAndGet((Integer) message);
      }
    });

    final Port sender = environment.spawnWorker(new WorkerFunction() {
      @Override
      public void apply(Object message) {
        receiver.send(message);
      }
    });

    while (counter.get() < MAX) {
      sender.send(1);
    }
    environment.awaitTermination(1, TimeUnit.SECONDS);
    environment.shutdown();
  }
}
