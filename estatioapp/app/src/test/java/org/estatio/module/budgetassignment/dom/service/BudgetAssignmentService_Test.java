package org.estatio.module.budgetassignment.dom.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

import org.estatio.module.asset.dom.Property;
import org.estatio.module.budget.dom.budget.Budget;
import org.estatio.module.budget.dom.budgetcalculation.BudgetCalculation;
import org.estatio.module.budget.dom.budgetcalculation.Status;
import org.estatio.module.budget.dom.partioning.Partitioning;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationResult;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationResultLink;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationResultLinkRepository;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationResultRepository;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationRun;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationRunRepository;
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
import org.estatio.module.lease.dom.occupancy.Occupancy;

import static org.assertj.core.api.Assertions.assertThat;

public class BudgetAssignmentService_Test {

    BudgetAssignmentService budgetAssignmentService;
    Budget budget;
    Lease leaseWith1ActiveOccupancy;
    Lease leaseWith2ActiveOccupancies;
    Lease leaseWithNoActiveOccupancies;
    Lease leaseTerminated;
    Occupancy o1;
    Occupancy o2;
    Occupancy o3;
    Occupancy o4;
    Occupancy o5;

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Before
    public void before() throws Exception {
        budgetAssignmentService = new BudgetAssignmentService();

        o1 = new Occupancy();
        leaseWith1ActiveOccupancy = new Lease(){
            @Override
            public SortedSet<Occupancy> getOccupancies(){
                return new TreeSet<>(Arrays.asList(o1));
            }
        };

        o2 = new Occupancy();
        o3 = new Occupancy();
        leaseWith2ActiveOccupancies = new Lease(){
            @Override
            public SortedSet<Occupancy> getOccupancies(){
                return new TreeSet<>(Arrays.asList(o2, o3));
            }
        };

        o4 = new Occupancy();
        leaseWithNoActiveOccupancies = new Lease(){
            @Override
            public SortedSet<Occupancy> getOccupancies(){
                return new TreeSet<>(Arrays.asList(o4));
            }
        };

        o5 = new Occupancy();
        leaseTerminated = new Lease(){
            @Override
            public SortedSet<Occupancy> getOccupancies(){
                return new TreeSet<>(Arrays.asList(o5));
            }
        };

        budget = new Budget();
        LocalDate startDate = new LocalDate(2015,01,01);
        LocalDate endDate = new LocalDate(2015,12,31);
        budget.setStartDate(startDate);
        budget.setEndDate(endDate);
        LeaseRepository leaseRepository = new LeaseRepository(){
            @Override
            public List<Lease> findLeasesByProperty(final Property property) {
                return Arrays.asList(
                        leaseWith1ActiveOccupancy,
                        leaseWith2ActiveOccupancies,
                        leaseWithNoActiveOccupancies,
                        leaseTerminated);
            }
        };
        budgetAssignmentService.leaseRepository = leaseRepository;

    }

    @Test
    public void leasesWithActiveOccupanciesTest() {

        // given
        o1.setStartDate(new LocalDate(2015,01,01));
        o2.setStartDate(new LocalDate(2015,01,01));
        o3.setStartDate(new LocalDate(2015,01,01));
        o4.setEndDate(new LocalDate(2014,12,31));
        o5.setStartDate(new LocalDate(2015,01,01));
        leaseTerminated.setStatus(LeaseStatus.TERMINATED);

        // when
        List<Lease> leasesfound = budgetAssignmentService.leasesWithActiveOccupations(budget);

        // then
        assertThat(leasesfound.size()).isEqualTo(2);
        assertThat(leasesfound.get(0)).isEqualTo(leaseWith1ActiveOccupancy);
        assertThat(leasesfound.get(1)).isEqualTo(leaseWith2ActiveOccupancies);
    }


    @Test
    public void findOrCreateLeaseItemForServiceCharge_returns_active_item_when_found() throws Exception {

        // given
        LeaseItem itemToBeFound = new LeaseItem();
        Lease lease = new Lease(){
            @Override
            public LeaseItem findFirstActiveItemOfTypeAndChargeOnDate(final LeaseItemType leaseItemType, final Charge charge, final LocalDate date){
                return itemToBeFound;
            }
        };
        BudgetCalculationResult budgetCalculationResult = new BudgetCalculationResult();
        LocalDate termStartDate = new LocalDate(2018,1,1);

        // when
        LeaseItem itemFound = budgetAssignmentService.findOrCreateLeaseItemForServiceCharge(lease, budgetCalculationResult, termStartDate);

        // then
        assertThat(itemFound).isEqualTo(itemToBeFound);
        
    }

    @Mock Lease mockLease;

