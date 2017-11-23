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
package org.estatio.module.asset.fixtures.person.builders;

import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;

import org.apache.isis.applib.fixturescripts.FixtureScript;

import org.isisaddons.module.security.dom.user.ApplicationUserRepository;

import org.estatio.module.asset.dom.Property;
import org.estatio.module.asset.dom.PropertyRepository;
import org.estatio.module.asset.dom.role.FixedAssetRole;
import org.estatio.module.asset.dom.role.FixedAssetRoleTypeEnum;
import org.estatio.module.base.platform.fake.EstatioFakeDataService;
import org.estatio.module.party.dom.Person;
import org.estatio.module.party.dom.role.PartyRole;
import org.estatio.module.party.dom.role.PartyRoleTypeService;
import org.estatio.module.party.fixtures.person.builders.PersonPartyRolesBuilder;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * A superset of {@link PersonPartyRolesBuilder}, adding a {@link FixedAssetRole} and a corresponding {@link PartyRole}.
 */
@Accessors(chain = true)
public class PersonFixedAssetRolesBuilder extends FixtureScript {

    @Getter @Setter
    private Person person;

    @Data
    static class FixedAssetRoleSpec {
        final FixedAssetRoleTypeEnum roleType;
        final String propertyRef;
    }

    @Getter
    private List<FixedAssetRoleSpec> fixedAssetRoles = Lists.newArrayList();
    public PersonFixedAssetRolesBuilder addFixedAssetRole(
            final FixedAssetRoleTypeEnum fixedAssetRoleType,
            final String propertyRef) {
        fixedAssetRoles.add(new FixedAssetRoleSpec(fixedAssetRoleType, propertyRef));
        return this;
    }

    @Override
    public void execute(ExecutionContext executionContext) {

        defaultAndCheckParams(executionContext);

        for (FixedAssetRoleSpec spec : fixedAssetRoles) {
            Property property = propertyRepository.findPropertyByReference(spec.getPropertyRef());
            property.addRoleIfDoesNotExist(
                    person, spec.roleType, null, null);
            partyRoleTypeService.createRole(person, spec.roleType);
        }

    }

    public PersonFixedAssetRolesBuilder defaultAndCheckParams(
            final ExecutionContext executionContext) {
        checkParam("person", executionContext, Person.class);
        return this;
    }

    @Inject
    EstatioFakeDataService fakeDataService;

    @Inject
    PartyRoleTypeService partyRoleTypeService;

    @Inject
    PropertyRepository propertyRepository;

    @Inject
    ApplicationUserRepository applicationUserRepository;

}

