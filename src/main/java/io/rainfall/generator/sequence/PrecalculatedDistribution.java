package io.rainfall.generator.sequence;

import io.rainfall.utils.ConcurrentPseudoRandom;

import org.terracotta.offheapstore.OffHeapHashMap;
import org.terracotta.offheapstore.buffersource.OffHeapBufferSource;
import org.terracotta.offheapstore.paging.PageSource;
import org.terracotta.offheapstore.paging.UnlimitedPageSource;
import org.terracotta.offheapstore.storage.OffHeapBufferStorageEngine;
import org.terracotta.offheapstore.storage.PointerSize;
import org.terracotta.offheapstore.storage.portability.Portability;
import org.terracotta.offheapstore.storage.portability.SerializablePortability;
import org.terracotta.offheapstore.util.MemoryUnit;

import java.io.Serializable;
import java.util.Random;

/**
 * @author Aurelien Broszniowski
 */
public class PrecalculatedDistribution {

  private OffHeapHashMap<Long, Long> map;
  private long maxentries = new Double(Math.pow(2, 30)).longValue();
  static final double SQRPI2 = Math.sqrt(6.283185307179586);

  private long minimum;
  private long maximum;
  private long width;

  private PrecalculatedDistribution() {}

  public PrecalculatedDistribution(long minimum, long maximum, long width) {
    this.minimum = minimum;
    this.maximum = maximum;
    this.width = width;
    long mean = (maximum - minimum) / 2 + minimum;

    PageSource source = new UnlimitedPageSource(new OffHeapBufferSource());
    Portability<Serializable> portability = new SerializablePortability();

    map = new OffHeapHashMap<Long, Long>(
        source,
        new OffHeapBufferStorageEngine<Long, Long>(PointerSize.INT, source, MemoryUnit.KILOBYTES.toBytes(8),
            portability, portability));

    // we precalculate a table of 4M entries forthe gaussian distribution
    for (long i = 0; i < maxentries; i++) {

      double d2 = (i - mean) / width;
      Double d = Math.exp(-0.5 * d2 * d2) / (SQRPI2 * width);

      map.put(i, d.longValue());
      if(i%10000 == 0) System.out.println("-" + i +" = "+ d.longValue());
    }
  }

  public long generate(ConcurrentPseudoRandom rnd) {
    return map.get(Math.abs(rnd.nextLong()) % maxentries);
  }

  public String getDescription() {
    return "Gaussian";
  }


}
