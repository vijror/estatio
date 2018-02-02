package org.estatio.module.budgetassignment.dom.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.services.message.MessageService;

import org.incode.module.base.dom.valuetypes.LocalDateInterval;

import org.estatio.module.budget.dom.budget.Budget;
import org.estatio.module.budget.dom.budgetcalculation.BudgetCalculation;
import org.estatio.module.budget.dom.budgetcalculation.BudgetCalculationType;
import org.estatio.module.budget.dom.budgetcalculation.Status;
import org.estatio.module.budget.dom.budgetitem.BudgetItem;
import org.estatio.module.budget.dom.partioning.PartitionItem;
import org.estatio.module.budget.dom.partioning.Partitioning;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationResult;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationResultLink;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationResultLinkRepository;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationResultRepository;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationRun;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationRunRepository;
import org.estatio.module.budgetassignment.dom.override.BudgetOverride;
import org.estatio.module.budgetassignment.dom.override.BudgetOverrideRepository;
import org.estatio.module.budgetassignment.dom.override.BudgetOverrideValue;
import org.estatio.module.charge.dom.Charge;
import org.estatio.module.invoice.dom.PaymentMethod;
import org.estatio.module.lease.dom.InvoicingFrequency;
import org.estatio.module.lease.dom.Lease;
import org.estatio.module.lease.dom.LeaseAgreementRoleTypeEnum;
import org.estatio.module.lease.dom.LeaseItem;
import org.estatio.module.lease.dom.LeaseItemType;
import org.estatio.module.lease.dom.LeaseRepository;
import org.estatio.module.lease.dom.LeaseStatus;
import org.estatio.module.lease.dom.LeaseTermForServiceCharge;
import org.estatio.module.lease.dom.LeaseTermRepository;
import org.estatio.module.lease.dom.occupancy.Occupancy;

@DomainService(nature = NatureOfService.DOMAIN)
public class BudgetAssignmentService {

    public List<BudgetCalculationRun> calculateResultsForLeases(final Budget budget, final BudgetCalculationType type){
        List<BudgetCalculationRun> results = new ArrayList<>();

        for (Lease lease : leasesWithActiveOccupations(budget)) {
            removeNewOverrideValues(lease);
            calculateOverrideValues(lease, budget);
            results.addAll(executeCalculationRuns(lease, budget, type));
        }

        return results;
    }

    List<Lease> leasesWithActiveOccupations(final Budget budget){
        List<Lease> result = new ArrayList<>();
        for (Lease lease : leaseRepository.findLeasesByProperty(budget.getProperty())){
            // TODO: this is an extra filter because currently occupancies can outrun terminated leases
            if (lease.getStatus()!=LeaseStatus.TERMINATED) {
                for (Occupancy occupancy : lease.getOccupancies()) {
                    if (occupancy.getInterval().overlaps(budget.getInterval())) {
                        result.add(lease);
                        break;
                    }
                }
            }
        }
        return result;
    }

    public List<BudgetCalculationRun> executeCalculationRuns(final Lease lease, final Budget budget, final BudgetCalculationType type){
        List<BudgetCalculationRun> result = new ArrayList<>();
        for (Partitioning partitioning : budget.getPartitioningsOfType(type)) {
            BudgetCalculationRun run = budgetCalculationRunRepository.findOrCreateBudgetCalculationRun(lease, partitioning);
            if (run.getStatus() == Status.NEW) {
                createBudgetCalculationResults(run);
            }
            result.add(run);
        }
        return result;
    }

    public void createBudgetCalculationResults(final BudgetCalculationRun run){

        run.removeCalculationResults();
        for (Partitioning partitioning : run.getBudget().getPartitionings()){
            for (Charge invoiceCharge : partitioning.getDistinctInvoiceCharges()){
                BudgetCalculationResult result = run.createCalculationResult(invoiceCharge);
                result.calculate();
            }
        }

    }