    @Test
    public void findOrCreateLeaseItemForServiceCharge_works_when_item_to_copy_from_found() throws Exception {

        // given
        LeaseItem leaseItemToCopyFrom = new LeaseItem();
        leaseItemToCopyFrom.setInvoicingFrequency(InvoicingFrequency.QUARTERLY_IN_ADVANCE);
        leaseItemToCopyFrom.setPaymentMethod(PaymentMethod.CHEQUE);
        BudgetAssignmentService budgetAssignmentService = new BudgetAssignmentService(){
            @Override
            LeaseItem findItemToCopyFrom(final Lease lease){
                return leaseItemToCopyFrom;
            }
        };
        Charge charge = new Charge();
        BudgetCalculationResult budgetCalculationResult = new BudgetCalculationResult();
        budgetCalculationResult.setInvoiceCharge(charge);
        LocalDate termStartDate = new LocalDate(2018,1,1);

        // expect
        context.checking(new Expectations(){{
            oneOf(mockLease).findFirstActiveItemOfTypeAndChargeOnDate(LeaseItemType.SERVICE_CHARGE, charge, termStartDate);
            will(returnValue(null));
            oneOf(mockLease).newItem(
                    LeaseItemType.SERVICE_CHARGE,
                    LeaseAgreementRoleTypeEnum.LANDLORD,
                    budgetCalculationResult.getInvoiceCharge(),
                    leaseItemToCopyFrom.getInvoicingFrequency(),
                    leaseItemToCopyFrom.getPaymentMethod(),
                    termStartDate);
        }});

        // when
        budgetAssignmentService.findOrCreateLeaseItemForServiceCharge(mockLease, budgetCalculationResult, termStartDate);
    }

    @Test
    public void findOrCreateLeaseItemForServiceCharge_works_when_no_item_to_copy_from_found() throws Exception {

        // given
        BudgetAssignmentService budgetAssignmentService = new BudgetAssignmentService(){
            @Override
            LeaseItem findItemToCopyFrom(final Lease lease){
                return null;
            }
        };
        Charge charge = new Charge();
        BudgetCalculationResult budgetCalculationResult = new BudgetCalculationResult();
        budgetCalculationResult.setInvoiceCharge(charge);
        LocalDate termStartDate = new LocalDate(2018,1,1);

        InvoicingFrequency invoicingFrequencyGuess = InvoicingFrequency.QUARTERLY_IN_ADVANCE;
        PaymentMethod paymentMethodGuess = PaymentMethod.DIRECT_DEBIT;

        // expect
        context.checking(new Expectations(){{
            oneOf(mockLease).findFirstActiveItemOfTypeAndChargeOnDate(LeaseItemType.SERVICE_CHARGE, charge, termStartDate);
            will(returnValue(null));
            oneOf(mockLease).newItem(
                    LeaseItemType.SERVICE_CHARGE,
                    LeaseAgreementRoleTypeEnum.LANDLORD,
                    budgetCalculationResult.getInvoiceCharge(),
                    invoicingFrequencyGuess,
                    paymentMethodGuess,
                    termStartDate);
        }});

        // when
        budgetAssignmentService.findOrCreateLeaseItemForServiceCharge(mockLease, budgetCalculationResult, termStartDate);
    }

    @Test
    public void itemToCopyFrom_when_no_items_on_lease_returns_null() throws Exception {

        // given
        Lease lease = new Lease();
        // when
        LeaseItem itemFound = budgetAssignmentService.findItemToCopyFrom(lease);
        // then
        assertThat(itemFound).isNull();

    }

    @Test
    public void itemToCopyFrom_with_lease_having_service_charge_item_works() throws Exception {

        // expect
        context.checking(new Expectations(){{
            oneOf(mockLease).findFirstItemOfType(LeaseItemType.SERVICE_CHARGE);
        }});

        // when
        budgetAssignmentService.findItemToCopyFrom(mockLease);

    }

    @Test
    public void itemToCopyFrom_with_lease_having_rent_item_works() throws Exception {

        // expect
        context.checking(new Expectations(){{
            oneOf(mockLease).findFirstItemOfType(LeaseItemType.SERVICE_CHARGE);
            will(returnValue(null));
            oneOf(mockLease).findFirstItemOfType(LeaseItemType.RENT);
        }});

        // when
        budgetAssignmentService.findItemToCopyFrom(mockLease);

    }

