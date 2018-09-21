package org.estatio.module.capex.integtests.document;

import java.util.Arrays;

import javax.inject.Inject;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.fixturescripts.FixtureScript;

import org.estatio.module.capex.fixtures.document.enums.IncomingGenericPdf_enum;
import org.estatio.module.capex.integtests.CapexModuleIntegTestAbstract;
import org.estatio.module.capex.seed.DocumentTypesAndTemplatesForCapexFixture;
import org.estatio.module.invoice.dom.InvoiceRepository;
import org.estatio.module.invoice.dom.InvoiceRunType;
import org.estatio.module.invoice.dom.InvoiceStatus;
import org.estatio.module.lease.contributions.Lease_calculate;
import org.estatio.module.lease.dom.Lease;
import org.estatio.module.lease.dom.LeaseItemType;
import org.estatio.module.lease.fixtures.leaseitems.enums.LeaseItemForTax_enum;

import static org.assertj.core.api.Assertions.assertThat;

public class Document_Attach_Tax_Receipt_IntegTest extends CapexModuleIntegTestAbstract {

    @Before
    public void setupData() {
        runFixtureScript(new FixtureScript() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                executionContext.executeChild(this, LeaseItemForTax_enum.OxfTopModel001Gb.builder());
                executionContext.executeChild(this, new DocumentTypesAndTemplatesForCapexFixture());
                executionContext.executeChild(this, IncomingGenericPdf_enum.FakeTaxReceiptGbr.builder().setRunAs("estatio-user-gb"));
            }
        });
    }

    @Test
    public void attach_tax_receipt_test() throws Exception {

        // given
        final LocalDate invoiceDueDate = new LocalDate(2018, 1, 1);
        Lease lease = LeaseItemForTax_enum.OxfTopModel001Gb.findUsing(serviceRegistry).getLease();
        lease.verifyUntil(invoiceDueDate);
        Lease_calculate mixin = new Lease_calculate(lease);
        wrap(mixin).exec(InvoiceRunType.NORMAL_RUN, Arrays.asList(LeaseItemType.RENT, LeaseItemType.TAX), invoiceDueDate, invoiceDueDate, invoiceDueDate.plusDays(1));
        transactionService.nextTransaction();
        assertThat(invoiceRepository.findByStatus(InvoiceStatus.NEW)).hasSize(1);


    }

    @Inject
    InvoiceRepository invoiceRepository;

}
