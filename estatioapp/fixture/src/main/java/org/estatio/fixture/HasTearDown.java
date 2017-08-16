package org.estatio.fixture;

import org.incode.module.fixturesupport.dom.scripts.TeardownFixtureAbstract;

public interface HasTearDown {
     abstract TeardownFixtureAbstract getTearDown();
}