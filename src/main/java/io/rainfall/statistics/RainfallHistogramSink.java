package io.rainfall.statistics;

import org.HdrHistogram.Histogram;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This is a thin facade on the Histogram class's recording functions.
 * Per thread Histograms are created via a factory method. Other than the 3 record...
 * methods, the reset(), marks the per thread histograms and resets the values,
 * as dead, and fetchHistogram() method returns an aggregate Histogram of
 * all the active histograms.
 *
 * @author cschanck
 **/
public class RainfallHistogramSink {

  private final Factory factory;
  private ConcurrentLinkedQueue<HistogramHolder> actives = new ConcurrentLinkedQueue<HistogramHolder>();
  private static final ThreadLocal<HistogramHolder> context = new ThreadLocal<HistogramHolder>();

  private static class HistogramHolder {
    private volatile boolean dead = false;
    private Histogram histogram;

    public HistogramHolder(Histogram histogram) {
      this.setHistogram(histogram);
    }

    public boolean isDead() {
      return dead;
    }

    public void setDead(boolean dead) {
      this.dead = dead;
    }

    public Histogram getHistogram() {
      return histogram;
    }

    public void setHistogram(Histogram histogram) {
      this.histogram = histogram;
    }
  }

  public static interface Factory {
    public Histogram createHistogram();
  }

  public RainfallHistogramSink(Factory factory) {
    this.factory = factory;
  }

  private HistogramHolder perThread() {
    HistogramHolder hh = context.get();
    if (hh == null || hh.isDead()) {
      hh = new HistogramHolder(factory.createHistogram());
      actives.add(hh);
      context.set(hh);
    }
    return hh;
  }

  public void recordValueWithExpectedInterval(long value,
                                              long expectedIntervalBetweenValueSamples) throws ArrayIndexOutOfBoundsException {
    perThread().getHistogram().recordValueWithExpectedInterval(value, expectedIntervalBetweenValueSamples);
  }

  public void recordValueWithCount(long value, long count) throws ArrayIndexOutOfBoundsException {
    perThread().getHistogram().recordValueWithCount(value, count);
  }

  public void recordValue(long value) throws ArrayIndexOutOfBoundsException {
    perThread().getHistogram().recordValue(value);
  }

  public Histogram fetchHistogram() {
    Histogram aggregate = factory.createHistogram();
    for(HistogramHolder hh:actives) {
      aggregate.add(hh.getHistogram());
    }
    return aggregate;
  }

  public void reset() {
    for (HistogramHolder hh : actives) {
      hh.setDead(true);
      hh.getHistogram().reset();
    }
    actives.clear();
  }

}
