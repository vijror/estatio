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
package org.estatio.module.party.fixtures;

import javax.inject.Inject;

import org.apache.isis.applib.fixturescripts.FixtureScript;

import org.incode.module.country.dom.impl.CountryRepository;

import org.estatio.module.party.dom.Person;
import org.estatio.module.party.dom.PersonGenderType;
import org.estatio.module.party.dom.PersonRepository;

public abstract class PersonAbstract extends FixtureScript {

    @Override
    public abstract void execute(ExecutionContext executionContext);

    protected Person createPerson(
            final String atPath,
            final String reference,
            final String initials,
            final String firstName,
            final String lastName,
            final PersonGenderType gender,
            final ExecutionContext executionContext) {

        Person person = personRepository.newPerson(reference, initials, firstName, lastName, gender, atPath);
        return executionContext.addResult(this, person.getReference(), person);
    }


    // //////////////////////////////////////

    @Inject
    protected CountryRepository countryRepository;

    @Inject
    protected PersonRepository personRepository;

}
