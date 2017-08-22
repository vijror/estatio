/*
 *
 *  Copyright 2012-2014 Eurocommercial Properties NV
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
package org.estatio.tax.dom;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

import org.incode.module.base.dom.with.WithNameGetter;
import org.incode.module.base.dom.with.WithReferenceUnique;

import org.estatio.dom.apptenancy.WithApplicationTenancy;
import org.estatio.dom.apptenancy.WithApplicationTenancyCountry;

public interface Tax
        extends WithNameGetter, WithReferenceUnique, WithApplicationTenancy, WithApplicationTenancyCountry {

    String getDescription();
    String getExternalReference();

    TaxRate taxRateFor(LocalDate localDate);

    BigDecimal grossFromNet(BigDecimal netValue, LocalDate date);

    BigDecimal percentageFor(LocalDate localDate);

}
