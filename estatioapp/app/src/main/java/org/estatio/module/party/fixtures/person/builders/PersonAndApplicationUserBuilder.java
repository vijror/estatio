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

import org.isisaddons.module.security.dom.user.ApplicationUser;

import org.estatio.module.party.dom.Person;
import org.estatio.module.party.dom.PersonGenderType;
import org.estatio.module.party.fixtures.PersonAbstract;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class PersonAndApplicationUserBuilder extends PersonAbstract {

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

    @Getter @Setter
    private String securityUsername;

    @Getter @Setter
    private String securityUserAccountCloneFrom;

    @Getter
    private Person person;

    @Getter
    private ApplicationUser applicationUser;

    @Override
    public void execute(ExecutionContext executionContext) {

        // person
        PersonBuilder personBuilder = new PersonBuilder();
        personBuilder.setAtPath(atPath)
                .setReference(reference)
                .setFirstName(firstName)
                .setInitials(initials)
                .setLastName(lastName)
                .setPersonGenderType(personGenderType)
                .defaultAndCheckParams(executionContext);
        personBuilder.execute(executionContext);
        person = personBuilder.getPerson();


        // application user
        ApplicationUserBuilder applicationUserBuilder = new ApplicationUserBuilder();

        if(securityUsername != null) {
            applicationUserBuilder.setSecurityUsername(securityUsername)
                    .setSecurityUserAccountCloneFrom(securityUserAccountCloneFrom)
                    .defaultAndCheckParams(executionContext);

            applicationUserBuilder.execute(executionContext);
            applicationUser = applicationUserBuilder.getApplicationUser();
        }
    }

}