    public List<BudgetOverrideValue> calculateOverrideValues(final Lease lease, final Budget budget){
        List<BudgetOverrideValue> results = new ArrayList<>();
        for (BudgetOverride override : budgetOverrideRepository.findByLease(lease)) {
            results.addAll(override.findOrCreateValues(budget.getStartDate()));
        }
        return results;
    }

    public void removeNewOverrideValues(final Lease lease){
        for (BudgetOverride override : budgetOverrideRepository.findByLease(lease)) {
            for (BudgetOverrideValue value : override.getValues()){
                value.removeWithStatusNew();
            }
        }
    }

    public void assignForType(final Budget budget, final BudgetCalculationType budgetCalculationType){
        switch (budgetCalculationType){
        case BUDGETED:
            for (Partitioning partitioning : budget.getPartitioningsOfType(BudgetCalculationType.BUDGETED)) {
                assignForBudgeted(partitioning);
            }
            break;
        case ACTUAL:
            for (Partitioning partitioning : budget.getPartitioningsOfType(BudgetCalculationType.ACTUAL)) {
                assignForActual(partitioning);
            }
            break;
        }
    }

    void assignForBudgeted(final Partitioning partitioning){
        for (BudgetCalculationRun run : budgetCalculationRunRepository.findByPartitioningAndStatus(partitioning, Status.NEW)){
            for (BudgetCalculationResult resultForLease : run.getBudgetCalculationResults()){

                LocalDate termStartDate = run.getLease().getStartDate().isAfter(partitioning.getStartDate()) ?
                        run.getLease().getStartDate() :
                        partitioning.getStartDate();

                LeaseItem leaseItem = findOrCreateLeaseItemForServiceCharge(run.getLease(), resultForLease, termStartDate);

                LeaseTermForServiceCharge leaseTerm = (LeaseTermForServiceCharge) leaseTermRepository.findOrCreateWithStartDate(leaseItem, new LocalDateInterval(termStartDate, partitioning.getEndDate()));

                budgetCalculationResultLinkRepository.findOrCreateLink(resultForLease, leaseTerm);

                leaseTerm.setBudgetedValue(resultForLease.getValue());
            }

            run.finalizeRun();
        }
    }

    void assignForActual(final Partitioning partitioning){
        for (BudgetCalculationRun run : budgetCalculationRunRepository.findByPartitioningAndStatus(partitioning, Status.NEW)){
            // TODO: for the moment we have just one partition for budgeted. This may change
            final Partitioning partitioningForBudgeting = partitioning.getBudget().getPartitioningForBudgeting();
            final List<BudgetCalculationRun> assignedRunsForLeaseBudgeted = budgetCalculationRunRepository.findByLeaseAndPartitioningAndStatus(run.getLease(), partitioningForBudgeting, Status.ASSIGNED);
            // TODO: for the moment we know there will be only one at most - this will change when we allow for more assigned runs on a lease
            if (assignedRunsForLeaseBudgeted.size()==1) {
                BudgetCalculationRun runForBudgeted = assignedRunsForLeaseBudgeted.get(0);
                for (BudgetCalculationResult result : run.getBudgetCalculationResults()){
                    // only a term that is controlled by this budget should be updated with an audited value when the audited value is empty
                    // so there should be a 'corresponding budgeted result' linked
                    BudgetCalculationResult correspondingBudgetedResult = budgetCalculationResultRepository.findUnique(runForBudgeted, result.getInvoiceCharge());
                    final List<BudgetCalculationResultLink> linksForCorrespondingBudgetedResult = budgetCalculationResultLinkRepository.findByCalculationResult(correspondingBudgetedResult);
                    if (linksForCorrespondingBudgetedResult.size()==1){
                        // this should always be the case
                        LeaseTermForServiceCharge termToBeUpdated = linksForCorrespondingBudgetedResult.get(0).getLeaseTermForServiceCharge();
                        if (termToBeUpdated.getAuditedValue()==null) {
                            termToBeUpdated.setAuditedValue(result.getValue());
                            budgetCalculationResultLinkRepository.findOrCreateLink(result, termToBeUpdated);
                            result.finalizeCalculationResult();
                        } else {
                            // this should not happen
                            final String message = String.format("Could not update term with id %s because an audited value was found", termToBeUpdated.getId()) ;
                            messageService.warnUser(message);
                        }
                    } else {
                        // this should not happen
                        final String message = String.format("No or more than 1 calculation result links were found for %s", run.getLease().getReference()) ;
                        messageService.warnUser(message);
                    }
                }
            }
            // TODO: for a run of type actual 'assigned' does not mean that all results were assigned - it just means that it tried to reconcile and assign at the time is was executed
            run.setStatus(Status.ASSIGNED);
        }
    }

