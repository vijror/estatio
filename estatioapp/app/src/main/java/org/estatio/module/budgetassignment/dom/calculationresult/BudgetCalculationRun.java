package org.estatio.module.budgetassignment.dom.calculationresult;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.Unique;
import javax.jdo.annotations.VersionStrategy;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;

import org.isisaddons.module.security.dom.tenancy.ApplicationTenancy;

import org.incode.module.base.dom.utils.TitleBuilder;

import org.estatio.module.base.dom.UdoDomainObject2;
import org.estatio.module.budget.dom.budget.Budget;
import org.estatio.module.budget.dom.budgetcalculation.BudgetCalculationType;
import org.estatio.module.budget.dom.budgetcalculation.Status;
import org.estatio.module.budget.dom.partioning.Partitioning;
import org.estatio.module.charge.dom.Charge;
import org.estatio.module.lease.dom.Lease;

import lombok.Getter;
import lombok.Setter;

@javax.jdo.annotations.PersistenceCapable(
        identityType = IdentityType.DATASTORE
        ,schema = "dbo" // Isis' ObjectSpecId inferred from @DomainObject#objectType
)
@javax.jdo.annotations.DatastoreIdentity(
        strategy = IdGeneratorStrategy.NATIVE,
        column = "id")
@javax.jdo.annotations.Version(
        strategy = VersionStrategy.VERSION_NUMBER,
        column = "version")
@javax.jdo.annotations.Queries({
        @Query(
                name = "findUnique", language = "JDOQL",
                value = "SELECT " +
                        "FROM org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationRun " +
                        "WHERE lease == :lease && "
                        + "partitioning == :partitioning"),
        @Query(
                name = "findByLease", language = "JDOQL",
                value = "SELECT " +
                        "FROM org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationRun " +
                        "WHERE lease == :lease"),
        @Query(
                name = "findByPartitioning", language = "JDOQL",
                value = "SELECT " +
                        "FROM org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationRun " +
                        "WHERE partitioning == :partitioning"),
        @Query(
                name = "findByPartitioningAndStatus", language = "JDOQL",
                value = "SELECT " +
                        "FROM org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationRun " +
                        "WHERE partitioning == :partitioning && status == :status"),
        @Query(
                name = "findByLeaseAndPartitioningAndStatus", language = "JDOQL",
                value = "SELECT " +
                        "FROM org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationRun " +
                        "WHERE lease == :lease && partitioning == :partitioning && status == :status")
})
@Unique(name = "BudgetCalculationRun_lease_partitioning_UNQ", members = { "lease", "partitioning" })

@DomainObject(
        objectType = "org.estatio.dom.budgetassignment.calculationresult.BudgetCalculationRun"
)
public class BudgetCalculationRun extends UdoDomainObject2<BudgetCalculationRun> {

    public BudgetCalculationRun() {
        super("lease, partitioning");
    }

    public String title(){
        return TitleBuilder
                .start()
                .withName(getLease())
                .withName(" - ")
                .withName(getPartitioning())
                .toString();
    }

    @Getter @Setter
    @Column(name = "leaseId", allowsNull = "false")
    private Lease lease;

    @Getter @Setter
    @Column(name = "partitioningId", allowsNull = "false")
    @PropertyLayout(hidden = Where.REFERENCES_PARENT)
    private Partitioning partitioning;

    @Getter @Setter
    @Column(allowsNull = "false")
    private Status status;

    @Getter @Setter
    @Persistent(mappedBy = "budgetCalculationRun", dependentElement = "true")
    private SortedSet<BudgetCalculationResult> budgetCalculationResults = new TreeSet<>();

    @Override public ApplicationTenancy getApplicationTenancy() {
        return getLease().getApplicationTenancy();
    }

    @Action(semantics = SemanticsOf.SAFE)
    public Budget getBudget(){
        return partitioning.getBudget();
    }

    @Action(semantics = SemanticsOf.SAFE)
    public BudgetCalculationType getType(){
        return partitioning.getType();
    }

    @Programmatic
    public BudgetCalculationResult createCalculationResult(final Charge invoiceCharge) {
        return budgetCalculationResultRepository.createBudgetCalculationResult(this, invoiceCharge);
    }

    @Programmatic
    public void remove() {
        remove(this);
    }

    @Programmatic
    public void finalizeRun(){
        for (BudgetCalculationResult calculationResult : getBudgetCalculationResults()){
            calculationResult.finalizeCalculationResult();
        }
        setStatus(Status.ASSIGNED);
    }

    @Programmatic
    public void removeCalculationResults() {
        for (BudgetCalculationResult result : getBudgetCalculationResults()){
            result.remove();
        }
    }

    @Inject
    private BudgetCalculationResultRepository budgetCalculationResultRepository;


}
