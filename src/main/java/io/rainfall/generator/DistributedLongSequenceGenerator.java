package io.rainfall.generator;

import io.rainfall.SequenceGenerator;
import io.rainfall.configuration.DistributedConfig;

import java.util.concurrent.atomic.AtomicLong;
/**
 *
 * This generator is used for a distrbtued Rainfall test and uses the Rainfall master/slave capabilities to spread the indexes amongst
 * the distributed Rainfall test instances
 *
 * It uses StripedLongSequenceGenerator that stripes the indexes distribution, but which requires a rank value which is given by an
 * InstanceIndexSupplier. In this particular case,the Rainfall master/slave setup is providing the ranks of the different clients.
 *
 * @author Ludovic Orban
 */

public class DistributedLongSequenceGenerator  implements SequenceGenerator {

  private final DistributedConfig distributedConfig;
  private volatile StripedLongSequenceGenerator stripedLongSequenceGenerator;

  public DistributedLongSequenceGenerator(DistributedConfig distributedConfig) {
    this.distributedConfig = distributedConfig;
  }

  @Override
  public long next() {
    if (stripedLongSequenceGenerator == null) {
      stripedLongSequenceGenerator = new StripedLongSequenceGenerator(distributedConfig.getNbClients(), new StripedLongSequenceGenerator.InstanceIndexSupplier() {
        @Override
        public Integer get() {
          return distributedConfig.getCurrentClientId();
        }
      });
    }
    return stripedLongSequenceGenerator.next();
  }

  @Override
  public String getDescription() {
    return "DistributedLongSequenceGenerator";
  }
}
