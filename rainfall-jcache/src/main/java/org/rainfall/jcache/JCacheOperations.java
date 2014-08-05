package org.rainfall.jcache;

import org.rainfall.jcache.operation.PutOperation;

/**
 * Contains the helper methods to instantiate the JCache {@link org.rainfall.Operation} objects.
 *
 * @author Aurelien Broszniowski
 */

public class JCacheOperations {

  public static PutOperation put(final double weight) {
    return new PutOperation(weight);
  }
}
