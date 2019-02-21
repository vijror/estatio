/*
 *
 *  Copyright 2012-2015 Eurocommercial Properties NV
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.estatio.module.capex.integtests.project;

import java.math.BigDecimal;

import javax.inject.Inject;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.applib.services.wrapper.InvalidException;

import org.estatio.module.capex.dom.project.Project;
import org.estatio.module.capex.dom.project.ProjectItem;
import org.estatio.module.capex.dom.project.ProjectItemAmendment;
import org.estatio.module.capex.dom.project.ProjectItemAmendmentRepository;
import org.estatio.module.capex.fixtures.project.enums.Project_enum;
import org.estatio.module.capex.integtests.CapexModuleIntegTestAbstract;
import org.estatio.module.charge.fixtures.charges.enums.Charge_enum;

import static org.assertj.core.api.Assertions.assertThat;

public class ProjectAmendment_IntegTest extends CapexModuleIntegTestAbstract {

    @Before
    public void setupData() {
        runFixtureScript(new FixtureScript() {
            @Override
            protected void execute(ExecutionContext executionContext) {
                executionContext.executeChild(this, Project_enum.KalProject1.builder());
            }
        });
    }

    @Inject ProjectItemAmendmentRepository projectItemAmendmentRepository;

    @Test
    public void amending_works() throws Exception {

        // given
        final LocalDate amendmentDate = new LocalDate(2018,1,1);
        final BigDecimal budgetedAmount = new BigDecimal("123.45");

        Project project = Project_enum.KalProject1.findUsing(serviceRegistry);

        project.addItem(Charge_enum.NlIncomingCharge1.findUsing(serviceRegistry), "First item", budgetedAmount, null, null, null, null);
        assertThat(project.getItems()).hasSize(1);

        ProjectItem firstItem = project.getItems().first();
        assertThat(firstItem.getBudgetedAmount()).isEqualTo(budgetedAmount);
        assertThat(projectItemAmendmentRepository.listAll()).isEmpty();
        assertThat(projectItemAmendmentRepository.findByProjectItemSorted(firstItem)).isEmpty();
        assertThat(projectItemAmendmentRepository.findUnique(firstItem, amendmentDate)).isNull();

        // when
        wrap(firstItem).amendAmount(new BigDecimal("100.05"), null, amendmentDate);

        // then
        assertThat(projectItemAmendmentRepository.listAll()).hasSize(1);
        ProjectItemAmendment amendment = projectItemAmendmentRepository.listAll().get(0);
        assertThat(amendment.getProjectItem()).isEqualTo(firstItem);
        assertThat(amendment.getDate()).isEqualTo(amendmentDate);
        assertThat(amendment.getPreviousBudgetedAmount()).isEqualTo(budgetedAmount);
        final BigDecimal newAmount = new BigDecimal("223.50");
        assertThat(firstItem.getBudgetedAmount()).isEqualTo(newAmount);
        assertThat(amendment.getNewBudgetedAmount()).isEqualTo(newAmount);

        assertThat(projectItemAmendmentRepository.findByProjectItemSorted(firstItem)).hasSize(1);
        assertThat(projectItemAmendmentRepository.findUnique(firstItem, amendmentDate)).isEqualTo(amendment);

        // and when amending on same date
        wrap(firstItem).amendAmount(null, new BigDecimal("100.00"), amendmentDate);
        // then still
        assertThat(projectItemAmendmentRepository.findByProjectItemSorted(firstItem)).hasSize(1);
        // and the amounts are adapted
        amendment = projectItemAmendmentRepository.findByProjectItemSorted(firstItem).get(0);
        assertThat(amendment.getDate()).isEqualTo(amendmentDate);
        assertThat(amendment.getPreviousBudgetedAmount()).isEqualTo(budgetedAmount); // this is the original budgeted amount taken from the amendment that is being upserted here and NOT the budgeted amount taken from the budget item
        final BigDecimal newAmount2 = new BigDecimal("123.50");
        assertThat(amendment.getNewBudgetedAmount()).isEqualTo(newAmount2);
        // and of course
        assertThat(firstItem.getBudgetedAmount()).isEqualTo(newAmount2);

        // and when amending on other (later) date
        wrap(firstItem.amendAmount(new BigDecimal("123.50"), null, amendmentDate.plusDays(1)));
        // then
        assertThat(projectItemAmendmentRepository.findByProjectItemSorted(firstItem)).hasSize(2);
        final BigDecimal newAmount3 = new BigDecimal("247.00");
        assertThat(firstItem.getBudgetedAmount()).isEqualTo(newAmount3);
        amendment = projectItemAmendmentRepository.findUnique(firstItem, amendmentDate.plusDays(1));
        assertThat(amendment.getPreviousBudgetedAmount()).isEqualTo(newAmount2);
        assertThat(amendment.getNewBudgetedAmount()).isEqualTo(newAmount3);

        // and expect
        // expect
        expectedExceptions.expect(InvalidException.class);
        expectedExceptions.expectMessage("There is an amendment after the chosen date already");

        // and when trying to update on the original date again
        wrap(firstItem).amendAmount(new BigDecimal("100.00"), null, amendmentDate);

    }

}