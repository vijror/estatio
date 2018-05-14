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
package org.estatio.module.lease.integtests.imports;

import java.math.BigDecimal;

import javax.inject.Inject;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.applib.services.registry.ServiceRegistry2;

import org.estatio.module.asset.dom.Property;
import org.estatio.module.asset.fixtures.property.enums.Property_enum;
import org.estatio.module.lease.contributions.Property_maintainTurnOverRentSwe;
import org.estatio.module.lease.fixtures.lease.enums.Lease_enum;
import org.estatio.module.lease.fixtures.leaseitems.enums.LeaseItemForTurnoverRentFixed_enum;
import org.estatio.module.lease.imports.LeaseTermForTurnOverRentFixedImport;
import org.estatio.module.lease.imports.LeaseTermForTurnoverRentFixedImportManager;
import org.estatio.module.lease.integtests.LeaseModuleIntegTestAbstract;

import static org.assertj.core.api.Assertions.assertThat;

public class LeasetermForTurnoverRentFixedImport_IntegTest extends LeaseModuleIntegTestAbstract {

    @Before
    public void setupData() {
        runFixtureScript(new FixtureScript() {
            @Override
            protected void execute(ExecutionContext executionContext) {
                executionContext.executeChild(this, LeaseItemForTurnoverRentFixed_enum.HanPoison001Se.builder());
                executionContext.executeChild(this, LeaseItemForTurnoverRentFixed_enum.HanTopModel002Se.builder());
            }
        });
    }


    @Test
    public void happyCase() throws Exception {

        // given
        Property han = Property_enum.HanSe.findUsing(serviceRegistry2);
        Property_maintainTurnOverRentSwe mixin = new Property_maintainTurnOverRentSwe(han);

        // when
        final LeaseTermForTurnoverRentFixedImportManager manager = wrap(mixin).maintainTurnoverRent(2011);

        // then
        assertThat(manager.getTurnoverRentLines()).hasSize(2);
        final LeaseTermForTurnOverRentFixedImport termForPoison = manager.getTurnoverRentLines().get(0);
        assertThat(termForPoison.getLeaseReference()).isEqualTo(Lease_enum.HanPoison001Se.findUsing(serviceRegistry2).getReference());
        assertThat(termForPoison.getStartDate()).isEqualTo(new LocalDate(2011,1,1));
        assertThat(termForPoison.getEndDate()).isEqualTo(new LocalDate(2011,12,31));
        assertThat(termForPoison.getValue()).isEqualTo(new BigDecimal("20000.00"));
        final LeaseTermForTurnOverRentFixedImport termForTopModel = manager.getTurnoverRentLines().get(1);
        assertThat(termForTopModel.getLeaseReference()).isEqualTo(Lease_enum.HanTopModel002Se.findUsing(serviceRegistry2).getReference());
        assertThat(termForTopModel.getStartDateCurrent()).isEqualTo(new LocalDate(2010,7,15));
        assertThat(termForTopModel.getValueCurrent()).isEqualTo(new BigDecimal("2000.00"));
        
    }

    @Inject ServiceRegistry2 serviceRegistry2;




}