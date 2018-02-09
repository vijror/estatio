package org.estatio.module.budgetassignment.dom.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import org.estatio.module.lease.dom.LeaseTerm;
import org.estatio.module.lease.dom.LeaseTermForServiceCharge;
import org.estatio.module.lease.dom.LeaseTermRepository;
import org.estatio.module.lease.dom.occupancy.Occupancy;

@DomainService(nature = NatureOfService.DOMAIN)
public class BudgetAssignmentService {

    public void calculateResultsForLeases(final Budget budget, final BudgetCalculationType type){
        for (Lease lease : leasesWithActiveOccupations(budget)) {
            removeNewOverrideValues(lease);
            calculateOverrideValues(lease, budget);
            executeCalculationRun(lease, budget, type);
        }
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

    public void executeCalculationRun(final Lease lease, final Budget budget, final BudgetCalculationType type){
        for (Partitioning partitioning : budget.getPartitioningsOfType(type)) {
            createBudgetCalculationResults(lease, partitioning);
        }
    }

    public void createBudgetCalculationResults(final Lease lease, final Partitioning partitioning){
        budgetCalculationResultRepository.removeNewCalculationResultsForLeaseAndPartitioning(lease, partitioning);
        for (Charge invoiceCharge : partitioning.getDistinctInvoiceCharges()){
            BudgetCalculationResult result = budgetCalculationResultRepository.findOrCreateBudgetCalculationResult(partitioning, lease, invoiceCharge);
            if (result.getStatus()==Status.NEW) {
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

        for (BudgetCalculationResult resultForPartitioning : budgetCalculationResultRepository.findByPartitioning(partitioning)){
            if (resultForPartitioning.getStatus()==Status.NEW) {
                LocalDate termStartDate = resultForPartitioning.getLease().getStartDate().isAfter(partitioning.getStartDate()) ?
                        resultForPartitioning.getLease().getStartDate() :
                        partitioning.getStartDate();

                LeaseItem leaseItem = findOrCreateLeaseItemForServiceCharge(resultForPartitioning.getLease(), resultForPartitioning, termStartDate);

                LeaseTermForServiceCharge leaseTerm = (LeaseTermForServiceCharge) leaseTermRepository.findOrCreateWithStartDate(leaseItem, new LocalDateInterval(termStartDate, partitioning.getEndDate()));

                budgetCalculationResultLinkRepository.findOrCreateLink(resultForPartitioning, leaseTerm);

                leaseTerm.setBudgetedValue(resultForPartitioning.getValue());

                resultForPartitioning.finalizeCalculationResult();
            }
        }

    }

    void assignForActual(final Partitioning partitioning){

        for (BudgetCalculationResult result : budgetCalculationResultRepository.findByPartitioning(partitioning)) {
            if (result.getStatus()==Status.NEW) {
                LeaseTermForServiceCharge termToBeUpdated = findOrCreateLeaseTermToActualize(result);
                if (termToBeUpdated != null) {
                    if (termToBeUpdated.getAuditedValue() == null) {
                        termToBeUpdated.setAuditedValue(result.getValue());
                        budgetCalculationResultLinkRepository.findOrCreateLink(result, termToBeUpdated);
                        result.finalizeCalculationResult();
                    } else {
                        // this should not happen
                        final String message = String.format("Could not update term with id %s because an audited value was found", termToBeUpdated.getId());
                        messageService.warnUser(message);
                    }

                } else {
                    final String message = String.format("No term to update found for lease with reference %s", result.getLease().getReference());
                    messageService.warnUser(message);
                }
            }
        }

    }

    LeaseTermForServiceCharge findOrCreateLeaseTermToActualize(final BudgetCalculationResult resultForActual){

        if (findCandidateTermsToActualize(resultForActual).isEmpty()) return null;

        if (findCandidateTermsToActualize(resultForActual).size()>1) {
            final String message = String.format("More than 1 term to update found for lease with reference %s and charge %s", resultForActual.getLease().getReference(), resultForActual.getInvoiceCharge().getReference()) ;
            messageService.warnUser(message);
            return null;
        }

        LeaseTermForServiceCharge termToActualize = findCandidateTermsToActualize(resultForActual).get(0);

        // split if needed
        Partitioning partitioningForActual = resultForActual.getPartitioning();
        if (partitioningForActual.getEndDate().isBefore(termToActualize.getEndDate())){
            termToActualize.split(partitioningForActual.getEndDate().plusDays(1));
        }

        return termToActualize;
    }

    List<LeaseTermForServiceCharge> findCandidateTermsToActualize(final BudgetCalculationResult resultForActual){

        List<LeaseTermForServiceCharge> candidateTermsToActualize = new ArrayList<>();

        Lease leaseToUpdate = resultForActual.getLease();
        List<BudgetCalculationResult> budgetedResultsForCharge = findByBudgetAndLeaseAndChargeAndTypeAndStatus(resultForActual.getPartitioning().getBudget(), leaseToUpdate, resultForActual.getInvoiceCharge(), BudgetCalculationType.BUDGETED, Status.ASSIGNED);
        if (budgetedResultsForCharge.isEmpty()) return candidateTermsToActualize; // fail fast

        for (BudgetCalculationResult result : budgetedResultsForCharge){
            for (BudgetCalculationResultLink link : budgetCalculationResultLinkRepository.findByCalculationResult(result)){
                LeaseTermForServiceCharge linkedTerm = link.getLeaseTermForServiceCharge();
                LeaseItem linkedItem = linkedTerm.getLeaseItem();
                for (LeaseTerm candidate : linkedItem.getTerms()) {
                    if (candidate.getInterval().overlaps(resultForActual.getPartitioning().getInterval())) {
                        candidateTermsToActualize.add((LeaseTermForServiceCharge) candidate);
                    }
                }
            }
        }
        return candidateTermsToActualize;
    }

    List<BudgetCalculationResult> findByBudgetAndLeaseAndChargeAndTypeAndStatus(final Budget budget, final Lease lease, final Charge invoiceCharge, final BudgetCalculationType type, final Status status) {
        return budgetCalculationResultRepository.findByBudget(budget)
                .stream()
                .filter(x->x.getLease()==lease)
                .filter(x->x.getInvoiceCharge()==invoiceCharge)
                .filter(x->x.getPartitioning().getType()==type)
                .filter(x->x.getStatus()==status)
                .collect(Collectors.toList());
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

        for (BudgetCalculationResult result : budgetCalculationResultRepository.findByPartitioning(partitioningForBudgeting)){
            CalculationResultViewModel vm = new CalculationResultViewModel(
                    result.getLease(),
                    result.getInvoiceCharge(),
                    result.getPartitioning().getType()==BudgetCalculationType.BUDGETED ? result.getValue().add(result.getShortfall()) : BigDecimal.ZERO,
                    result.getPartitioning().getType()==BudgetCalculationType.BUDGETED ? result.getValue() :BigDecimal.ZERO ,
                    result.getPartitioning().getType()==BudgetCalculationType.BUDGETED ? result.getShortfall() : BigDecimal.ZERO,
                    result.getPartitioning().getType()==BudgetCalculationType.ACTUAL ? result.getValue().add(result.getShortfall()) : BigDecimal.ZERO,
                    result.getPartitioning().getType()==BudgetCalculationType.ACTUAL ? result.getValue() :BigDecimal.ZERO ,
                    result.getPartitioning().getType()==BudgetCalculationType.ACTUAL ? result.getShortfall(): BigDecimal.ZERO
            );
            String unitString = result.getLease().getOccupancies().first().getUnit().getReference();
            if (result.getLease().getOccupancies().size()>1) {
                boolean skip = true;
                for (Occupancy occupancy : result.getLease().getOccupancies()){
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

        for (BudgetCalculationResult result : budgetCalculationResultRepository.findByLeaseAndPartitioning(lease, partitioning)){

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

                    BigDecimal effectiveValueForIncomingCharge = calculation.getValue();
                    BigDecimal shortFallForIncomingCharge = BigDecimal.ZERO;
                    BigDecimal valueInBudget = BigDecimal.ZERO;

                    DetailedCalculationResultViewmodel vm = new DetailedCalculationResultViewmodel(
                            calculation.getUnit(),
                            calculation.getIncomingCharge().getDescription(),
                            calculation.getValue(),
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
                            shortFallForIncomingCharge = calculation.getValue().subtract(effectiveValueForIncomingCharge);
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
    BudgetCalculationResultRepository budgetCalculationResultRepository;

    @Inject
    BudgetCalculationResultLinkRepository budgetCalculationResultLinkRepository;

    @Inject
    MessageService messageService;

}
