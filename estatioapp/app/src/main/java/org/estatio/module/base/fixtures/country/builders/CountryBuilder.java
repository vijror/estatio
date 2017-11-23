package org.estatio.module.base.fixtures.country.builders;

import org.incode.module.country.dom.impl.Country;

import org.estatio.module.base.fixtures.country.enums.Country_data;
import org.estatio.module.base.platform.fixturesupport.BuilderScriptAbstract;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class CountryBuilder extends BuilderScriptAbstract<CountryBuilder> {

    @Getter @Setter
    Country_data data;

    @Getter
    private Country country;

    @Override
    protected void execute(final ExecutionContext executionContext) {
        checkParam("data", executionContext, Country_data.class);

        country = data.upsertUsing(serviceRegistry);

        executionContext.addResult(this, country.getReference(), country);
    }
}
