package org.estatio.module.numerator.integtests.dom;

import org.apache.isis.applib.services.registry.ServiceRegistry2;

import org.isisaddons.module.base.platform.fixturesupport.DemoData2;
import org.isisaddons.module.base.platform.fixturesupport.DemoData2Persist;
import org.isisaddons.module.base.platform.fixturesupport.DemoData2Teardown;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Getter
@Accessors(chain = true)
public enum NumeratorExampleObject_data implements DemoData2<NumeratorExampleObject_data, NumeratorExampleObject> {

    Kal("Kal"),
    Oxf("Oxf");

    private final String name;

    @Override
    public NumeratorExampleObject asDomainObject(final ServiceRegistry2 serviceRegistry2) {
        return NumeratorExampleObject.builder()
                .name(name)
                .build();
    }

    public static class PersistScript extends DemoData2Persist<NumeratorExampleObject_data, NumeratorExampleObject> {
        public PersistScript() {
            super(NumeratorExampleObject_data.class);
        }
    }

    public static class DeleteScript
            extends DemoData2Teardown<NumeratorExampleObject_data, NumeratorExampleObject> {
        public DeleteScript() {
            super(NumeratorExampleObject_data.class);
        }
    }


}
