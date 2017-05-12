package org.estatio.capex.dom.invoice.approval;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;

import org.joda.time.LocalDateTime;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;

import org.estatio.capex.dom.invoice.IncomingInvoice;
import org.estatio.capex.dom.state.StateTransitionAbstract;
import org.estatio.capex.dom.state.StateTransitionRepositoryAbstract;
import org.estatio.capex.dom.task.Task;

import lombok.Getter;
import lombok.Setter;

@PersistenceCapable(
        identityType = IdentityType.DATASTORE,
        schema = "incomingInvoice",
        table = "IncomingInvoiceApprovalStateTransition"
)
@DatastoreIdentity(
        strategy = IdGeneratorStrategy.IDENTITY,
        column = "id"
)
@javax.jdo.annotations.Inheritance(
        strategy = InheritanceStrategy.NEW_TABLE)
@Queries({
        @Query(
                name = "findByDomainObject", language = "JDOQL",
                value = "SELECT "
                        + "FROM org.estatio.capex.dom.invoice.approval.IncomingInvoiceApprovalStateTransition "
                        + "WHERE invoice == :domainObject "
                        + "ORDER BY completedOn DESC "
        ),
        @Query(
                name = "findByDomainObjectAndCompleted", language = "JDOQL",
                value = "SELECT "
                        + "FROM org.estatio.capex.dom.invoice.approval.IncomingInvoiceApprovalStateTransition "
                        + "WHERE invoice == :domainObject "
                        + "&& completed == :completed "
                        + "ORDER BY completedOn DESC "
        ),
        @Query(
                name = "findByTask", language = "JDOQL",
                value = "SELECT "
                        + "FROM org.estatio.capex.dom.invoice.approval.IncomingInvoiceApprovalStateTransition "
                        + "WHERE task == :task "
        ),
})
@DomainObject(objectType = "incomingInvoice.IncomingInvoiceApprovalStateTransition" )
public class IncomingInvoiceApprovalStateTransition
        extends StateTransitionAbstract<
                    IncomingInvoice,
        IncomingInvoiceApprovalStateTransition,
        IncomingInvoiceApprovalStateTransitionType,
        IncomingInvoiceApprovalState> {

    /**
     * For the first transition, represents the initial state of the domain object
     * Thereafter, will hold the same value as the "to state" of the preceding transition.
     */
    @Column(allowsNull = "false")
    @Getter @Setter
    private IncomingInvoiceApprovalState fromState;

    @Column(allowsNull = "false")
    @Getter @Setter
    private IncomingInvoiceApprovalStateTransitionType transitionType;

    /**
     * If null, then this transition is not yet complete.
     */
    @Column(allowsNull = "true")
    @Getter @Setter
    private IncomingInvoiceApprovalState toState;

    @Column(allowsNull = "false", name = "invoiceId")
    @Getter @Setter
    private IncomingInvoice invoice;


    /**
     * Not every transition necessarily has a task.
     */
    @Column(allowsNull = "true", name = "taskId")
    @Getter @Setter
    private Task task;

    @Column(allowsNull = "false")
    @Getter @Setter
    private LocalDateTime createdOn;

    @Getter @Setter
    @Column(allowsNull = "true")
    private LocalDateTime completedOn;

    @Getter @Setter
    @Column(allowsNull = "false")
    private boolean completed;

    @Programmatic
    @Override
    public IncomingInvoice getDomainObject() {
        return getInvoice();
    }

    @Programmatic
    @Override
    public void setDomainObject(final IncomingInvoice domainObject) {
        setInvoice(domainObject);
    }

    @DomainService(
            nature = NatureOfService.DOMAIN,
            repositoryFor = IncomingInvoiceApprovalStateTransition.class
    )
    public static class Repository
            extends StateTransitionRepositoryAbstract<
                    IncomingInvoice,
            IncomingInvoiceApprovalStateTransition,
            IncomingInvoiceApprovalStateTransitionType,
            IncomingInvoiceApprovalState> {

        public Repository() {
            super(IncomingInvoiceApprovalStateTransition.class);
        }

    }
}