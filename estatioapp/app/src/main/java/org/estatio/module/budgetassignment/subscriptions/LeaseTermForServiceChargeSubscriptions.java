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
package org.estatio.module.budgetassignment.subscriptions;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;

import org.estatio.module.base.dom.UdoDomainRepositoryAndFactory;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationResultRepository;
import org.estatio.module.lease.dom.LeaseTermForServiceCharge;

@DomainService(nature = NatureOfService.DOMAIN)
public class LeaseTermForServiceChargeSubscriptions extends UdoDomainRepositoryAndFactory<LeaseTermForServiceCharge> {

    public String getId() {
        return "estatio.LeaseTermForServiceChargeSubscriptions";
    }

    public LeaseTermForServiceChargeSubscriptions() {
        super(LeaseTermForServiceChargeSubscriptions.class, LeaseTermForServiceCharge.class);
    }


    @Programmatic
    @com.google.common.eventbus.Subscribe
    @org.axonframework.eventhandling.annotation.EventHandler
    public void on(final LeaseTermForServiceCharge.changeValuesEvent ev) {
        LeaseTermForServiceCharge sourceTerm = ev.getSource();

        switch (ev.getEventPhase()) {
        case DISABLE:
            if (!budgetCalculationResultRepository.findByLeaseTerm(sourceTerm).isEmpty()){
                ev.disable("This term is controlled by a budget");
            }
            break;
        default:
        }
    }

    @Inject
    BudgetCalculationResultRepository budgetCalculationResultRepository;

}
