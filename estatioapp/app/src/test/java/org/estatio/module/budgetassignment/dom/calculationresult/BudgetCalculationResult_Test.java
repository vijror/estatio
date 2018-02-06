/*
 * Copyright 2015 Yodo Int. Projects and Consultancy
 *
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.estatio.module.budgetassignment.dom.calculationresult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

import org.incode.module.unittestsupport.dom.bean.AbstractBeanPropertiesTest;

import org.estatio.module.budget.dom.budgetcalculation.BudgetCalculation;
import org.estatio.module.budget.dom.budgetcalculation.BudgetCalculationType;
import org.estatio.module.budget.dom.partioning.Partitioning;
import org.estatio.module.budgetassignment.dom.override.BudgetOverride;
import org.estatio.module.budgetassignment.dom.override.BudgetOverrideDummy;
import org.estatio.module.budgetassignment.dom.override.BudgetOverrideForFixed;
import org.estatio.module.budgetassignment.dom.override.BudgetOverrideRepository;
import org.estatio.module.budgetassignment.dom.override.BudgetOverrideValue;
import org.estatio.module.charge.dom.Charge;
import org.estatio.module.lease.dom.Lease;

import static org.assertj.core.api.Assertions.assertThat;

public class BudgetCalculationResult_Test {

    public static class BeanProperties extends AbstractBeanPropertiesTest {

        @Test
        public void test() {
            final BudgetCalculationResult pojo = new BudgetCalculationResult();
            newPojoTester()
                    .withFixture(pojos(BudgetCalculationRun.class, BudgetCalculationRun.class))
                    .withFixture(pojos(Charge.class, Charge.class))
                    .exercise(pojo);
        }

    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    public static class CalculateTest extends BudgetCalculationResult_Test {

        BudgetCalculationResult budgetCalculationResult;
        BudgetCalculation bc1 = new BudgetCalculation();
        BudgetCalculation bc2 = new BudgetCalculation();
        Charge incomingCharge1 = new Charge();
        Charge incomingCharge2 = new Charge();
        BudgetOverrideValue bOvVal1 = new BudgetOverrideValue();
        BudgetOverrideValue bOvVal2 = new BudgetOverrideValue();
        List<BudgetCalculation> budgetCalculations = new ArrayList<>();
        List<BudgetOverrideValue> budgetOverrideValues = new ArrayList<>();
        BudgetOverride budgetOverride = new BudgetOverrideDummy();
        BudgetOverride otherBudgetOverride = new BudgetOverrideDummy();
        BigDecimal valueCalculatedByBudget = new BigDecimal("100.00");
        BigDecimal valueUsingOverrides = new BigDecimal("99.99");

        @Before
        public void setUp(){
            bc1.setValue(new BigDecimal("75.00"));
            bc1.setIncomingCharge(incomingCharge1);
            budgetCalculations.add(bc1);

            bc2.setValue(new BigDecimal("25.00"));
            bc2.setIncomingCharge(incomingCharge2);
            budgetCalculations.add(bc2);
        }

        @Test
        public void calculate_Overriding_All_BudgetCalculations() {

            // given
            bOvVal1.setBudgetOverride(budgetOverride);
            bOvVal1.setValue(valueUsingOverrides);
            budgetOverrideValues.add(bOvVal1);
            budgetCalculationResult = new BudgetCalculationResult(){
                @Override
                public List<BudgetCalculation> getBudgetCalculations(){
                    return budgetCalculations;
                }
                @Override
                public List<BudgetOverrideValue> getOverrideValues(){
                    return budgetOverrideValues;
                }
                @Override
                void validateOverrides(){}
            };

            // when
            budgetCalculationResult.calculate();

            // then
            assertThat(budgetCalculationResult.getValue()).isEqualTo(valueUsingOverrides);
            assertThat(budgetCalculationResult.getShortfall()).isEqualTo(new BigDecimal("0.01"));
            assertThat(budgetCalculationResult.getShortfall()).isEqualTo(valueCalculatedByBudget.subtract(valueUsingOverrides));

        }

        @Test
        public void calculate_Overriding_One_BudgetCalculation() {

            // given
            budgetOverride.setIncomingCharge(incomingCharge1);
            bOvVal1.setBudgetOverride(budgetOverride);
            bOvVal1.setValue(new BigDecimal("74.99"));
            budgetOverrideValues.add(bOvVal1);
            otherBudgetOverride.setIncomingCharge(incomingCharge2);
            bOvVal2.setBudgetOverride(otherBudgetOverride);
            bOvVal2.setValue(new BigDecimal("1234.56"));
            budgetCalculationResult = new BudgetCalculationResult(){
                @Override
                public List<BudgetCalculation> getBudgetCalculations(){
                    return budgetCalculations;
                }
                @Override
                public List<BudgetOverrideValue> getOverrideValues(){
                    return budgetOverrideValues;
                }
                @Override
                void validateOverrides(){}
            };

            // when
            budgetCalculationResult.calculate();

            // then
            assertThat(budgetCalculationResult.getValue()).isEqualTo(valueUsingOverrides);
            assertThat(budgetCalculationResult.getShortfall()).isEqualTo(new BigDecimal("0.01"));
            assertThat(budgetCalculationResult.getShortfall()).isEqualTo(valueCalculatedByBudget.subtract(valueUsingOverrides));

        }

    }

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    public static class GetOverrideValuesForResult extends BudgetCalculationResult_Test {

        @Mock BudgetOverrideRepository mockBudgetOverrideRepository;

        @Test
        public void get_override_values_takes_override_interval_into_account() throws Exception {

            // given
            final LocalDate partitioningStartDate = new LocalDate(2017, 1, 1);
            final LocalDate partitioningEndDate = new LocalDate(2017, 12, 31);
            Partitioning partitioning = new Partitioning();
            partitioning.setStartDate(partitioningStartDate);
            partitioning.setEndDate(partitioningEndDate);
            partitioning.setType(BudgetCalculationType.BUDGETED);

            BudgetCalculationResult budgetCalculationResult = new BudgetCalculationResult(){
                @Override
                Partitioning getPartitioning(){
                    return partitioning;
                }
            };
            budgetCalculationResult.budgetOverrideRepository = mockBudgetOverrideRepository;

            BudgetCalculationRun run = new BudgetCalculationRun();
            Lease lease = new Lease();
            run.setLease(lease);
            run.setPartitioning(partitioning);

            Charge invoiceCharge = new Charge();
            budgetCalculationResult.setInvoiceCharge(invoiceCharge);

            budgetCalculationResult.setBudgetCalculationRun(run);

            BudgetOverride override1 = new BudgetOverrideForFixed();
            override1.setStartDate(partitioningStartDate);
            override1.setEndDate(partitioningEndDate);
            BudgetOverrideValue value1 = new BudgetOverrideValue();
            value1.setType(BudgetCalculationType.BUDGETED);
            override1.getValues().add(value1);

            BudgetOverrideForFixed override2 = new BudgetOverrideForFixed();
            override2.setStartDate(partitioningStartDate.plusDays(1));
            override2.setEndDate(partitioningEndDate);
            BudgetOverrideValue value2 = new BudgetOverrideValue();
            value2.setType(BudgetCalculationType.BUDGETED);
            override2.getValues().add(value2);

            BudgetOverrideForFixed override3 = new BudgetOverrideForFixed();
            override3.setStartDate(partitioningStartDate);
            override3.setEndDate(partitioningEndDate.minusDays(1));
            BudgetOverrideValue value3 = new BudgetOverrideValue();
            value3.setType(BudgetCalculationType.BUDGETED);
            override3.getValues().add(value3);

            // expect
            context.checking(new Expectations(){{
                oneOf(mockBudgetOverrideRepository).findByLeaseAndInvoiceCharge(lease, invoiceCharge);
                will(returnValue(Arrays.asList(override1, override2, override3)));
            }});

            // when
            List<BudgetOverrideValue> result = budgetCalculationResult.getOverrideValues();

            // then
            assertThat(result.size()).isEqualTo(1);
            assertThat(result).contains(value1);

        }

    }



}
