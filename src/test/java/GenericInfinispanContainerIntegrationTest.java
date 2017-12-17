import org.infinispan.client.hotrod.ProtocolVersion;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.LogMessageWaitStrategy;

import java.time.Duration;
import java.util.concurrent.*;

import static org.junit.Assert.assertNotNull;

public class GenericInfinispanContainerIntegrationTest {

  ExecutorService executorService = Executors.newCachedThreadPool();
  RemoteCacheManager cacheManager;

  @ClassRule
  public static GenericContainer infinispan =
      new GenericContainer("jboss/infinispan-server:9.1.3.Final")
          .waitingFor(new LogMessageWaitStrategy().withRegEx(".*Infinispan Server.*started in.*\\s"))
          .withStartupTimeout(Duration.ofMillis(20000))
          .withClasspathResourceMapping(
              "infinispan-standalone.xml",
              "/opt/jboss/infinispan-server/standalone/configuration/standalone.xml",
              BindMode.READ_ONLY)
          .withCommand("standalone");

  @Before
  public void setup() {
    cacheManager = new RemoteCacheManager(new ConfigurationBuilder()
        .addServers(getServerAddress())
        .version(ProtocolVersion.PROTOCOL_VERSION_26)
        .build());
  }

  @Test
  public void should_be_able_to_retrieve_a_cache() throws Exception {
    Future<RemoteCache<Object, Object>> result = executorService.submit(() -> cacheManager.getCache());
    assertNotNull(result.get(1500, TimeUnit.MILLISECONDS));
  }

  @Test
  public void should_be_able_to_retrieve_a_configured_cache() throws Exception {
    Future<RemoteCache<Object, Object>> result = executorService.submit(() -> cacheManager.getCache("testCache"));
    assertNotNull(result.get(1500, TimeUnit.MILLISECONDS));
  }

  private String getServerAddress() {
    return infinispan.getContainerIpAddress() + ":" + infinispan.getMappedPort(11222);
  }
}
