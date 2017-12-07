package de.rpr.testcontainers.infinispan;

import org.junit.rules.TestRule;
import org.testcontainers.containers.Container;

public interface InfinispanContainer<SELF extends InfinispanContainer<SELF>> extends Container<SELF>, TestRule {

}