    LeaseItem findOrCreateLeaseItemForServiceCharge(final Lease lease, final BudgetCalculationResult calculationResult, final LocalDate startDate){

        LeaseItem leaseItem = lease.findFirstActiveItemOfTypeAndChargeOnDate(LeaseItemType.SERVICE_CHARGE, calculationResult.getInvoiceCharge(), startDate);

        if (leaseItem==null){
            LeaseItem itemToCopyFrom = findItemToCopyFrom(lease); // try to copy invoice frequency and payment method from another lease item
            leaseItem = lease.newItem(
                    LeaseItemType.SERVICE_CHARGE,
                    LeaseAgreementRoleTypeEnum.LANDLORD,
                    calculationResult.getInvoiceCharge(),
                    itemToCopyFrom!=null ? itemToCopyFrom.getInvoicingFrequency() : InvoicingFrequency.QUARTERLY_IN_ADVANCE,
                    itemToCopyFrom!=null ? itemToCopyFrom.getPaymentMethod() : PaymentMethod.DIRECT_DEBIT,
                    startDate);
        }
        return leaseItem;
    }

    LeaseItem findItemToCopyFrom(final Lease lease){
        LeaseItem itemToCopyFrom = lease.findFirstItemOfType(LeaseItemType.SERVICE_CHARGE);
        if (itemToCopyFrom==null){
            // then try rent item
            itemToCopyFrom = lease.findFirstItemOfType(LeaseItemType.RENT);
        }
        if (itemToCopyFrom==null && lease.getItems().size()>0) {
            // then try any item
            itemToCopyFrom = lease.getItems().first();
        }
        return itemToCopyFrom;
    }

    public List<CalculationResultViewModel> getCalculationResults(final Budget budget){

        List<CalculationResultViewModel> results = new ArrayList<>();
        // TODO: for the moment we have just one partition for budgeted. This may change
        final Partitioning partitioningForBudgeting = budget.getPartitioningForBudgeting();
        for (BudgetCalculationRun run : budgetCalculationRunRepository.findByPartitioning(partitioningForBudgeting)){
            for (BudgetCalculationResult result : run.getBudgetCalculationResults()){
                CalculationResultViewModel vm = new CalculationResultViewModel(
                        run.getLease(),
                        result.getInvoiceCharge(),
                        run.getType()==BudgetCalculationType.BUDGETED ? result.getValue().add(result.getShortfall()) : BigDecimal.ZERO,
                        run.getType()==BudgetCalculationType.BUDGETED ? result.getValue() :BigDecimal.ZERO ,
                        run.getType()==BudgetCalculationType.BUDGETED ? result.getShortfall() : BigDecimal.ZERO,
                        run.getType()==BudgetCalculationType.ACTUAL ? result.getValue().add(result.getShortfall()) : BigDecimal.ZERO,
                        run.getType()==BudgetCalculationType.ACTUAL ? result.getValue() :BigDecimal.ZERO ,
                        run.getType()==BudgetCalculationType.ACTUAL ? result.getShortfall(): BigDecimal.ZERO
                );
                String unitString = run.getLease().getOccupancies().first().getUnit().getReference();
                if (run.getLease().getOccupancies().size()>1) {
                    boolean skip = true;
                    for (Occupancy occupancy : run.getLease().getOccupancies()){
                        if (skip){
                            skip = false;
                        } else {
                            unitString = unitString.concat(" | ").concat(occupancy.getUnit().getReference());
                        }
                    }
                }
                vm.setUnit(unitString);
                results.add(vm);
            }
        }
        return results;
    }


