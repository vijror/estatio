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

import org.isisaddons.module.security.dom.tenancy.ApplicationTenancies;

import org.incode.module.communications.dom.impl.commchannel.CommunicationChannelOwner_newChannelContributions;
import org.incode.module.communications.dom.impl.commchannel.CommunicationChannelType;
import org.incode.module.country.dom.impl.CountryRepository;

import org.estatio.module.party.dom.Party;
import org.estatio.module.party.dom.PartyRepository;
import org.estatio.module.party.dom.Person;
import org.estatio.module.party.dom.PersonGenderType;
import org.estatio.module.party.dom.PersonRepository;
import org.estatio.module.party.dom.relationship.PartyRelationshipRepository;

public abstract class PersonAndCommsAndRelationshipAbstract extends PersonAbstract {

    @Override
    public abstract void execute(ExecutionContext executionContext);

    protected Person createPerson(
            final String atPath,
            final String reference,
            final String initials,
            final String firstName,
            final String lastName,
            final PersonGenderType gender,
            final String phoneNumber,
            final String emailAddress,
            final String fromPartyStr,
            final String relationshipType,
            final ExecutionContext executionContext) {

        // adds to executionContext
        final Person person = createPerson(atPath, reference, initials, firstName, lastName, gender, executionContext);

        if(emailAddress != null) {
            communicationChannelContributedActions
                    .newEmail(person, CommunicationChannelType.EMAIL_ADDRESS, emailAddress);
        }
        if(phoneNumber != null) {
            communicationChannelContributedActions
                    .newPhoneOrFax(person, CommunicationChannelType.PHONE_NUMBER, phoneNumber);
        }

        // associate person
        Party from = partyRepository.findPartyByReference(fromPartyStr);
        if(relationshipType != null) {
            partyRelationshipRepository.newRelationship(from, person, relationshipType, null);
        }

        return person;
    }


    // //////////////////////////////////////

    @Inject
    protected CountryRepository countryRepository;

    @Inject
    protected PartyRepository partyRepository;

    @Inject
    protected PersonRepository personRepository;

    @Inject
    protected CommunicationChannelOwner_newChannelContributions communicationChannelContributedActions;

    @Inject
    protected PartyRelationshipRepository partyRelationshipRepository;

    @Inject
    protected ApplicationTenancies applicationTenancies;

}
