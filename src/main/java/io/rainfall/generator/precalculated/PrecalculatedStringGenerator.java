package io.rainfall.generator.precalculated;

import io.rainfall.ObjectGenerator;
import io.rainfall.generator.StringGenerator;
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

/**
 * @author Aurelien Broszniowski
 */
public class PrecalculatedStringGenerator implements ObjectGenerator<String> {

  OffHeapHashMap<Long, String> map;

  public PrecalculatedStringGenerator(long nbPrecalculations, int length) {
    PageSource source = new UnlimitedPageSource(new OffHeapBufferSource());
    Portability<Serializable> portability = new SerializablePortability();

    map = new OffHeapHashMap<Long, String>(
        source,
        new OffHeapBufferStorageEngine<Long, String>(PointerSize.INT, source, MemoryUnit.KILOBYTES.toBytes(8),
            portability, portability));

    StringGenerator generator = new StringGenerator(length);

    for (long seed = 0; seed < nbPrecalculations; seed++) {
      map.put(seed, generator.generate(seed));
    }
  }

  @Override
  public String generate(final Long seed) {
    return map.get(seed);
  }

  @Override
  public String getDescription() {
    return null;
  }
}