    public List<DetailedCalculationResultViewmodel> getDetailedCalculationResults(final Lease lease, final Budget budget, final BudgetCalculationType type){

        List<DetailedCalculationResultViewmodel> results = new ArrayList<>();

        for (Partitioning partitioning : budget.getPartitioningsOfType(type)) {

            results.addAll(getDetailedCalculationResults(lease, partitioning));

        }

        return results;
    }


    List<DetailedCalculationResultViewmodel> getDetailedCalculationResults(final Lease lease, final Partitioning partitioning){

        List<DetailedCalculationResultViewmodel> results = new ArrayList<>();

        BudgetCalculationRun runForLease = budgetCalculationRunRepository.findUnique(lease, partitioning);
        if (runForLease==null){return results;}

        for (BudgetCalculationResult result : runForLease.getBudgetCalculationResults()){

            // scenario: one override for incoming charge
            if (result.overrideValueForInvoiceCharge() != null){

                results.add(new DetailedCalculationResultViewmodel(
                        lease.primaryOccupancy().get().getUnit(),
                        "Override for total " + result.getInvoiceCharge().getDescription(),
                        result.getValue().add(result.getShortfall()),
                        result.getValue(),
                        result.getShortfall(),
                        null,
                        result.getInvoiceCharge()
                ));

            } else {

                for (BudgetCalculation calculation : result.getBudgetCalculations()) {

                    BigDecimal effectiveValueForIncomingCharge = calculation.getEffectiveValue();
                    BigDecimal shortFallForIncomingCharge = BigDecimal.ZERO;
                    BigDecimal valueInBudget = BigDecimal.ZERO;

                    DetailedCalculationResultViewmodel vm = new DetailedCalculationResultViewmodel(
                            calculation.getUnit(),
                            calculation.getIncomingCharge().getDescription(),
                            calculation.getEffectiveValue(),
                            valueInBudget,
                            effectiveValueForIncomingCharge,
                            shortFallForIncomingCharge,
                            calculation.getInvoiceCharge()
                    );

                    // set value in Budget
                    PartitionItem partitionItem = calculation.getPartitionItem();
                    BudgetItem budgetItem = calculation.getBudgetItem();
                    BigDecimal valueForBudgetItem = partitioning.getType() == BudgetCalculationType.BUDGETED ? budgetItem.getBudgetedValue() : budgetItem.getAuditedValue();
                    valueInBudget = valueForBudgetItem;
                    vm.setTotalValueInBudget(valueInBudget);

                    // set possible overrides for incoming charge
                    for (BudgetOverrideValue overrideValue : result.getOverrideValues()) {
                        if (overrideValue.getBudgetOverride().getIncomingCharge() == calculation.getIncomingCharge() && overrideValue.getType() == partitioning.getType()) {
                            effectiveValueForIncomingCharge = overrideValue.getValue().multiply(calculation.getPartitionItem().getPartitioning().getFractionOfYear());
                            shortFallForIncomingCharge = calculation.getEffectiveValue().subtract(effectiveValueForIncomingCharge);
                        }
                    }
                    if (effectiveValueForIncomingCharge != BigDecimal.ZERO) {
                        vm.setEffectiveValueForLease(effectiveValueForIncomingCharge);
                        vm.setShortfall(shortFallForIncomingCharge);
                    }
                    results.add(vm);
                }

            }

        }

        return results;
    }

    @Inject
    LeaseRepository leaseRepository;

    @Inject
    private LeaseTermRepository leaseTermRepository;

    @Inject
    private BudgetOverrideRepository budgetOverrideRepository;

    @Inject
    BudgetCalculationRunRepository budgetCalculationRunRepository;

    @Inject
    BudgetCalculationResultRepository budgetCalculationResultRepository;

    @Inject
    BudgetCalculationResultLinkRepository budgetCalculationResultLinkRepository;

    @Inject
    MessageService messageService;

}
