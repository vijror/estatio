package org.estatio.module.base.fixtures.country.enums;

import org.apache.isis.applib.services.registry.ServiceRegistry2;

import org.isisaddons.module.base.platform.fixturesupport.DemoData2;
import org.isisaddons.module.base.platform.fixturesupport.DemoData2Persist;
import org.isisaddons.module.base.platform.fixturesupport.DemoData2Teardown;

import org.incode.module.country.dom.impl.Country;
import org.incode.module.country.dom.impl.CountryRepository;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Getter
@Accessors(chain = true)
public enum Country_data implements DemoData2<Country_data, Country> {

    GreatBritain("GBR", "GB", "United Kingdom"),
    Netherlands ("NLD", "NL", "Netherlands"),
    Italy       ("ITA", "IT", "Italy"),
    France      ("FRA", "FR", "France"),
    Sweden      ("SWE", "SE", "Sweden")
    ;

    private final String reference;
    private final String alpha2Code;
    private final String name;

    @Override
    public Country asDomainObject(final ServiceRegistry2 serviceRegistry2) {
        final Country country = new Country(reference, alpha2Code, name);
        return country;
    }

    @Override
    public Country findUsing(final ServiceRegistry2 serviceRegistry) {
        return findByAlpha2Code(serviceRegistry, this.alpha2Code);
    }

    private static Country findByAlpha2Code(final ServiceRegistry2 serviceRegistry2, final String alpha2Code) {
        return serviceRegistry2.lookupService(CountryRepository.class).findCountryByAlpha2Code(alpha2Code);
    }

    public static class PersistScript
            extends DemoData2Persist<Country_data, Country> {
        public PersistScript() {
            super(Country_data.class);
        }
    }

    public static class DeleteScript
            extends DemoData2Teardown<Country_data, Country> {
        public DeleteScript() {
            super(Country_data.class);
        }
    }

}
