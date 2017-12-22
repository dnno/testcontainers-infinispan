import de.rpr.testcontainers.infinispan.InfinispanContainer;
import org.infinispan.client.hotrod.ProtocolVersion;
import org.junit.ClassRule;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class Infinispan91xContainerIntegrationTest {

  @ClassRule
  public static InfinispanContainer infinispan = new InfinispanContainer("jboss/infinispan-server:9.1.3.Final")
          .withProtocolVersion(ProtocolVersion.PROTOCOL_VERSION_20)
          .withCaches("testCache");

  @Test
  public void rule_should_have_mapped_hotrod_port() {
    assertNotNull(infinispan.getMappedPort(11222));
  }

  @Test
  public void should_get_existing_cache() {
    assertNotNull(infinispan.getCacheManager().getCache("testCache"));
  }
}
