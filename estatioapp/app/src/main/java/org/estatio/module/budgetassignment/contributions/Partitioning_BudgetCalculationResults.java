package org.estatio.module.budgetassignment.contributions;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.SemanticsOf;

import org.estatio.module.budget.dom.partioning.Partitioning;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationResult;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationResultRepository;

@Mixin
public class Partitioning_BudgetCalculationResults {

    private final Partitioning partitioning;
    public Partitioning_BudgetCalculationResults(Partitioning partitioning){
        this.partitioning = partitioning;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(contributed = Contributed.AS_ASSOCIATION)
    public List<BudgetCalculationResult> budgetCalculationResults() {
        return budgetCalculationResultRepository.findByPartitioning(partitioning);
    }

    @Inject
    private BudgetCalculationResultRepository budgetCalculationResultRepository;

}
