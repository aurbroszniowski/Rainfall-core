package io.rainfall.configuration;

import io.rainfall.Configuration;
import io.rainfall.utils.RainfallClient;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * holds Distributed tests information
 * TODO :Merge Distributed and Concurrency configs
 *
 * @author Aurelien Broszniowski
 */
public class DistributedConfig extends Configuration {

  private InetSocketAddress masterAddress;
  private int nbClients;

  private RainfallClient currentClient;

  private DistributedConfig(final InetSocketAddress masterAddress, final int nbClients) {
    this.masterAddress = masterAddress;
    this.nbClients = nbClients;
  }

  public static DistributedConfig distributedConfig(final InetSocketAddress masterAddress, int nbClients) {
    return new DistributedConfig(masterAddress, nbClients);
  }

  public static InetSocketAddress address(String hostname, int port) {
    return new InetSocketAddress(hostname, port);
  }

  public int getNbClients() {
    return this.nbClients;
  }

  public InetSocketAddress getMasterAddress() {
    return masterAddress;
  }

  @Override
  public List<String> getDescription() {
    List<String> desc = new ArrayList<String>();
    desc.add("Number of testing clients = " + nbClients);
    return desc;
  }

  public void setCurrentClient(final RainfallClient currentClient) {
    this.currentClient = currentClient;
  }

  public RainfallClient getCurrentClient() {
    return currentClient;
  }

  public int getCurrentClientId() {
    return currentClient.getClientId();
  }
}
