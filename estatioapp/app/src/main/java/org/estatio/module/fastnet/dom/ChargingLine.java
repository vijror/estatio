package org.estatio.module.fastnet.dom;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.Indices;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.Unique;
import javax.jdo.annotations.Uniques;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.services.repository.RepositoryService;

import org.estatio.module.base.dom.Importable;

import lombok.Getter;
import lombok.Setter;

@PersistenceCapable(
        identityType = IdentityType.DATASTORE,
        schema = "fastnet",
        table = "ChargingLine"
)
@DatastoreIdentity(
        strategy = IdGeneratorStrategy.IDENTITY,
        column = "id")
@Version(
        strategy = VersionStrategy.VERSION_NUMBER,
        column = "version")
@Queries({
        @Query(
                name = "findByKontraktNr", language = "JDOQL",
                value = "SELECT "
                        + "FROM org.estatio.module.fastnet.dom.ChargingLine "
                        + "WHERE kontraktNr == :kontraktNr "),
        @Query(
                name = "findByKontraktNrAndExportDate", language = "JDOQL",
                value = "SELECT "
                        + "FROM org.estatio.module.fastnet.dom.ChargingLine "
                        + "WHERE kontraktNr == :kontraktNr && "
                        + "exportDate == :exportDate"),
        @Query(
                name = "findUnique", language = "JDOQL",
                value = "SELECT "
                        + "FROM org.estatio.module.fastnet.dom.ChargingLine "
                        + "WHERE kontraktNr == :kontraktNr && "
                        + "kod == :kod && "
                        + "kod2 == :kod2 && "
                        + "fromDat == :fromDat && "
                        + "tomDat == :tomDat && "
                        + "arsBel == :arsBel && "
                        + "evdInSd == :evdInSd "),
})
@Indices({
        @Index(name = "ChargingLine_kontraktNr_IDX", members = { "kontraktNr" })
})
@Uniques({
        @Unique(
                name = "RentRollLine_unique_UNQ",
                members = { "kontraktNr", "kod", "kod2", "fromDat", "tomDat", "arsBel", "evdInSd" }
        )
})
@DomainObject(
        editing = Editing.DISABLED,
        objectType = "org.estatio.module.fastnet.dom.ChargingLine"
)
public class ChargingLine implements Importable{

    public ChargingLine(){}

    @Getter @Setter
    @Column(allowsNull = "false")
    private String klientKod;

    @Getter @Setter
    @Column(allowsNull = "false")
    private String klientNamn;

    @Getter @Setter
    @Column(allowsNull = "false")
    private String fastighetsNr;

    @Getter @Setter
    @Column(allowsNull = "false")
    private String fastighetsBeteckning;

    @Getter @Setter
    @Column(allowsNull = "false")
    private String objektNr;

    // lease number --> lease#externalReference
    @Getter @Setter
    @Column(allowsNull = "false")
    private String kontraktNr;

    @Getter @Setter
    @Column(allowsNull = "false")
    private String kundNr;

    @Getter @Setter
    @Column(allowsNull = "false")
    private String kod;

    @Getter @Setter
    @Column(allowsNull = "false")
    @PropertyLayout(named = "KONT_TEXT")
    private String kontText;

    @Getter @Setter
    @Column(allowsNull = "false")
    private String kod2;

    @Getter @Setter
    @Column(allowsNull = "false")
    @PropertyLayout(named = "KONT_TEXT2")
    private String kontText2;

    // charge date
    // NOTE: We take the string here because the excel import file we have to handle does not use the date format
    @Getter @Setter
    @Column(allowsNull = "false")
    private String fromDat;

    // charge end date
    @Getter @Setter
    @Column(allowsNull = "true")
    private String tomDat;

    @Getter @Setter
    @Column(allowsNull = "false")
    private BigDecimal perBel;

    @Getter @Setter
    @Column(allowsNull = "false")
    // yearly amount (?, amount in swedish = belopp)
    private BigDecimal arsBel;

    // index base date
    @Getter @Setter
    @Column(allowsNull = "true")
    private String basar;

    // invoicing period
    @Getter @Setter
    @Column(allowsNull = "false")
    private String debPer;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "first_pos_start")
    private String firstPosStart;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "deb_ny_index")
    private BigDecimal debNyIndex;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "deb_index_ny_datum")
    private String debIndexNyDatum;

    @Getter @Setter
    @Column(allowsNull = "false")
    @PropertyLayout(named = "enhet_andr")
    private int enhetAndr;

    @Getter @Setter
    @Column(allowsNull = "false")
    @PropertyLayout(named = "adj_freq_months")
    private int adjFreqMonths;

    @Getter @Setter
    @Column(allowsNull = "false")
    @PropertyLayout(named = "adj_delay_months")
    private int adjDelayMonths;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "deb_index_andel")
    private BigDecimal debIndexAndel;

    @Getter @Setter
    @Column(allowsNull = "false")
    @PropertyLayout(named = "bara_hoj")
    private String baraHoj;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "akt_kpi")
    private BigDecimal aktKpi;

    @Getter @Setter
    @Column(allowsNull = "false")
    @PropertyLayout(named = "evd-in-sd")
    private LocalDateTime evdInSd;

    @Getter @Setter
    @Column(allowsNull = "false")
    private LocalDate exportDate;

    @Override
    public List<Object> importData(final Object previousRow){
        if (chargingLineRepository.findUnique(getKontraktNr(), getKod(), getKod2(), getFromDat(), getTomDat(), getArsBel(), getEvdInSd())==null) {
            setExportDate(getEvdInSd().toLocalDate());
            repositoryService.persistAndFlush(this);
        }
        return Collections.emptyList();
    }

    private LocalDate stringToDate(final String dateString) {
        return dateString != null ? LocalDate.parse(dateString) : null;
    }

    @Inject
    ChargingLineRepository chargingLineRepository;

    @Inject
    RepositoryService repositoryService;

}
