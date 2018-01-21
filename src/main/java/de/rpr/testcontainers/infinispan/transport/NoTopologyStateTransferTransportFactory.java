package de.rpr.testcontainers.infinispan.transport;

import org.infinispan.client.hotrod.impl.transport.tcp.TcpTransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Collection;

public class NoTopologyStateTransferTransportFactory extends TcpTransportFactory {

  private static final Logger LOG = LoggerFactory.getLogger(NoTopologyStateTransferTransportFactory.class);

  @Override
  public void updateServers(final Collection<SocketAddress> newServers, final byte[] cacheName, final boolean quiet) {
    LOG.info("Receiving new Servers: {}. Ignoring...", newServers);
  }
}
