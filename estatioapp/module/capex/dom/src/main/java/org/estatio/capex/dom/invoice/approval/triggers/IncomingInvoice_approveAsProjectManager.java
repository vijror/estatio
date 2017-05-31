package org.estatio.capex.dom.invoice.approval.triggers;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Mixin;

import org.estatio.capex.dom.invoice.IncomingInvoice;
import org.estatio.capex.dom.invoice.approval.IncomingInvoiceApprovalStateTransition;
import org.estatio.capex.dom.invoice.approval.IncomingInvoiceApprovalStateTransitionType;
import org.estatio.capex.dom.task.Task;

@Mixin(method = "act")
public class IncomingInvoice_approveAsProjectManager extends IncomingInvoice_triggerAbstract {

    public IncomingInvoice_approveAsProjectManager(IncomingInvoice incomingInvoice) {
        super(incomingInvoice, IncomingInvoiceApprovalStateTransitionType.APPROVE_AS_PROJECT_MANAGER);
    }

    @Action()
    @MemberOrder(sequence = "2.2")
    public Object act(@Nullable final String comment) {
        return super.act(comment);
    }

    @Override
    public boolean hideAct() {
        return super.hideAct();
    }

}