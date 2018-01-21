import de.rpr.testcontainers.infinispan.ClusteredInfinispanContainer;
import de.rpr.testcontainers.infinispan.InfinispanContainer;
import org.infinispan.client.hotrod.ProtocolVersion;
import org.junit.ClassRule;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class Infinispan91xClusteredContainerIntegrationTest {

  @ClassRule
  public static InfinispanContainer infinispan =
      new ClusteredInfinispanContainer("jboss/infinispan-server:9.1.4.Final")
          .withCaches("testCache")
          .withProtocolVersion(ProtocolVersion.PROTOCOL_VERSION_20);

  @Test
  public void rule_should_have_mapped_hotrod_port() {
    assertNotNull(infinispan.getMappedPort(11222));
  }

  @Test
  public void should_get_existing_cache() {
    assertNotNull(infinispan.getCacheManager().getCache("testCache"));
  }
}
