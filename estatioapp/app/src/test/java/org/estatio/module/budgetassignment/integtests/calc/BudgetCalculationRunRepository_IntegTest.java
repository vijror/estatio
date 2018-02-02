package org.estatio.module.budgetassignment.integtests.calc;

import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.fixturescripts.FixtureScript;

import org.estatio.module.asset.dom.Property;
import org.estatio.module.asset.dom.PropertyRepository;
import org.estatio.module.asset.fixtures.property.enums.Property_enum;
import org.estatio.module.budget.dom.budget.Budget;
import org.estatio.module.budget.dom.budget.BudgetRepository;
import org.estatio.module.budget.dom.budgetcalculation.BudgetCalculationType;
import org.estatio.module.budget.dom.budgetcalculation.Status;
import org.estatio.module.budget.dom.partioning.Partitioning;
import org.estatio.module.budget.fixtures.budgets.enums.Budget_enum;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationRun;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationRunRepository;
import org.estatio.module.budgetassignment.integtests.BudgetAssignmentModuleIntegTestAbstract;
import org.estatio.module.lease.dom.Lease;
import org.estatio.module.lease.dom.LeaseRepository;
import org.estatio.module.lease.fixtures.lease.enums.Lease_enum;

import static org.assertj.core.api.Assertions.assertThat;

public class BudgetCalculationRunRepository_IntegTest extends BudgetAssignmentModuleIntegTestAbstract {

    @Inject
    BudgetRepository budgetRepository;

    @Inject
    BudgetCalculationRunRepository budgetCalculationRunRepository;

    @Inject
    PropertyRepository propertyRepository;

    @Inject
    LeaseRepository leaseRepository;

    Property propertyOxf;
    List<Budget> budgetsForOxf;
    Budget budget2015;

    @Before
    public void setupData() {
        runFixtureScript(new FixtureScript() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                executionContext.executeChild(this, Budget_enum.OxfBudget2015.builder());
                executionContext.executeChild(this, Budget_enum.OxfBudget2016.builder());

                executionContext.executeChild(this, Lease_enum.OxfTopModel001Gb.builder());
            }
        });
        propertyOxf = Property_enum.OxfGb.findUsing(serviceRegistry);
        budgetsForOxf = budgetRepository.findByProperty(propertyOxf);
        budget2015 = budgetRepository.findByPropertyAndStartDate(propertyOxf, Budget_enum.OxfBudget2015.getStartDate());
        budget2015.findOrCreatePartitioningForBudgeting();
    }

    public static class FindOrCreate extends BudgetCalculationRunRepository_IntegTest {

        @Test
        public void test() {

            Lease leaseTopModel;

            // given
            leaseTopModel = Lease_enum.OxfTopModel001Gb.findUsing(serviceRegistry);
            assertThat(budgetCalculationRunRepository.allBudgetCalculationRuns().size()).isEqualTo(0);

            // when
            BudgetCalculationRun run = wrap(budgetCalculationRunRepository).findOrCreateBudgetCalculationRun(leaseTopModel, budget2015.getPartitioningForBudgeting());

            // then
            assertThat(budgetCalculationRunRepository.allBudgetCalculationRuns().size()).isEqualTo(1);
            assertThat(budgetCalculationRunRepository.allBudgetCalculationRuns().get(0)).isEqualTo(run);
            assertThat(run.getBudget()).isEqualTo(budget2015);
            assertThat(run.getLease()).isEqualTo(leaseTopModel);
            assertThat(run.getType()).isEqualTo(BudgetCalculationType.BUDGETED);
            assertThat(run.getStatus()).isEqualTo(Status.NEW);

            // and when again
            wrap(budgetCalculationRunRepository).findOrCreateBudgetCalculationRun(leaseTopModel, budget2015.getPartitioningForBudgeting());

            // then is idemPotent
            assertThat(budgetCalculationRunRepository.allBudgetCalculationRuns().size()).isEqualTo(1);

            // and when
            budget2015.newPartitioning();
            Partitioning partitioningForActual = budget2015.getPartitioningsOfType(BudgetCalculationType.ACTUAL).get(0);
            run = wrap(budgetCalculationRunRepository).findOrCreateBudgetCalculationRun(leaseTopModel, partitioningForActual);

            // then
            assertThat(budgetCalculationRunRepository.allBudgetCalculationRuns().size()).isEqualTo(2);
            assertThat(run.getType()).isEqualTo(BudgetCalculationType.ACTUAL);

        }
    }

    public static class FindByLease extends BudgetCalculationRunRepository_IntegTest {

        @Test
        public void findByLease() {

            Lease leaseTopModel;

            // given
            leaseTopModel = Lease_enum.OxfTopModel001Gb.findUsing(serviceRegistry);
            assertThat(budgetCalculationRunRepository.findByLease(leaseTopModel).size()).isEqualTo(0);

            // when
            wrap(budgetCalculationRunRepository).findOrCreateBudgetCalculationRun(leaseTopModel, budget2015.getPartitioningForBudgeting());

            // then
            assertThat(budgetCalculationRunRepository.findByLease(leaseTopModel).size()).isEqualTo(1);

        }

    }

    public static class FindByBudget extends BudgetCalculationRunRepository_IntegTest {

        @Test
        public void findByBudget() {

            // given
            Lease leaseTopModel = Lease_enum.OxfTopModel001Gb.findUsing(serviceRegistry);

            // when
            wrap(budgetCalculationRunRepository).findOrCreateBudgetCalculationRun(leaseTopModel, budget2015.getPartitioningForBudgeting());

            // then
            assertThat(budgetCalculationRunRepository.findByBudget(budget2015).size()).isEqualTo(1);

        }

    }

    public static class FindByPartitioning extends BudgetCalculationRunRepository_IntegTest {

        @Test
        public void findByPartitioning() {

            // given
            Lease leaseTopModel = Lease_enum.OxfTopModel001Gb.findUsing(serviceRegistry);

            // when
            wrap(budgetCalculationRunRepository).findOrCreateBudgetCalculationRun(leaseTopModel, budget2015.getPartitioningForBudgeting());

            // then
            assertThat(budgetCalculationRunRepository.findByPartitioning(budget2015.getPartitioningForBudgeting()).size()).isEqualTo(1);

        }

    }

    public static class FindByPartitioningAndStatus extends BudgetCalculationRunRepository_IntegTest {

        @Test
        public void findByPartitioningAndStatus() {

            // given
            Lease leaseTopModel = Lease_enum.OxfTopModel001Gb.findUsing(serviceRegistry);

            // when
            wrap(budgetCalculationRunRepository).findOrCreateBudgetCalculationRun(leaseTopModel, budget2015.getPartitioningForBudgeting());

            // then
            assertThat(budgetCalculationRunRepository.findByPartitioningAndStatus(budget2015.getPartitioningForBudgeting(), Status.NEW).size()).isEqualTo(1);
            assertThat(budgetCalculationRunRepository.findByPartitioningAndStatus(budget2015.getPartitioningForBudgeting(), Status.ASSIGNED).size()).isEqualTo(0);

        }

    }

}
