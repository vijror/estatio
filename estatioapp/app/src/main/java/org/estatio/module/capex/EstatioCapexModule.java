package org.estatio.module.capex;

import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Sets;

import org.apache.isis.applib.Module;
import org.apache.isis.applib.ModuleAbstract;
import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.applib.fixturescripts.teardown.TeardownFixtureAbstract2;

import org.incode.module.document.DocumentModule;

import org.estatio.canonical.EstatioCanonicalModule;
import org.estatio.module.assetfinancial.EstatioAssetFinancialModule;
import org.estatio.module.budget.EstatioBudgetModule;
import org.estatio.module.capex.dom.bankaccount.verification.BankAccountVerificationStateTransition;
import org.estatio.module.capex.dom.coda.CodaElement;
import org.estatio.module.capex.dom.coda.CodaMapping;
import org.estatio.module.capex.dom.documents.categorisation.IncomingDocumentCategorisationStateTransition;
import org.estatio.module.capex.dom.invoice.IncomingInvoice;
import org.estatio.module.capex.dom.invoice.IncomingInvoiceItem;
import org.estatio.module.capex.dom.invoice.approval.IncomingInvoiceApprovalStateTransition;
import org.estatio.module.capex.dom.order.Order;
import org.estatio.module.capex.dom.order.OrderItem;
import org.estatio.module.capex.dom.order.approval.OrderApprovalStateTransition;
import org.estatio.module.capex.dom.order.paperclips.PaperclipForOrder;
import org.estatio.module.capex.dom.orderinvoice.OrderItemInvoiceItemLink;
import org.estatio.module.capex.dom.payment.PaymentBatch;
import org.estatio.module.capex.dom.payment.approval.PaymentBatchApprovalStateTransition;
import org.estatio.module.capex.dom.project.Project;
import org.estatio.module.capex.dom.project.ProjectItem;
import org.estatio.module.capex.dom.project.ProjectRole;
import org.estatio.module.capex.dom.project.ProjectTerm;
import org.estatio.module.capex.dom.task.Task;
import org.estatio.module.invoice.EstatioInvoiceModule;
import org.estatio.module.invoice.dom.Invoice;
import org.estatio.module.invoice.dom.InvoiceItem;
import org.estatio.module.invoice.dom.attr.InvoiceAttribute;
import org.estatio.module.invoice.dom.paperclips.PaperclipForInvoice;
import org.estatio.module.order.EstatioOrderAttributeModule;

@XmlRootElement(name = "module")
public class EstatioCapexModule extends ModuleAbstract {

    public EstatioCapexModule() {}

    @Override
    public Set<Module> getDependencies(){
        return Sets.newHashSet(
                new EstatioInvoiceModule(),
                new EstatioAssetFinancialModule(),
                new EstatioBudgetModule(),

                new DocumentModule(),
                new EstatioCanonicalModule(),
                new EstatioOrderAttributeModule()
        );
    }


    @Override
    public FixtureScript getTeardownFixture() {
        return new TeardownFixtureAbstract2() {

            @Override
            protected void execute(final FixtureScript.ExecutionContext executionContext) {

                String schema;
                String sql;
                String table;

                deleteFrom(PaymentBatchApprovalStateTransition.class);
                deleteFrom(IncomingInvoiceApprovalStateTransition.class);
                deleteFrom(OrderApprovalStateTransition.class);
                deleteFrom(BankAccountVerificationStateTransition.class);
                deleteFrom(IncomingDocumentCategorisationStateTransition.class);
                deleteFrom(Task.class);

                deleteFrom(CodaMapping.class);
                deleteFrom(CodaElement.class);

                // OrderItemInvoiceItemLink
                schema = schemaOf(OrderItemInvoiceItemLink.class);
                table = tableOf(OrderItemInvoiceItemLink.class);
                sql = String.format(
                        "DELETE FROM \"%s\".\"%s\" "
                                + "WHERE \"%s\" IN "
                                + "(SELECT \"id\" FROM \"%s\".\"%s\" WHERE \"%s\" = '%s') ",
                        schema, table, "invoiceItemId",
                        schemaOf(InvoiceItem.class), tableOf(InvoiceItem.class), // supertype of IncomingInvoiceItem
                        discriminatorColumnOf(InvoiceItem.class),
                        discriminatorValueOf(IncomingInvoiceItem.class)
                );
                this.isisJdoSupport.executeUpdate(sql);

                // InvoiceAttribute
                schema = schemaOf(InvoiceAttribute.class);
                table = tableOf(InvoiceAttribute.class);
                sql = String.format(
                        "DELETE FROM \"%s\".\"%s\" "
                                + "WHERE \"%s\" IN "
                                + "(SELECT \"id\" FROM \"%s\".\"%s\" WHERE \"%s\" = '%s') ",
                        schema, table, "invoiceId",
                        schemaOf(Invoice.class), tableOf(Invoice.class), // supertype of IncomingInvoice
                        discriminatorColumnOf(Invoice.class),
                        discriminatorValueOf(IncomingInvoice.class)
                );
                this.isisJdoSupport.executeUpdate(sql);

                // PaperclipForInvoice
                schema = schemaOf(PaperclipForInvoice.class);
                table = tableOf(PaperclipForInvoice.class);
                sql = String.format(
                        "DELETE FROM \"%s\".\"%s\" "
                                + "WHERE \"%s\" IN "
                                + "(SELECT \"id\" FROM \"%s\".\"%s\" WHERE \"%s\" = '%s') ",
                        schema, table, "invoiceId",
                        schemaOf(Invoice.class), tableOf(Invoice.class), // supertype of IncomingInvoice
                        discriminatorColumnOf(Invoice.class),
                        discriminatorValueOf(IncomingInvoice.class)
                );
                this.isisJdoSupport.executeUpdate(sql);

                deleteFrom(IncomingInvoiceItem.class);
                deleteFrom(IncomingInvoice.class);

                deleteFrom(PaymentBatch.class);

                deleteFrom(PaperclipForOrder.class);
                deleteFrom(OrderItem.class);
                deleteFrom(Order.class);

                deleteFrom(ProjectTerm.class);
                deleteFrom(ProjectRole.class);
                deleteFrom(ProjectItem.class);
                deleteFrom(Project.class);
            }
        };
    }




}
