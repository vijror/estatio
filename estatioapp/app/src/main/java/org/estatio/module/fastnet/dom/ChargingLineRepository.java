package org.estatio.module.fastnet.dom;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.services.registry.ServiceRegistry2;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.value.Blob;

import org.isisaddons.module.excel.dom.ExcelService;
import org.isisaddons.module.excel.dom.util.Mode;

@DomainService(
        nature = NatureOfService.DOMAIN,
        repositoryFor = ChargingLine.class
)
public class ChargingLineRepository {

    // NOTE : no creator method in this repo because the only way objects get instantiated is by excelservice

    @Programmatic
    public List<ChargingLine> listAll() {
        return repositoryService.allInstances(ChargingLine.class);
    }

    @Programmatic
    public List<ChargingLine> findByKontraktNr(final String kontraktNr) {
        return repositoryService.allMatches(
                new QueryDefault<>(
                        ChargingLine.class,
                        "findByKontraktNr",
                        "kontraktNr", kontraktNr));
    }

    @Programmatic
    public List<ChargingLine> findByKontraktNrAndExportDate(final String kontraktNr, final LocalDate exportDate) {
        return repositoryService.allMatches(
                new QueryDefault<>(
                        ChargingLine.class,
                        "findByKontraktNrAndExportDate",
                        "kontraktNr", kontraktNr,
                        "exportDate", exportDate));
    }

    @Programmatic
    public List<ChargingLine> findByExportDate(final LocalDate exportDate) {
        return repositoryService.allMatches(
                new QueryDefault<>(
                        ChargingLine.class,
                        "findByExportDate",
                        "exportDate", exportDate));
    }

    @Programmatic
    public ChargingLine findUnique(
            final String kontraktNr,
            final String kod,
            final String kod2,
            final String fromDat,
            final String tomDat,
            final BigDecimal arsBel,
            final LocalDateTime evdInSd) {
        return repositoryService.uniqueMatch(
                new QueryDefault<>(
                        ChargingLine.class,
                        "findUnique",
                        "kontraktNr", kontraktNr,
                        "kod", kod,
                        "kod2", kod2,
                        "fromDat", fromDat,
                        "tomDat", tomDat,
                        "arsBel", arsBel,
                        "evdInSd", evdInSd));
    }

    @Programmatic
    public List<ChargingLine> importChargingItems(final Blob spreadsheet) {
        List<ChargingLine> chargingLines =
                excelService.fromExcel(spreadsheet, ChargingLine.class, "Charging", Mode.RELAXED);
        chargingLines.forEach(x -> {
            serviceRegistry2.injectServicesInto(x);
            x.importData(null);
        });
        return chargingLines;
    }


    @Inject
    RepositoryService repositoryService;

    @Inject
    ServiceRegistry2 serviceRegistry2;

    @Inject
    ExcelService excelService;

}
