package io.rainfall.configuration;

import io.rainfall.Configuration;

import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * holds Distributed tests information
 *
 * @author Aurelien Broszniowski
 */
public class DistributedConfig extends Configuration {

  private InetSocketAddress masterSocketAddress;
  private InetSocketAddress[] clientsSocketAddresses;

  public static DistributedConfig distributedConfig() {
    return new DistributedConfig();
  }

  public static InetSocketAddress address(String hostname, int port) {
    return new InetSocketAddress(hostname, port);
  }

  public DistributedConfig master(final InetSocketAddress masterSocketAddress) {
    this.masterSocketAddress = masterSocketAddress;
    return this;
  }

  public DistributedConfig clients(final InetSocketAddress... clientsSocketAddresses) {
    this.clientsSocketAddresses = clientsSocketAddresses;
    return this;
  }

  @Override
  public List<String> getDescription() {
    List<String> desc = new ArrayList<String>();
    desc.add("Number of testing clients = " + clientsSocketAddresses.length);
    return desc;
  }
}