    @Test
    public void itemToCopyFrom_with_lease_having_no_rent_and_no_service_charge_item_works() throws Exception {

        // given
        LeaseItem anyItemOtherThanRentOrServiceCharge = new LeaseItem();

        // expect
        context.checking(new Expectations(){{
            oneOf(mockLease).findFirstItemOfType(LeaseItemType.SERVICE_CHARGE);
            will(returnValue(null));
            oneOf(mockLease).findFirstItemOfType(LeaseItemType.RENT);
            will(returnValue(null));
            allowing(mockLease).getItems();
            will(returnValue(new TreeSet(Arrays.asList(anyItemOtherThanRentOrServiceCharge))));
        }});

        // when
        LeaseItem itemToCopyFrom = budgetAssignmentService.findItemToCopyFrom(mockLease);

        // then
        assertThat(itemToCopyFrom).isEqualTo(anyItemOtherThanRentOrServiceCharge);

    }

    @Mock
    BudgetCalculationRunRepository mockBudgetCalculationRunRepository;
    
    @Test
    public void assignForActual_always_sets_run_status_to_assigned() throws Exception {

        // given
        budgetAssignmentService.budgetCalculationRunRepository = mockBudgetCalculationRunRepository;

        Partitioning partitioningForBudgeted = new Partitioning();
        Partitioning partitioningForActual = new Partitioning();
        budget = new Budget(){
            @Override
            public Partitioning getPartitioningForBudgeting(){
                return partitioningForBudgeted;
            };
        };
        partitioningForActual.setBudget(budget);

        BudgetCalculationRun run = new BudgetCalculationRun();
        run.setStatus(Status.NEW);
        Lease lease = new Lease();
        run.setLease(lease);

        // expect
        context.checking(new Expectations(){{
            oneOf(mockBudgetCalculationRunRepository).findByPartitioningAndStatus(partitioningForActual, Status.NEW);
            will(returnValue(Arrays.asList(run)));
            oneOf(mockBudgetCalculationRunRepository).findByLeaseAndPartitioningAndStatus(lease, partitioningForBudgeted, Status.ASSIGNED);
        }});

        // when
        budgetAssignmentService.assignForActual(partitioningForActual);
        // then
        assertThat(run.getStatus()).isEqualTo(Status.ASSIGNED);

    }

    @Mock
    BudgetCalculationResultRepository mockBudgetCalculationResultRepository;

    @Mock
    BudgetCalculationResultLinkRepository mockBudgetCalculationResultLinkRepository;

    @Mock
    MessageService mockMessageService;

    @Test
    public void assignForActual_does_not_update_terms_with_an_audited_value_set() throws Exception {

        LeaseTermForServiceCharge termToBeUpdated = new LeaseTermForServiceCharge(){
            @Override
            public  String getId(){
                return "123";
            }
        };

        // given
        final String expectedMessage = String.format("Could not update term with id %s because an audited value was found", termToBeUpdated.getId()) ;
        budgetAssignmentService.budgetCalculationRunRepository = mockBudgetCalculationRunRepository;
        budgetAssignmentService.budgetCalculationResultRepository = mockBudgetCalculationResultRepository;
        budgetAssignmentService.budgetCalculationResultLinkRepository = mockBudgetCalculationResultLinkRepository;
        budgetAssignmentService.messageService = mockMessageService;

        Partitioning partitioningForBudgeted = new Partitioning();
        Partitioning partitioningForActual = new Partitioning();
        budget = new Budget(){
            @Override
            public Partitioning getPartitioningForBudgeting(){
                return partitioningForBudgeted;
            };
        };
        partitioningForActual.setBudget(budget);

        BudgetCalculationRun runForActual = new BudgetCalculationRun();
        runForActual.setStatus(Status.NEW);
        Lease lease = new Lease();

        BudgetCalculation calculationForActual = new BudgetCalculation();
        calculationForActual.setStatus(Status.NEW);

        BudgetCalculationResult resultForActual = new BudgetCalculationResult(){
            @Override
            public List<BudgetCalculation> getBudgetCalculations(){
                return Arrays.asList(calculationForActual);
            }
        };
        Charge invoiceCharge = new Charge();
        resultForActual.setInvoiceCharge(invoiceCharge);

        runForActual.getBudgetCalculationResults().add(resultForActual);

        runForActual.setLease(lease);
        BudgetCalculationRun runForBudgeted = new BudgetCalculationRun();

        BudgetCalculationResult correspondingBudgetedResult = new BudgetCalculationResult();
        BudgetCalculationResultLink linkForCorrespondingBudgetedResult = new BudgetCalculationResultLink();

        linkForCorrespondingBudgetedResult.setLeaseTermForServiceCharge(termToBeUpdated);

        // expect
        context.checking(new Expectations(){{
            oneOf(mockBudgetCalculationRunRepository).findByPartitioningAndStatus(partitioningForActual, Status.NEW);
            will(returnValue(Arrays.asList(runForActual)));
            oneOf(mockBudgetCalculationRunRepository).findByLeaseAndPartitioningAndStatus(lease, partitioningForBudgeted, Status.ASSIGNED);
            will(returnValue(Arrays.asList(runForBudgeted)));
            oneOf(mockBudgetCalculationResultRepository).findUnique(runForBudgeted, invoiceCharge);
            will(returnValue(correspondingBudgetedResult));
            oneOf(mockBudgetCalculationResultLinkRepository).findByCalculationResult(correspondingBudgetedResult);
            will(returnValue(Arrays.asList(linkForCorrespondingBudgetedResult)));
            oneOf(mockMessageService).warnUser(expectedMessage);
        }});

        // when
        termToBeUpdated.setAuditedValue(BigDecimal.ZERO);
        budgetAssignmentService.assignForActual(partitioningForActual);

        // then still
        assertThat(calculationForActual.getStatus()).isEqualTo(Status.NEW);

    }

