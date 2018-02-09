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
import org.estatio.module.budget.dom.budgetcalculation.BudgetCalculationType;
import org.estatio.module.budget.dom.budgetcalculation.Status;
import org.estatio.module.budget.dom.partioning.Partitioning;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationResult;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationResultLink;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationResultLinkRepository;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationResultRepository;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationRun;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationRunRepository;
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

        Partitioning partitioningForActual = new Partitioning();
        BudgetCalculationRun run = new BudgetCalculationRun();

        // expect
        context.checking(new Expectations(){{
            oneOf(mockBudgetCalculationRunRepository).findByPartitioningAndStatus(partitioningForActual, Status.NEW);
            will(returnValue(Arrays.asList(run)));
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
        budgetAssignmentService = new BudgetAssignmentService(){
            @Override
            LeaseTermForServiceCharge findOrCreateLeaseTermToActualize(final BudgetCalculationResult resultForActual){
                return termToBeUpdated;
            }
        };
        budgetAssignmentService.budgetCalculationRunRepository = mockBudgetCalculationRunRepository;
        budgetAssignmentService.messageService = mockMessageService;

        Partitioning partitioningForActual = new Partitioning();
        BudgetCalculationRun runForActual = new BudgetCalculationRun();

        BudgetCalculationResult resultForActual = new BudgetCalculationResult();
        runForActual.getBudgetCalculationResults().add(resultForActual);

        // expect
        context.checking(new Expectations(){{
            oneOf(mockBudgetCalculationRunRepository).findByPartitioningAndStatus(partitioningForActual, Status.NEW);
            will(returnValue(Arrays.asList(runForActual)));
            oneOf(mockMessageService).warnUser(expectedMessage);
        }});

        // when
        termToBeUpdated.setAuditedValue(BigDecimal.ZERO);
        budgetAssignmentService.assignForActual(partitioningForActual);

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
        budgetAssignmentService = new BudgetAssignmentService(){
            @Override
            LeaseTermForServiceCharge findOrCreateLeaseTermToActualize(final BudgetCalculationResult resultForActual){
                return termToBeUpdated;
            }
        };
        budgetAssignmentService.budgetCalculationRunRepository = mockBudgetCalculationRunRepository;
        budgetAssignmentService.budgetCalculationResultLinkRepository = mockBudgetCalculationResultLinkRepository;

        Partitioning partitioningForActual = new Partitioning();

        BudgetCalculationRun runForActual = new BudgetCalculationRun();
        BudgetCalculationResult resultForActual = new BudgetCalculationResult(){
            @Override
            public void finalizeCalculationResult(){
            }
        };
        resultForActual.setValue(actualValue);
        runForActual.getBudgetCalculationResults().add(resultForActual);

        // expect
        context.checking(new Expectations(){{
            oneOf(mockBudgetCalculationRunRepository).findByPartitioningAndStatus(partitioningForActual, Status.NEW);
            will(returnValue(Arrays.asList(runForActual)));
            oneOf(mockBudgetCalculationResultLinkRepository).findOrCreateLink(resultForActual, termToBeUpdated);
        }});

        // when
        termToBeUpdated.setAuditedValue(null);
        budgetAssignmentService.assignForActual(partitioningForActual);

        // then
        assertThat(termToBeUpdated.getAuditedValue()).isEqualTo(resultForActual.getValue());
        assertThat(runForActual.getStatus()).isEqualTo(Status.ASSIGNED);

    }

    @Test
    public void find_candidate_terms_to_actualize_works() throws Exception {

        // given
        Partitioning partitioning = new Partitioning();
        partitioning.setStartDate(new LocalDate(2018, 7,1));
        partitioning.setEndDate(new LocalDate(2018, 11, 15));
        LeaseTermForServiceCharge overlappingTerm1 = new LeaseTermForServiceCharge();
        overlappingTerm1.setStartDate(new LocalDate(2018, 1, 1));
        overlappingTerm1.setEndDate(new LocalDate(2018, 7, 1));
        LeaseItem leaseItem1 = new LeaseItem();
        overlappingTerm1.setLeaseItem(leaseItem1);
        leaseItem1.getTerms().add(overlappingTerm1);

        LeaseTermForServiceCharge overlappingTerm2 = new LeaseTermForServiceCharge();
        overlappingTerm2.setStartDate(new LocalDate(2018, 11, 15));
        overlappingTerm2.setEndDate(new LocalDate(2018, 12, 31));
        LeaseTermForServiceCharge noOverlapTerm = new LeaseTermForServiceCharge();
        noOverlapTerm.setStartDate(new LocalDate(2018, 1, 1));
        noOverlapTerm.setEndDate(new LocalDate(2018, 6, 30));
        LeaseItem leaseItem2 = new LeaseItem();
        overlappingTerm2.setLeaseItem(leaseItem2);
        noOverlapTerm.setLeaseItem(leaseItem2);
        leaseItem2.getTerms().add(overlappingTerm2);
        leaseItem2.getTerms().add(noOverlapTerm);


        BudgetCalculationResult resultForBudgeted1 = new BudgetCalculationResult();
        BudgetCalculationResult resultForBudgeted2 = new BudgetCalculationResult();
        BudgetCalculationResult resultForBudgeted3 = new BudgetCalculationResult();
        BudgetAssignmentService budgetAssignmentService = new BudgetAssignmentService(){
            @Override
            List<BudgetCalculationResult> findByBudgetAndLeaseAndChargeAndTypeAndStatus(final Budget budget, final Lease lease, final Charge invoiceCharge, final BudgetCalculationType type, final Status status) {
                return Arrays.asList(resultForBudgeted1, resultForBudgeted2, resultForBudgeted3);
            }
        };
        budgetAssignmentService.budgetCalculationResultLinkRepository = mockBudgetCalculationResultLinkRepository;
        BudgetCalculationResultLink link1 = new BudgetCalculationResultLink();
        link1.setLeaseTermForServiceCharge(overlappingTerm1);
        BudgetCalculationResultLink link2 = new BudgetCalculationResultLink();
        link2.setLeaseTermForServiceCharge(overlappingTerm2);

        Lease lease = new Lease();
        Budget budget = new Budget();
        partitioning.setBudget(budget);
        BudgetCalculationRun run = new BudgetCalculationRun();
        run.setLease(lease);
        run.setPartitioning(partitioning);
        BudgetCalculationResult resultForActual = new BudgetCalculationResult();
        resultForActual.setBudgetCalculationRun(run);

        // expect
        context.checking(new Expectations(){{
            oneOf(mockBudgetCalculationResultLinkRepository).findByCalculationResult(resultForBudgeted1);
            will(returnValue(Arrays.asList(link1)));
            oneOf(mockBudgetCalculationResultLinkRepository).findByCalculationResult(resultForBudgeted2);
            will(returnValue(Arrays.asList(link2)));
            oneOf(mockBudgetCalculationResultLinkRepository).findByCalculationResult(resultForBudgeted3);
            will(returnValue(Arrays.asList()));
        }});

        // when
        List<LeaseTermForServiceCharge> result = budgetAssignmentService.findCandidateTermsToActualize(resultForActual);

        // then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).contains(overlappingTerm1);
        assertThat(result).contains(overlappingTerm2);
        assertThat(result).doesNotContain(noOverlapTerm);
    }

    @Mock
    LeaseTermForServiceCharge mockLeaseTerm;

    @Test
    public void findOrCreateLeaseTermToActualize_works_with_term_split() throws Exception {

        LocalDate partitioningEndDate;

        // given
        Partitioning partitioning = new Partitioning();
        partitioningEndDate = new LocalDate(2018, 10, 15);
        partitioning.setEndDate(partitioningEndDate);
        BudgetAssignmentService budgetAssignmentService = new BudgetAssignmentService(){
            @Override
            List<LeaseTermForServiceCharge> findCandidateTermsToActualize(final BudgetCalculationResult resultForActual){
                return Arrays.asList(mockLeaseTerm);
            }
        };

        Lease lease = new Lease();
        Budget budget = new Budget();
        partitioning.setBudget(budget);
        BudgetCalculationRun run = new BudgetCalculationRun();
        run.setLease(lease);
        run.setPartitioning(partitioning);
        BudgetCalculationResult resultForActual = new BudgetCalculationResult();
        resultForActual.setBudgetCalculationRun(run);

        // expect
        context.checking(new Expectations(){{
            oneOf(mockLeaseTerm).getEndDate();
            will(returnValue(partitioningEndDate.plusDays(1))); // splitting needed
            oneOf(mockLeaseTerm).split(partitioning.getEndDate().plusDays(1));
        }});

        // when
        LeaseTermForServiceCharge termToActualize = budgetAssignmentService.findOrCreateLeaseTermToActualize(resultForActual);

        // then
        assertThat(termToActualize).isEqualTo(mockLeaseTerm);
    }

    @Test
    public void findOrCreateLeaseTermToActualize_works_without_term_split() throws Exception {

        LocalDate partitioningEndDate;

        // given
        Partitioning partitioning = new Partitioning();
        partitioningEndDate = new LocalDate(2018, 10, 15);
        partitioning.setEndDate(partitioningEndDate);
        BudgetAssignmentService budgetAssignmentService = new BudgetAssignmentService(){
            @Override
            List<LeaseTermForServiceCharge> findCandidateTermsToActualize(final BudgetCalculationResult resultForActual){
                return Arrays.asList(mockLeaseTerm);
            }
        };

        Lease lease = new Lease();
        Budget budget = new Budget();
        partitioning.setBudget(budget);
        BudgetCalculationRun run = new BudgetCalculationRun();
        run.setLease(lease);
        run.setPartitioning(partitioning);
        BudgetCalculationResult resultForActual = new BudgetCalculationResult();
        resultForActual.setBudgetCalculationRun(run);

        // expect
        context.checking(new Expectations(){{
            oneOf(mockLeaseTerm).getEndDate(); // no splitting needed
            will(returnValue(partitioningEndDate));
        }});

        // when
        LeaseTermForServiceCharge termToActualize = budgetAssignmentService.findOrCreateLeaseTermToActualize(resultForActual);

        // then
        assertThat(termToActualize).isEqualTo(mockLeaseTerm);
    }

    @Test
    public void findOrCreateLeaseTermToActualize_works_when_ambiguous() throws Exception {

        // given

        LeaseTermForServiceCharge term1 = new LeaseTermForServiceCharge();
        LeaseTermForServiceCharge term2 = new LeaseTermForServiceCharge();
        BudgetAssignmentService budgetAssignmentService = new BudgetAssignmentService(){
            @Override
            List<LeaseTermForServiceCharge> findCandidateTermsToActualize(final BudgetCalculationResult resultForActual){
                return Arrays.asList(term1, term2);
            }
        };
        budgetAssignmentService.messageService = mockMessageService;
        Lease lease = new Lease();
        lease.setReference("LREF-1234");
        BudgetCalculationRun run = new BudgetCalculationRun();
        run.setLease(lease);
        BudgetCalculationResult resultForActual = new BudgetCalculationResult();
        resultForActual.setBudgetCalculationRun(run);
        Charge charge = new Charge();
        charge.setReference("CHREF-456");
        resultForActual.setInvoiceCharge(charge);

        // expect
        context.checking(new Expectations(){{
            oneOf(mockMessageService).warnUser("More than 1 term to update found for lease with reference LREF-1234 and charge CHREF-456");
        }});

        // when
        budgetAssignmentService.findOrCreateLeaseTermToActualize(resultForActual);

    }

} 
