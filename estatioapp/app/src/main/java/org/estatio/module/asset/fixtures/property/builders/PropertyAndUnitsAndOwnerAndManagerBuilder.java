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
package org.estatio.module.asset.fixtures.property.builders;

import java.util.List;

import com.google.common.collect.Lists;

import org.joda.time.LocalDate;

import org.incode.module.country.dom.impl.Country;

import org.estatio.module.asset.dom.Property;
import org.estatio.module.asset.dom.PropertyType;
import org.estatio.module.asset.dom.Unit;
import org.estatio.module.base.platform.fixturesupport.BuilderScriptAbstract;
import org.estatio.module.party.dom.Party;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class PropertyAndUnitsAndOwnerAndManagerBuilder
        extends BuilderScriptAbstract<PropertyAndUnitsAndOwnerAndManagerBuilder> {

    PropertyBuilder propertyBuilder = new PropertyBuilder();
    PropertyUnitsBuilder propertyUnitsBuilder = new PropertyUnitsBuilder();
    PropertyOwnerBuilder propertyOwnerBuilder = new PropertyOwnerBuilder();
    PropertyManagerBuilder propertyManagerBuilder = new PropertyManagerBuilder();

    @Getter @Setter
    private String reference;

    @Getter @Setter
    private String name;

    @Getter @Setter
    private String city;

    @Getter @Setter
    private Country country;

    @Getter @Setter
    private PropertyType propertyType;

    @Getter @Setter
    private LocalDate acquireDate;

    @Getter @Setter
    private LocalDate openingDate;

    @Getter @Setter
    private String locationStr;


    @Getter @Setter
    private Party owner;

    @Getter @Setter
    private Party manager;

    @Getter @Setter
    private Integer numberOfUnits;

    @Getter
    private Property property;

    @Getter
    private List<Unit> units = Lists.newArrayList();

    @Override
    protected void execute(final ExecutionContext executionContext) {

        this.property = propertyBuilder
                .setReference(reference)
                .setName(name)
                .setAcquireDate(acquireDate)
                .setCity(city)
                .setCountry(country)
                .setOpeningDate(openingDate)
                .setLocationStr(locationStr)
                .build(executionContext)
                .getProperty();

        this.units = propertyUnitsBuilder
                .setProperty(property)
                .setNumberOfUnits(numberOfUnits)
                .build(executionContext)
                .getUnits();

        if(owner != null) {
            propertyOwnerBuilder
                    .setProperty(property)
                    .setOwner(owner)
                    .build(executionContext);
        }
        if(manager != null) {
            propertyManagerBuilder
                    .setProperty(property)
                    .setManager(manager)
                    .build(executionContext);
        }

    }

}
