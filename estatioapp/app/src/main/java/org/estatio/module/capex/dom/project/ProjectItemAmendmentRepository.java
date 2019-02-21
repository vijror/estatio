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
package org.estatio.module.capex.dom.project;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.repository.RepositoryService;

import org.estatio.module.base.dom.UdoDomainRepositoryAndFactory;

@DomainService(repositoryFor = ProjectItemAmendment.class, nature = NatureOfService.DOMAIN)
public class ProjectItemAmendmentRepository extends UdoDomainRepositoryAndFactory<ProjectItemAmendment> {

    public ProjectItemAmendmentRepository() {
        super(ProjectItemAmendmentRepository.class, ProjectItemAmendment.class);
    }

    @Programmatic
    public List<ProjectItemAmendment> listAll() {
        return allInstances();
    }

    @Programmatic
    public List<ProjectItemAmendment> findByProjectItemSorted(final ProjectItem projectItem) {
        return allMatches("findByProjectItem", "projectItem", projectItem).stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
    }

    @Programmatic
    public ProjectItemAmendment findUnique(final ProjectItem projectItem, final LocalDate date) {
        return uniqueMatch("findUnique", "projectItem", projectItem, "date", date);
    }

    @Programmatic
    public ProjectItemAmendment upsert(
            final ProjectItem projectItem,
            final LocalDate date,
            final BigDecimal newBudgetedAmount,
            final BigDecimal previousBudgetedAmount) {
        ProjectItemAmendment amendment = findUnique(projectItem, date);
        if(amendment == null) {
            amendment = create(projectItem, date, newBudgetedAmount, previousBudgetedAmount);
        } else {
            BigDecimal previousBudgetedAmountToBeUsedWhenUpserting = amendment.getPreviousBudgetedAmount();
            amendment.setNewBudgetedAmount(newBudgetedAmount);
            amendment.setPreviousBudgetedAmount(previousBudgetedAmountToBeUsedWhenUpserting);
        }
        return amendment;
    }

    private ProjectItemAmendment create(
            final ProjectItem projectItem,
            final LocalDate date,
            final BigDecimal newBudgetedAmount,
            final BigDecimal previousBudgetedAmount) {

        ProjectItemAmendment amendment = repositoryService.instantiate(ProjectItemAmendment.class);
        amendment.setProjectItem(projectItem);
        amendment.setDate(date);
        amendment.setNewBudgetedAmount(newBudgetedAmount);
        amendment.setPreviousBudgetedAmount(previousBudgetedAmount);

        repositoryService.persist(amendment);

        return amendment;
    }

    @Inject
    RepositoryService repositoryService;
}
