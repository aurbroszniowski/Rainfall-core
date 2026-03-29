package io.rainfall.generator;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author Henri Tremblay
 */
public class IterationSequenceGeneratorTest {
  
  @Test
  public void firstValueDefaultConstructor() {
    IterationSequenceGenerator sequenceGenerator = new IterationSequenceGenerator();
    assertThat(sequenceGenerator.next(), is(1L));
  }

  @Test
  public void firstValueParamConstructor() {
    IterationSequenceGenerator sequenceGenerator = new IterationSequenceGenerator(42);
    assertThat(sequenceGenerator.next(), is(42L));
  }

  @Test
  public void iterateOneByOne() {
    IterationSequenceGenerator sequenceGenerator = new IterationSequenceGenerator();
    assertThat(sequenceGenerator.next(), is(1L));
    assertThat(sequenceGenerator.next(), is(2L));
    assertThat(sequenceGenerator.next(), is(3L));
  }

  @Test
  public void getDescription() {
    IterationSequenceGenerator sequenceGenerator = new IterationSequenceGenerator();
    assertThat(sequenceGenerator.getDescription(), is("Iterative sequence"));
  }

  @Test
  public void shouldAdvanceCleanlyAcrossABlockBoundary() {
    IterationSequenceGenerator sequenceGenerator = new IterationSequenceGenerator();

    for (long expected = 1; expected <= IterationSequenceGenerator.BLOCK_SIZE + 2L; expected++) {
      assertThat(sequenceGenerator.next(), is(expected));
    }
  }

  @Test
  public void concurrentCallsShouldReturnUniqueValues() throws Exception {
    IterationSequenceGenerator sequenceGenerator = new IterationSequenceGenerator();
    Set<Long> generatedValues = Collections.newSetFromMap(new ConcurrentHashMap<Long, Boolean>());

    executeConcurrently(sequenceGenerator, generatedValues, 8, 500);

    assertThat(generatedValues.size(), is(4000));
  }

  @Test
  public void valuesShouldIncreaseWithinEachThread() throws Exception {
    IterationSequenceGenerator sequenceGenerator = new IterationSequenceGenerator(42);
    List<List<Long>> valuesByThread = Collections.synchronizedList(new ArrayList<List<Long>>());

    int threadCount = 8;
    int iterationsPerThread = 200;
    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
    CountDownLatch ready = new CountDownLatch(threadCount);
    CountDownLatch start = new CountDownLatch(1);
    try {
      for (int i = 0; i < threadCount; i++) {
        executorService.submit(() -> {
          List<Long> localValues = new ArrayList<Long>(iterationsPerThread);
          ready.countDown();
          await(start);
          for (int j = 0; j < iterationsPerThread; j++) {
            localValues.add(sequenceGenerator.next());
          }
          valuesByThread.add(localValues);
        });
      }
      assertTrue(ready.await(5, TimeUnit.SECONDS));
      start.countDown();
    } finally {
      shutdown(executorService);
    }

    assertThat(valuesByThread.size(), is(threadCount));
    for (List<Long> threadValues : valuesByThread) {
      assertThat(threadValues.size(), is(iterationsPerThread));
      assertThat(threadValues.get(0), greaterThan(41L));
      for (int i = 1; i < threadValues.size(); i++) {
        assertThat(threadValues.get(i), greaterThan(threadValues.get(i - 1)));
      }
    }
  }

  private void executeConcurrently(IterationSequenceGenerator sequenceGenerator, Set<Long> generatedValues,
                                   int threadCount, int iterationsPerThread) throws Exception {
    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
    CountDownLatch ready = new CountDownLatch(threadCount);
    CountDownLatch start = new CountDownLatch(1);
    try {
      for (int i = 0; i < threadCount; i++) {
        executorService.submit(() -> {
          ready.countDown();
          await(start);
          for (int j = 0; j < iterationsPerThread; j++) {
            generatedValues.add(sequenceGenerator.next());
          }
        });
      }
      assertTrue(ready.await(5, TimeUnit.SECONDS));
      start.countDown();
    } finally {
      shutdown(executorService);
    }
  }

  private void shutdown(ExecutorService executorService) throws InterruptedException {
    executorService.shutdown();
    assertTrue(executorService.awaitTermination(10, TimeUnit.SECONDS));
  }

  private void await(CountDownLatch latch) {
    try {
      latch.await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new AssertionError(e);
    }
  }
}
