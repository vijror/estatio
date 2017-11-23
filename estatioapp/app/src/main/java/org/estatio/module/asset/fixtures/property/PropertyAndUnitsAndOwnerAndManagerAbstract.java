/*
 *
 *  Copyright 2012-2014 Eurocommercial Properties NV
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.estatio.module.asset.fixtures.property;

import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;

import org.joda.time.LocalDate;

import org.apache.isis.applib.fixturescripts.FixtureScript;

import org.isisaddons.module.security.dom.tenancy.ApplicationTenancyRepository;

import org.incode.module.country.dom.impl.Country;
import org.incode.module.country.dom.impl.CountryRepository;

import org.estatio.module.asset.dom.Property;
import org.estatio.module.asset.dom.PropertyRepository;
import org.estatio.module.asset.dom.PropertyType;
import org.estatio.module.asset.dom.Unit;
import org.estatio.module.asset.fixtures.property.builders.PropertyBuilder;
import org.estatio.module.asset.fixtures.property.builders.PropertyManagerBuilder;
import org.estatio.module.asset.fixtures.property.builders.PropertyOwnerBuilder;
import org.estatio.module.asset.fixtures.property.builders.PropertyUnitsBuilder;
import org.estatio.module.party.dom.Party;
import org.estatio.module.party.dom.PartyRepository;

import lombok.Getter;
import static org.incode.module.base.integtests.VT.ld;

/**
 * Sets up the {@link Property} and also a number of
 * {@link Unit}s.
 */
public abstract class PropertyAndUnitsAndOwnerAndManagerAbstract extends FixtureScript {

    PropertyBuilder propertyBuilder = new PropertyBuilder() {};
    PropertyUnitsBuilder propertyUnitsBuilder = new PropertyUnitsBuilder() {};
    PropertyOwnerBuilder propertyOwnerBuilder = new PropertyOwnerBuilder() {};
    PropertyManagerBuilder propertyManagerBuilder = new PropertyManagerBuilder() {};

    @Getter
    public Property property;

    @Getter
    private List<Unit> units = Lists.newArrayList();

    protected Property createPropertyAndUnits(
            final String atPath,
            final String reference,
            final String name,
            final String city,
            final Country country,
            final PropertyType type,
            final int numberOfUnits,
            final LocalDate openingDate,
            final LocalDate acquireDate,
            final Party owner,
            final Party manager,
            final String locationStr,
            final ExecutionContext executionContext) {

        this.property = propertyBuilder
                .setReference(reference)
                .setName(name)
                .setAcquireDate(acquireDate)
                .setCity(city)
                .setCountry(country)
                .setOpeningDate(openingDate)
                .setLocationStr(locationStr)
                .build(this, executionContext)
                .getProperty();

        this.units = propertyUnitsBuilder
                .setProperty(property)
                .setNumberOfUnits(numberOfUnits)
                .build(this, executionContext)
                .getUnits();

        if(owner != null) {
            propertyOwnerBuilder
                    .setProperty(property)
                    .setOwner(owner)
                    .setStartDate(ld(1999, 1, 1))
                    .setEndDate(ld(2000, 1, 1))
                    .build(this, executionContext);
        }
        if(manager != null) {
            propertyManagerBuilder
                    .setProperty(property)
                    .setManager(manager)
                    .build(this, executionContext);
        }

        return property;
    }

    @Inject
    protected CountryRepository countryRepository;

    @Inject
    protected PropertyRepository propertyRepository;

    @Inject
    protected PartyRepository partyRepository;

    @Inject
    protected ApplicationTenancyRepository applicationTenancyRepository;

}
