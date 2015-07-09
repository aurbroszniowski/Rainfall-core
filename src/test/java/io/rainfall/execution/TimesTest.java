package io.rainfall.execution;

import io.rainfall.Operation;
import io.rainfall.TestException;
import io.rainfall.configuration.ConcurrencyConfig;
import io.rainfall.utils.RangeMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author Aurelien Broszniowski
 */
public class TimesTest {

  @Test
  public void testTimes() {
    ExecutorService executor = Executors.newFixedThreadPool(4);

    final AtomicInteger cnt = new AtomicInteger();
    final Random random = new Random();

    for (int threadNb = 0; threadNb < 4; threadNb++) {
      executor.submit(new Callable() {

        @Override
        public Object call() throws Exception {
          for (int i = 0; i < 100; i++) {
            new Thread() {

              @Override
              public void run() {
                try {
                  Thread.sleep(random.nextInt(100));
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
                cnt.getAndIncrement();
              }
            }.run();
          }
          return null;
        }
      });
    }
    System.out.println(cnt);
    executor.shutdown();
    try {
      boolean success = executor.awaitTermination((long)60, SECONDS);
      if (!success) {
        Assert.fail("error");
      }
    } catch (InterruptedException e) {
      Assert.fail("InterruptedException");
    }
    System.out.println(cnt);
  }
}
