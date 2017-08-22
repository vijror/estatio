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

import org.isisaddons.module.security.dom.tenancy.ApplicationTenancy;

public interface TaxRepository {

    Tax findOrCreate(String reference, String name, ApplicationTenancy applicationTenancy);

    Tax findByReference(String reference);

    // TODO: this is only here because TaxMenu uses it (when we move TaxMenu into this module, then can remove)
    Tax newTax(String reference, String name, ApplicationTenancy applicationTenancy);

}
