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
package org.estatio.module.party.fixtures.person.builders;

import javax.inject.Inject;

import org.isisaddons.module.security.dom.user.ApplicationUserRepository;

import org.estatio.module.base.fixtures.security.apptenancy.personas.ApplicationTenancyForGlobal;
import org.estatio.module.base.platform.fake.EstatioFakeDataService;
import org.estatio.module.party.dom.Person;
import org.estatio.module.party.dom.PersonGenderType;
import org.estatio.module.party.dom.role.PartyRoleTypeService;
import org.estatio.module.party.fixtures.PersonAbstract;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class PersonBuilder extends PersonAbstract {

    @Getter @Setter
    private String atPath;

    @Getter @Setter
    private String reference;

    @Getter @Setter
    private String firstName;

    @Getter @Setter
    private String initials;

    @Getter @Setter
    private String lastName;

    @Getter @Setter
    private PersonGenderType personGenderType;

    @Getter
    private Person person;


    @Override
    public void execute(ExecutionContext executionContext) {

        defaultAndCheckParams(executionContext);

        person = createPerson(getAtPath(), getReference(), getInitials(), getFirstName(), getLastName(), getPersonGenderType(), executionContext);

    }

    public PersonBuilder defaultAndCheckParams(final ExecutionContext executionContext) {

        defaultParam("atPath", executionContext, ApplicationTenancyForGlobal.PATH);
        defaultParam("reference", executionContext, fakeDataService.lorem().fixedString(6));
        defaultParam("firstName", executionContext, fakeDataService.name().firstName());
        defaultParam("lastName", executionContext, fakeDataService.name().fullName());
        defaultParam("personGenderType", executionContext, fakeDataService.collections().anEnum(PersonGenderType.class));
        defaultParam("initials", executionContext, firstName.substring(0,1));

        return this;
    }

    @Inject
    EstatioFakeDataService fakeDataService;

    @Inject
    PartyRoleTypeService partyRoleTypeService;

    @Inject
    ApplicationUserRepository applicationUserRepository;

}

