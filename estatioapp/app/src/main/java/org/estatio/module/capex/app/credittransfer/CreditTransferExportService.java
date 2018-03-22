package org.estatio.module.capex.app.credittransfer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;

import org.incode.module.document.dom.impl.docs.Document;

import org.estatio.module.capex.dom.documents.LookupAttachedPdfService;
import org.estatio.module.capex.dom.invoice.IncomingInvoice;
import org.estatio.module.capex.dom.invoice.IncomingInvoiceRepository;
import org.estatio.module.capex.dom.invoice.approval.IncomingInvoiceApprovalState;
import org.estatio.module.capex.dom.invoice.approval.IncomingInvoiceApprovalStateTransition;
import org.estatio.module.capex.dom.payment.CreditTransfer;
import org.estatio.module.capex.dom.payment.PaymentLine;
import org.estatio.module.capex.dom.task.Task;

@DomainService(
        nature = NatureOfService.DOMAIN,
        objectType = "org.estatio.module.capex.app.credittransfer.CreditTransferExportService"
)
public class CreditTransferExportService {

    @Programmatic
    public boolean isFirstUseBankAccount(final CreditTransfer creditTransfer){
        List<IncomingInvoice> otherInvoicesForThisBankAccount =
                incomingInvoiceRepository.findByBankAccount(creditTransfer.getSellerBankAccount())
                .stream()
                .filter(x->!invoicesInTransfer(creditTransfer).contains(x))
                .collect(Collectors.toList());
        if (otherInvoicesForThisBankAccount.isEmpty()) return true;
        return false;
    }

    private List<IncomingInvoice> invoicesInTransfer(final CreditTransfer creditTransfer){
        List<IncomingInvoice> result = new ArrayList<>();
        for (PaymentLine line : creditTransfer.getLines()){
            result.add(line.getInvoice());
        }
        return result;
    }

    @Programmatic
    public String getApprovalStateTransitionSummary(final IncomingInvoice invoice) {

        List<Task> approvalTasksForInvoice = new ArrayList<>();
        stateTransitionRepo.findByDomainObject(invoice).forEach(
                x-> addTaskIfApplicable(x, approvalTasksForInvoice)
        );
        StringBuilder builder = new StringBuilder();
        for (Task task : approvalTasksForInvoice){
            builder.append(task.getCompletedOn());
            builder.append(" ");
            builder.append(task.getCompletedBy());
            builder.append("/\n");
        }
        return builder.toString();
    }

    private void addTaskIfApplicable(final IncomingInvoiceApprovalStateTransition transition, List<Task> tasks){

        List<IncomingInvoiceApprovalState> applicableStates = Arrays.asList(
                IncomingInvoiceApprovalState.APPROVED,
                IncomingInvoiceApprovalState.APPROVED_BY_COUNTRY_DIRECTOR,
                IncomingInvoiceApprovalState.APPROVED_BY_CORPORATE_MANAGER
        );
        if (transition.getTask()!=null && transition.getTask().isCompleted() && applicableStates.contains(transition.getToState())){
            tasks.add(transition.getTask());
        }
    }

    @Programmatic
    public String getDescriptionSummary(final IncomingInvoice invoice) {
        // TODO: implement
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

    @Inject
    IncomingInvoiceRepository incomingInvoiceRepository;

    @Inject
    IncomingInvoiceApprovalStateTransition.Repository stateTransitionRepo;

}