    @Test
    public void assignForActual_does_update_terms_with_no_audited_value_set() throws Exception {

        LeaseTermForServiceCharge termToBeUpdated = new LeaseTermForServiceCharge(){
            @Override
            public  String getId(){
                return "123";
            }
        };
        BigDecimal actualValue = new BigDecimal("12345.67");

        // given
        budgetAssignmentService.budgetCalculationRunRepository = mockBudgetCalculationRunRepository;
        budgetAssignmentService.budgetCalculationResultRepository = mockBudgetCalculationResultRepository;
        budgetAssignmentService.budgetCalculationResultLinkRepository = mockBudgetCalculationResultLinkRepository;

        Partitioning partitioningForBudgeted = new Partitioning();
        Partitioning partitioningForActual = new Partitioning();
        budget = new Budget(){
            @Override
            public Partitioning getPartitioningForBudgeting(){
                return partitioningForBudgeted;
            };
        };
        partitioningForActual.setBudget(budget);

        BudgetCalculationRun runForActual = new BudgetCalculationRun();
        runForActual.setStatus(Status.NEW);
        Lease lease = new Lease();

        BudgetCalculation calculationForActual = new BudgetCalculation();
        calculationForActual.setStatus(Status.NEW);

        BudgetOverrideValue overrideValueForActual = new BudgetOverrideValue();
        overrideValueForActual.setStatus(Status.NEW);

        BudgetCalculationResult resultForActual = new BudgetCalculationResult(){
            @Override
            public List<BudgetCalculation> getBudgetCalculations(){
                return Arrays.asList(calculationForActual);
            }
            @Override
            public List<BudgetOverrideValue> getOverrideValues(){
                return Arrays.asList(overrideValueForActual);
            }
        };
        Charge invoiceCharge = new Charge();
        resultForActual.setInvoiceCharge(invoiceCharge);
        resultForActual.setValue(actualValue);

        runForActual.getBudgetCalculationResults().add(resultForActual);

        runForActual.setLease(lease);
        BudgetCalculationRun runForBudgeted = new BudgetCalculationRun();

        BudgetCalculationResult correspondingBudgetedResult = new BudgetCalculationResult();
        BudgetCalculationResultLink linkForCorrespondingBudgetedResult = new BudgetCalculationResultLink();

        linkForCorrespondingBudgetedResult.setLeaseTermForServiceCharge(termToBeUpdated);

        // expect
        context.checking(new Expectations(){{
            oneOf(mockBudgetCalculationRunRepository).findByPartitioningAndStatus(partitioningForActual, Status.NEW);
            will(returnValue(Arrays.asList(runForActual)));
            oneOf(mockBudgetCalculationRunRepository).findByLeaseAndPartitioningAndStatus(lease, partitioningForBudgeted, Status.ASSIGNED);
            will(returnValue(Arrays.asList(runForBudgeted)));
            oneOf(mockBudgetCalculationResultRepository).findUnique(runForBudgeted, invoiceCharge);
            will(returnValue(correspondingBudgetedResult));
            oneOf(mockBudgetCalculationResultLinkRepository).findByCalculationResult(correspondingBudgetedResult);
            will(returnValue(Arrays.asList(linkForCorrespondingBudgetedResult)));
            oneOf(mockBudgetCalculationResultLinkRepository).findOrCreateLink(resultForActual, termToBeUpdated);
        }});

        // when
        termToBeUpdated.setAuditedValue(null);
        budgetAssignmentService.assignForActual(partitioningForActual);

        // then
        assertThat(termToBeUpdated.getAuditedValue()).isEqualTo(resultForActual.getValue());
        assertThat(calculationForActual.getStatus()).isEqualTo(Status.ASSIGNED);
        assertThat(overrideValueForActual.getStatus()).isEqualTo(Status.ASSIGNED);
        assertThat(runForActual.getStatus()).isEqualTo(Status.ASSIGNED);

    }

} 
