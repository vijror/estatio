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
package org.estatio.module.lease.fixtures.leaseitems.builders;

import java.math.BigInteger;
import java.util.List;

import com.google.common.collect.Lists;

import org.apache.isis.applib.fixturescripts.BuilderScriptAbstract;

import org.estatio.module.charge.dom.Charge;
import org.estatio.module.charge.dom.ChargeGroup;
import org.estatio.module.charge.fixtures.chargegroups.enums.ChargeGroup_enum;
import org.estatio.module.charge.fixtures.charges.enums.Charge_enum;
import org.estatio.module.invoice.dom.PaymentMethod;
import org.estatio.module.lease.dom.InvoicingFrequency;
import org.estatio.module.lease.dom.Lease;
import org.estatio.module.lease.dom.LeaseAgreementRoleTypeEnum;
import org.estatio.module.lease.dom.LeaseItem;
import org.estatio.module.lease.dom.LeaseItemStatus;
import org.estatio.module.lease.dom.LeaseItemType;
import org.estatio.module.lease.dom.LeaseTermForIndexable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@EqualsAndHashCode(of={"lease","sequence"}, callSuper = false)
@ToString(of={"lease","sequence"})
@Accessors(chain = true)
public class LeaseItemForIndexableBuilder extends BuilderScriptAbstract<LeaseItem, LeaseItemForIndexableBuilder> {

    public static final LeaseItemType LEASE_ITEM_TYPE = LeaseItemType.SERVICE_CHARGE_INDEXABLE;
    private static final InvoicingFrequency INVOICING_FREQUENCY = InvoicingFrequency.QUARTERLY_IN_ADVANCE;

    @Getter @Setter Lease lease;
    @Getter @Setter BigInteger sequence;

    @Getter @Setter Charge charge;
    @Getter @Setter LeaseAgreementRoleTypeEnum invoicedBy;
    @Getter @Setter PaymentMethod paymentMethod;
    @Getter @Setter LeaseItemStatus status;

    @Getter @Setter List<LeaseTermForIndexableBuilder.TermSpec> termSpecs = Lists.newArrayList();

    @Getter LeaseItem object;
    @Getter List<LeaseTermForIndexable> terms = Lists.newArrayList();

    @Override
    protected void execute(final ExecutionContext ec) {

        defaultParam("charge", ec, Charge_enum.ItServiceChargeIndexable.findUsing(serviceRegistry));

        final ChargeGroup group = getCharge().getGroup();
        final ChargeGroup_enum expectedGroup = ChargeGroup_enum.ServiceChargeIndexable;
        if(group != expectedGroup.findUsing(serviceRegistry)) {
            throw new IllegalArgumentException("Charge's group must be " + expectedGroup.getRef());
        }

        LeaseItem leaseItem = new LeaseItemBuilder()
            .setLease(lease)
            .setCharge(charge)
            .setLeaseItemType(LEASE_ITEM_TYPE)
            .setInvoicingFrequency(INVOICING_FREQUENCY)
            .setInvoicedBy(invoicedBy)
            .setPaymentMethod(paymentMethod)
            .setStatus(status)
            .setSequence(sequence)
            .build(this, ec)
            .getObject();

        for (final LeaseTermForIndexableBuilder.TermSpec termSpec : termSpecs) {
            final LeaseTermForIndexable term = new LeaseTermForIndexableBuilder()
                    .setLeaseItem(leaseItem)
                    .setStartDate(termSpec.startDate)
                    .setEndDate(termSpec.endDate)
                    .setLeaseTermFrequency(termSpec.leaseTermFrequency)
                    .setBaseValue(termSpec.baseValue)
                    .setBaseIndexStartDate(termSpec.baseIndexStartDate)
                    .setNextIndexStartDate(termSpec.nextIndexStartDate)
                    .setEffectiveDate(termSpec.effectiveDate)
                    .setIndex(termSpec.index)
                    .build(this, ec)
                    .getObject();
            terms.add(term);
        }


        object = leaseItem;
    }
}
