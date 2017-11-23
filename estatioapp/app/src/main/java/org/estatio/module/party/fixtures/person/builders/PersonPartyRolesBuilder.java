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

import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;

import org.apache.isis.applib.fixturescripts.FixtureScript;

import org.estatio.module.party.dom.Person;
import org.estatio.module.party.dom.role.IPartyRoleType;
import org.estatio.module.party.dom.role.PartyRoleTypeService;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class PersonPartyRolesBuilder extends FixtureScript {

    @Getter @Setter
    private Person person;

    @Getter
    private List<IPartyRoleType> partyRoleTypes = Lists.newArrayList();
    public PersonPartyRolesBuilder addPartyRoleType(IPartyRoleType partyRoleType) {
        partyRoleTypes.add(partyRoleType);
        return this;
    }


    @Override
    public void execute(ExecutionContext executionContext) {

        defaultAndCheckParams(executionContext);

        for (IPartyRoleType partyRoleType : partyRoleTypes) {
            partyRoleTypeService.createRole(person, partyRoleType);
        }
    }

    public PersonPartyRolesBuilder defaultAndCheckParams(
            final ExecutionContext executionContext) {
        checkParam("person", executionContext, Person.class);
        return this;
    }

    @Inject
    PartyRoleTypeService partyRoleTypeService;


}

