import de.rpr.testcontainers.infinispan.InfinispanContainer;
import de.rpr.testcontainers.infinispan.InfinispanContainerFactory;
import org.infinispan.client.hotrod.ProtocolVersion;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.junit.ClassRule;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class InfinispanContainerIntegrationTest {

  @ClassRule
  public static InfinispanContainer infinispan =
      InfinispanContainerFactory
          .standalone("9.1.3.Final")
          .configurationFile("infinispan-standalone.xml")
          .expose()
          .hotrod()
          .build();

  @Test
  public void rule_should_have_mapped_hotrod_port() {
    assertNotNull(infinispan.getMappedPort(11222));
  }

  @Test
  public void should_get_existing_cache() {
    RemoteCacheManager cacheManager = new RemoteCacheManager(new ConfigurationBuilder()
        .addServers(infinispan.getContainerIpAddress() + ":" + infinispan.getMappedPort(11222))
        .version(ProtocolVersion.PROTOCOL_VERSION_26)
        .build());
    assertNotNull(cacheManager.getCache("testCache"));
  }
}
