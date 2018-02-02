package org.estatio.module.budgetassignment.dom.calculationresult;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;

import org.estatio.module.base.dom.UdoDomainRepositoryAndFactory;
import org.estatio.module.budget.dom.budget.Budget;
import org.estatio.module.budget.dom.budgetcalculation.Status;
import org.estatio.module.budget.dom.partioning.Partitioning;
import org.estatio.module.lease.dom.Lease;

@DomainService(repositoryFor = BudgetCalculationRun.class, nature = NatureOfService.DOMAIN)
public class BudgetCalculationRunRepository extends UdoDomainRepositoryAndFactory<BudgetCalculationRun> {

    public BudgetCalculationRunRepository() {
        super(BudgetCalculationRunRepository.class, BudgetCalculationRun.class);
    }

    public BudgetCalculationRun createBudgetCalculationRun(
            final Lease lease,
            final Partitioning partitioning,
            final Status status){

        BudgetCalculationRun budgetCalculationRun = newTransientInstance(BudgetCalculationRun.class);
        budgetCalculationRun.setLease(lease);
        budgetCalculationRun.setPartitioning(partitioning);
        budgetCalculationRun.setStatus(status);

        persist(budgetCalculationRun);

        return budgetCalculationRun;
    }

    public BudgetCalculationRun findOrCreateBudgetCalculationRun(
            final Lease lease,
            final Partitioning partitioning
    ){
        BudgetCalculationRun run = findUnique(lease, partitioning);
        return run== null ? createBudgetCalculationRun(lease, partitioning, Status.NEW) : run;
    }

    public BudgetCalculationRun findUnique(final Lease lease, final Partitioning partitioning){
        return uniqueMatch("findUnique", "lease", lease, "partitioning", partitioning);
    }

    public List<BudgetCalculationRun> allBudgetCalculationRuns(){
        return allInstances();
    }

    public List<BudgetCalculationRun> findByLease(final Lease lease) {
        return allMatches("findByLease", "lease", lease);
    }

    public List<BudgetCalculationRun> findByPartitioning(final Partitioning partitioning) {
        return allMatches("findByPartitioning", "partitioning", partitioning);
    }

    public List<BudgetCalculationRun> findByPartitioningAndStatus(final Partitioning partitioning, final Status status) {
        return allMatches("findByPartitioningAndStatus", "partitioning", partitioning, "status", status);
    }

    public List<BudgetCalculationRun> findByLeaseAndPartitioningAndStatus(final Lease lease, final Partitioning partitioning, final Status status) {
        return allMatches("findByLeaseAndPartitioningAndStatus", "lease", lease, "partitioning", partitioning, "status", status);
    }

    public List<BudgetCalculationRun> findByBudget(final Budget budget) {
        List<BudgetCalculationRun> result = new ArrayList<>();
        for (Partitioning partitioning : budget.getPartitionings()){
            result.addAll(findByPartitioning(partitioning));
        }
        return result;
    }
}

