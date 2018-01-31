package org.estatio.module.budgetassignment.contributions;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;

import org.estatio.module.budget.dom.budget.Budget;
import org.estatio.module.budget.dom.budgetcalculation.BudgetCalculationService;
import org.estatio.module.budget.dom.budgetcalculation.BudgetCalculationType;
import org.estatio.module.budgetassignment.dom.service.BudgetAssignmentService;

/**
 * This currently could be inlined into Budget, however it is incomplete and my suspicion is that eventually it
 * may (like the other mixins that do calculations) will depend upon services that are not within Budget.
 */
@Mixin
public class Budget_Reconcile {

    private final Budget budget;
    public Budget_Reconcile(Budget budget){
        this.budget = budget;
    }

    @Action(semantics = SemanticsOf.NON_IDEMPOTENT_ARE_YOU_SURE)
    @ActionLayout(contributed = Contributed.AS_ACTION)
    public Budget reconcile(
            @ParameterLayout(describedAs = "Final calculation will make the calculations permanent and impact the leases")
            final boolean finalCalculation) {
            budgetCalculationService.calculate(budget, BudgetCalculationType.ACTUAL);
            budgetAssignmentService.calculateResultsForLeases(budget, BudgetCalculationType.ACTUAL);
        if (finalCalculation){
            budgetAssignmentService.assignForType(budget, BudgetCalculationType.ACTUAL);
        }
        return budget;
    }

    // TODO: also check if there is a partitioning with partition items for type Actual
    public String disableReconcile(){
        return budget.noUnassignedItemsForTypeReason(BudgetCalculationType.ACTUAL);
    }

    @Inject
    private BudgetCalculationService budgetCalculationService;

    @Inject
    private BudgetAssignmentService budgetAssignmentService;

}
