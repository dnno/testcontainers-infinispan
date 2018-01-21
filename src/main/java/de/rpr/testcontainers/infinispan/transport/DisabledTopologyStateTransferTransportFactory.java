package de.rpr.testcontainers.infinispan.transport;

import org.infinispan.client.hotrod.impl.transport.tcp.TcpTransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Collection;

/**
 * This is a subclass of the {@link TcpTransportFactory}. It overwrites the {@link TcpTransportFactory#updateServers(Collection, byte[], boolean)}
 * in order to suppress the update of the cluster nodes, communicated back to the client during the Hotrod Topology State Transfer.
 */
public class DisabledTopologyStateTransferTransportFactory extends TcpTransportFactory {

  private static final Logger LOG = LoggerFactory.getLogger(DisabledTopologyStateTransferTransportFactory.class);

  @Override
  public void updateServers(final Collection<SocketAddress> newServers, final byte[] cacheName, final boolean quiet) {
    LOG.info("Receiving new Servers: {}. Ignoring...", newServers);
  }

}
