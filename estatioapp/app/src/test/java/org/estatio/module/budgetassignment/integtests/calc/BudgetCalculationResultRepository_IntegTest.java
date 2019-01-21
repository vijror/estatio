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
import org.estatio.module.budget.fixtures.budgets.enums.Budget_enum;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationResultRepository;
import org.estatio.module.budgetassignment.integtests.BudgetAssignmentModuleIntegTestAbstract;
import org.estatio.module.charge.dom.ChargeRepository;
import org.estatio.module.lease.dom.Lease;
import org.estatio.module.lease.dom.LeaseRepository;
import org.estatio.module.lease.fixtures.lease.enums.Lease_enum;

public class BudgetCalculationResultRepository_IntegTest extends BudgetAssignmentModuleIntegTestAbstract {

    @Inject
    BudgetRepository budgetRepository;

    @Inject
    BudgetCalculationResultRepository budgetCalculationResultRepository;

    @Inject
    PropertyRepository propertyRepository;

    @Inject
    LeaseRepository leaseRepository;

    @Inject
    ChargeRepository chargeRepository;

    Property propertyOxf;
    List<Budget> budgetsForOxf;
    Budget budget2015;
    Lease leaseTopModel;


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
        leaseTopModel = Lease_enum.OxfTopModel001Gb.findUsing(serviceRegistry);
    }

    public static class FindOrCreate extends BudgetCalculationResultRepository_IntegTest {

        @Test
        public void test() {

            // TODO: finish after refactoring if still needed

        }
    }

}
