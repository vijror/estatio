package org.estatio.module.budgetassignment.dom.calculationresult;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;

import org.estatio.module.base.dom.UdoDomainRepositoryAndFactory;
import org.estatio.module.budget.dom.budget.Budget;
import org.estatio.module.budget.dom.budgetcalculation.Status;
import org.estatio.module.budget.dom.partioning.Partitioning;
import org.estatio.module.charge.dom.Charge;
import org.estatio.module.lease.dom.Lease;

@DomainService(repositoryFor = BudgetCalculationResult.class, nature = NatureOfService.DOMAIN)
public class BudgetCalculationResultRepository extends UdoDomainRepositoryAndFactory<BudgetCalculationResult> {

    public BudgetCalculationResultRepository() {
        super(BudgetCalculationResultRepository.class, BudgetCalculationResult.class);
    }

    public BudgetCalculationResult createBudgetCalculationResult(
            final Partitioning partitioning,
            final Lease lease,
            final Charge invoiceCharge){

        BudgetCalculationResult budgetCalculationResult = newTransientInstance(BudgetCalculationResult.class);
        budgetCalculationResult.setPartitioning(partitioning);
        budgetCalculationResult.setLease(lease);
        budgetCalculationResult.setInvoiceCharge(invoiceCharge);
        budgetCalculationResult.setStatus(Status.NEW);

        persist(budgetCalculationResult);

        return budgetCalculationResult;
    }

    public BudgetCalculationResult findOrCreateBudgetCalculationResult(
            final Partitioning partitioning,
            final Lease lease,
            final Charge invoiceCharge){
        BudgetCalculationResult result = findUnique(partitioning, lease, invoiceCharge);
        return result==null ? createBudgetCalculationResult(partitioning, lease, invoiceCharge) : result;
    }

    public BudgetCalculationResult findUnique(
            final Partitioning partitioning,
            final Lease lease,
            final Charge invoiceCharge) {
        return uniqueMatch("findUnique", "partitioning", partitioning, "lease", lease, "invoiceCharge", invoiceCharge);
    }

    public List<BudgetCalculationResult> findByPartitioning(final Partitioning partitioning){
                return allMatches("findByPartitioning", "partitioning", partitioning);
    }

    public List<BudgetCalculationResult> findByLease(final Lease lease){
        return allMatches("findByLease", "lease", lease);
    }

    public List<BudgetCalculationResult> findByLeaseAndPartitioning(final Lease lease, final Partitioning partitioning){
        return allMatches("findByLeaseAndPartitioning", "lease", lease, "partitioning", partitioning);
    }

    public List<BudgetCalculationResult> allBudgetCalculationResults(){
        return allInstances();
    }

    public List<BudgetCalculationResult> findByBudget(final Budget budget) {
        List<BudgetCalculationResult> result = new ArrayList<>();
        for (Partitioning partitioning : budget.getPartitionings()){
            result.addAll(findByPartitioning(partitioning));
        }
        return result;
    }

    public void removeNewCalculationResultsForLeaseAndPartitioning(final Lease lease, final Partitioning partitioning) {
        findByLeaseAndPartitioning(lease, partitioning).stream().filter(x->x.getStatus()==Status.NEW).forEach(x->x.remove());
    }
}

