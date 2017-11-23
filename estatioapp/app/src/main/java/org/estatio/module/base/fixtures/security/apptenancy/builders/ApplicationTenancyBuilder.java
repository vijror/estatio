package org.estatio.module.base.fixtures.security.apptenancy.builders;

import org.isisaddons.module.security.dom.tenancy.ApplicationTenancy;

import org.estatio.module.base.fixtures.security.apptenancy.enums.ApplicationTenancy_data;
import org.estatio.module.base.platform.fixturesupport.BuilderScriptAbstract;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class ApplicationTenancyBuilder extends BuilderScriptAbstract<ApplicationTenancyBuilder> {

    @Getter @Setter
    ApplicationTenancy_data data;

    @Getter
    private ApplicationTenancy applicationTenancy;

    @Override
    protected void execute(final ExecutionContext executionContext) {

        checkParam("data", executionContext, ApplicationTenancy_data.class);

        applicationTenancy = data.upsertUsing(serviceRegistry);

        executionContext.addResult(this, data.getPath(), applicationTenancy);
    }
}
