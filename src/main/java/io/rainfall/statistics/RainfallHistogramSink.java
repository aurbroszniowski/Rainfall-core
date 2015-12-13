package io.rainfall.statistics;

import org.HdrHistogram.Histogram;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This is a thin facade on the Histogram class's recording functions.
 * Per thread Histograms are created via a factory method. Other than the 3 record...
 * methods, the reset(), marks the per thread histograms and resets the values,
 * as dead, and fetchHistogram() method returns an aggregate Histogram of
 * all the active histograms.
 *
 * @author cschanck
 * @author Aurelien Broszniowski
 **/
public class RainfallHistogramSink<E extends Enum<E>> {

  private final Factory factory;
  private final ConcurrentLinkedQueue<HistogramHolder> actives = new ConcurrentLinkedQueue<HistogramHolder>();
  private static final ThreadLocal<HistogramHolder> context = new ThreadLocal<HistogramHolder>();

  private static class HistogramHolder {
    private volatile boolean dead = false;
    private ConcurrentHashMap<Enum, Histogram> histogram;

    public HistogramHolder(ConcurrentHashMap<Enum, Histogram> histogram) {
      this.setHistogram(histogram);
    }

    public boolean isDead() {
      return dead;
    }

    public void setDead(boolean dead) {
      this.dead = dead;
    }

    public Histogram getHistogram(Enum result) {
      return histogram.get(result);
    }

    public Collection<Histogram> getHistograms() {
      return histogram.values();
    }

    public void setHistogram(ConcurrentHashMap<Enum, Histogram> histogram) {
      this.histogram = histogram;
    }
  }

  public interface Factory {
    ConcurrentHashMap<Enum, Histogram> createHistograms();
  }

  public RainfallHistogramSink(Factory factory) {
    this.factory = factory;
  }

  private HistogramHolder perThread() {
    HistogramHolder hh = context.get();
    if (hh == null || hh.isDead()) {
      hh = new HistogramHolder(factory.createHistograms());
      actives.add(hh);
      context.set(hh);
    }
    return hh;
  }

  public void recordValueWithExpectedInterval(Enum<E> result, long value,
                                              long expectedIntervalBetweenValueSamples) {
    perThread().getHistogram(result).recordValueWithExpectedInterval(value, expectedIntervalBetweenValueSamples);
  }

  public void recordValueWithCount(Enum<E> result, long value, long count) {
    perThread().getHistogram(result).recordValueWithCount(value, count);
  }

  public void recordValue(Enum result, long value) {
    perThread().getHistogram(result).recordValue(value);
  }

  public Histogram fetchHistogram(final Enum<E> result) {
    ConcurrentHashMap<Enum, Histogram> aggregate = factory.createHistograms();
    for (HistogramHolder hh : actives) {
      aggregate.get(result).add(hh.getHistogram(result));
    }
    return aggregate.get(result);
  }

  public synchronized void reset() {
    for (HistogramHolder hh : actives) {
      hh.setDead(true);
      Collection<Histogram> histograms = hh.getHistograms();
      for (Histogram histogram : histograms) {
        histogram.reset();
      }
    }
    actives.clear();
  }

}
