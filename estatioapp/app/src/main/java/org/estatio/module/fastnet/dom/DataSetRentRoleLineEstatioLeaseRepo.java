/*
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
package org.estatio.module.fastnet.dom;

import java.util.List;

import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;

import org.estatio.module.base.dom.UdoDomainRepositoryAndFactory;

@DomainService(nature = NatureOfService.DOMAIN)
public class DataSetRentRoleLineEstatioLeaseRepo extends UdoDomainRepositoryAndFactory<DataSetRentRoleLineEstatioLease> {

    public DataSetRentRoleLineEstatioLeaseRepo() {
        super(DataSetRentRoleLineEstatioLeaseRepo.class, DataSetRentRoleLineEstatioLease.class);
    }

    @Programmatic
    public List<DataSetRentRoleLineEstatioLease> findByExportDate(
            final LocalDate exportDate) {
        return allMatches("findByExportDate",
                "exportDate", exportDate);
    }

    @Programmatic
    public List<DataSetRentRoleLineEstatioLease> estatioLeasesNotFoundByExportDate(
            final LocalDate exportDate) {
        return allMatches("estatioLeasesNotFoundByExportDate",
                "exportDate", exportDate);
    }

    @Programmatic
    public List<DataSetRentRoleLineEstatioLease> futureLeasesNotFoundByExportDate(
            final LocalDate exportDate) {
        return allMatches("futureLeasesNotFoundByExportDate",
                "exportDate", exportDate);
    }

}
