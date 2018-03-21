package org.estatio.module.capex.app.credittransfer;

import java.util.Optional;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;

import org.incode.module.document.dom.impl.docs.Document;

import org.estatio.module.capex.dom.documents.LookupAttachedPdfService;
import org.estatio.module.capex.dom.invoice.IncomingInvoice;

@DomainService(nature = NatureOfService.DOMAIN)
public class CreditTransferExportService {

    @Programmatic
    public String getApprovalStateSummary(final IncomingInvoice invoice) {
        // TODO: implement
        return null;
    }

    @Programmatic
    public String getDescriptionSummary(final IncomingInvoice invoice) {
        // TODO: implement getChargeSummary
        return null;
    }

    @Programmatic
    public String getInvoiceDocumentName(final IncomingInvoice invoice) {
        final Optional<Document> document = lookupAttachedPdfService.lookupIncomingInvoicePdfFrom(invoice);
        return document.isPresent() ? document.get().getName() : null;
    }

    @Programmatic
    public String getChargeSummary(final IncomingInvoice invoice) {
        // TODO: implement
        return null;
    }

    @Programmatic
    public String getProjectSummary(final IncomingInvoice invoice) {
        // TODO: implement
        return null;
    }

    @Programmatic
    public String getBudgetSummary(final IncomingInvoice invoice) {
        // TODO: implement
        return null;
    }

    @Programmatic
    public String getPropertySummary(final IncomingInvoice invoice) {
        // TODO: implement
        return null;
    }

    @Inject
    LookupAttachedPdfService lookupAttachedPdfService;

}
