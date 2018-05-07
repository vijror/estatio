package org.estatio.module.lease.imports;

import java.math.BigDecimal;
import java.util.List;

import com.google.common.collect.Lists;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.fixturescripts.FixtureScript;

import org.isisaddons.module.excel.dom.ExcelFixture;
import org.isisaddons.module.excel.dom.ExcelFixtureRowHandler;

import org.estatio.module.base.dom.Importable;

import lombok.Getter;
import lombok.Setter;

@DomainObject(
        nature = Nature.VIEW_MODEL,
        objectType = "org.estatio.dom.viewmodels.LeaseTermForTurnOverRentFixedImport"
)
public class LeaseTermForTurnOverRentFixedImport extends LeaseTermImportAbstract implements ExcelFixtureRowHandler, Importable {

    private static final Logger LOG = LoggerFactory.getLogger(LeaseTermForTurnOverRentFixedImport.class);

    @Getter @Setter
    private String leaseReference;

    @Getter @Setter
    private String leaseExternalReference;

    @Getter @Setter
    private LocalDate startDatePrevious;

    @Getter @Setter
    private BigDecimal valuePrevious;

    @Getter @Setter
    private LocalDate startDateCurrent;

    @Getter @Setter
    private BigDecimal valueCurrent;

    @Getter @Setter
    private LocalDate startDate;

    @Getter @Setter
    private BigDecimal value;

    @Programmatic
    @Override
    public List<Object> handleRow(FixtureScript.ExecutionContext executionContext, ExcelFixture excelFixture, Object o) {
        return importData(null);
    }

    public List<Object> importData() {
        return importData(null);
    }

    @Programmatic
    @Override
    public List<Object> importData(Object previousRow) {

        return Lists.newArrayList();

    }
}
