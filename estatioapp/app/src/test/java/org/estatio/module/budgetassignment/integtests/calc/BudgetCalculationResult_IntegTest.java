package org.estatio.module.budgetassignment.integtests.calc;

import java.math.BigDecimal;

import javax.inject.Inject;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.fixturescripts.FixtureScript;

import org.estatio.module.budget.dom.budget.Budget;
import org.estatio.module.budget.dom.budgetcalculation.BudgetCalculationType;
import org.estatio.module.budget.dom.budgetcalculation.Status;
import org.estatio.module.budget.dom.partioning.Partitioning;
import org.estatio.module.budget.fixtures.budgets.enums.Budget_enum;
import org.estatio.module.budget.fixtures.partitioning.enums.Partitioning_enum;
import org.estatio.module.budgetassignment.contributions.Budget_Calculate;
import org.estatio.module.budgetassignment.contributions.Lease_BudgetCalculationRuns;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationResult;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationResultRepository;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationRun;
import org.estatio.module.budgetassignment.dom.override.BudgetOverride;
import org.estatio.module.budgetassignment.dom.override.BudgetOverrideRepository;
import org.estatio.module.budgetassignment.dom.override.BudgetOverrideValue;
import org.estatio.module.budgetassignment.integtests.BudgetAssignmentModuleIntegTestAbstract;
import org.estatio.module.charge.dom.Charge;
import org.estatio.module.charge.fixtures.charges.enums.Charge_enum;
import org.estatio.module.lease.dom.Lease;
import org.estatio.module.lease.fixtures.lease.enums.Lease_enum;

import static org.assertj.core.api.Assertions.assertThat;

public class BudgetCalculationResult_IntegTest extends BudgetAssignmentModuleIntegTestAbstract {



    @Before
    public void setupData() {
        runFixtureScript(new FixtureScript() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                executionContext.executeChild(this, Budget_enum.OxfBudget2015Simple.builder());
                executionContext.executeChild(this, Partitioning_enum.OxfPartitioning2015Simple);
                executionContext.executeChild(this, Lease_enum.OxfTopModel001Gb.builder());
            }
        });
    }

    @Inject
    BudgetOverrideRepository budgetOverrideRepository;

    @Inject
    BudgetCalculationResultRepository budgetCalculationResultRepository;

    Lease leaseTopModel;
    LocalDate overrideStartDate;
    LocalDate overrideEndDate;
    BudgetCalculationResult calculationResultForLeaseTopmodel;
    BigDecimal overrideValue;

    @Test
    public void override_applied_when_having_interval_containing_the_partitioning_interval() throws Exception {

        // given
        Budget budget = Budget_enum.OxfBudget2015.findUsing(serviceRegistry);
        Partitioning partitioningForBudgeted = budget.getPartitioningForBudgeting();
        LocalDate partitioningStartDate = partitioningForBudgeted.getStartDate();
        LocalDate partitioningEndDate = partitioningForBudgeted.getEndDate();

        // when
        overrideStartDate = partitioningStartDate;
        overrideEndDate = partitioningEndDate;
        createOverrideForLeaseTopmModel(overrideStartDate, overrideEndDate);

        wrap(mixin(Budget_Calculate.class, budget)).calculate(false);
        BudgetCalculationRun run = wrap(mixin(Lease_BudgetCalculationRuns.class, leaseTopModel)).budgetCalculationRuns().get(0);
        calculationResultForLeaseTopmodel = run.getBudgetCalculationResults().first();

        // then
        assertThat(calculationResultForLeaseTopmodel.getOverrideValues().size()).isEqualTo(1);
        final BudgetOverrideValue overrideValueForTopModel = calculationResultForLeaseTopmodel.getOverrideValues().get(0);
        assertThat(overrideValueForTopModel.getValue()).isEqualTo(overrideValue);
        assertThat(overrideValueForTopModel.getStatus()).isEqualTo(Status.NEW);
        assertThat(overrideValueForTopModel.getType()).isEqualTo(BudgetCalculationType.BUDGETED);
        assertThat(calculationResultForLeaseTopmodel.getValue()).isEqualTo(overrideValue);
        assertThat(calculationResultForLeaseTopmodel.valueAsCalculatedByBudget()).isEqualTo(new BigDecimal("30.770"));
        assertThat(calculationResultForLeaseTopmodel.getShortfall()).isEqualTo(new BigDecimal("10.00"));
    }

    @Test
    public void override_NOT_applied_when_having_interval_not_containing_the_partitioning_interval() throws Exception {

        // given
        Budget budget = Budget_enum.OxfBudget2015.findUsing(serviceRegistry);
        Partitioning partitioningForBudgeted = budget.getPartitioningForBudgeting();
        LocalDate partitioningStartDate = partitioningForBudgeted.getStartDate();
        LocalDate partitioningEndDate = partitioningForBudgeted.getEndDate();

        // when
        overrideStartDate = partitioningStartDate;
        overrideEndDate = partitioningEndDate.minusDays(1);
        createOverrideForLeaseTopmModel(overrideStartDate, overrideEndDate);

        wrap(mixin(Budget_Calculate.class, budget)).calculate(false);
        BudgetCalculationRun run = wrap(mixin(Lease_BudgetCalculationRuns.class, leaseTopModel)).budgetCalculationRuns().get(0);
        calculationResultForLeaseTopmodel = run.getBudgetCalculationResults().first();

        // then
        assertThat(calculationResultForLeaseTopmodel.getOverrideValues().size()).isEqualTo(0);
        assertThat(calculationResultForLeaseTopmodel.getValue()).isEqualTo(new BigDecimal("30.77"));
        assertThat(calculationResultForLeaseTopmodel.valueAsCalculatedByBudget()).isEqualTo(new BigDecimal("30.770"));
        assertThat(calculationResultForLeaseTopmodel.getShortfall()).isEqualTo(new BigDecimal("0.00"));

    }

    private BudgetOverride createOverrideForLeaseTopmModel(final LocalDate start, final LocalDate end) {
        leaseTopModel = Lease_enum.OxfTopModel001Gb.findUsing(serviceRegistry);
        Charge invoiceCharge = Charge_enum.GbServiceCharge.findUsing(serviceRegistry);
        overrideValue = new BigDecimal("20.77");
        String reason = "Some reason";
        return wrap(budgetOverrideRepository).newBudgetOverrideForFixed(overrideValue, leaseTopModel, start, end, invoiceCharge, null, null, reason);
    }

}
