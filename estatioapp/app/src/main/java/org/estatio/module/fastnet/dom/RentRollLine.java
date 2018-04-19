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

import org.isisaddons.module.security.dom.tenancy.ApplicationTenancy;
import org.isisaddons.module.security.dom.tenancy.ApplicationTenancyRepository;

import org.estatio.module.base.dom.Importable;
import org.estatio.module.base.dom.UdoDomainObject2;

import lombok.Getter;
import lombok.Setter;


@PersistenceCapable(
        identityType = IdentityType.DATASTORE,
        schema = "fastnet",
        table = "RentRollLine"
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
                        + "FROM org.estatio.module.fastnet.dom.RentRollLine "
                        + "WHERE kontraktNr == :kontraktNr "),
        @Query(
                name = "findByObjektsNummerAndEvdInSd", language = "JDOQL",
                value = "SELECT "
                        + "FROM org.estatio.module.fastnet.dom.RentRollLine "
                        + "WHERE objektsNummer == :objektsNummer && "
                        + "evdInSd == :evdInSd "),
})
@Indices({
        @Index(name = "RentRollLine_kontraktNr_IDX", members = { "kontraktNr" }),
        @Index(name = "RentRollLine_objektsNummer_IDX", members = { "objektsNummer" })
})
@Uniques({
        @Unique(
                name = "RentRollLine_objektsNummer_evdInSd_IDX",
                members = { "objektsNummer", "evdInSd" }
        )
})
@DomainObject(
        editing = Editing.DISABLED,
        objectType = "org.estatio.module.fastnet.dom.RentRollLine"
)
public class RentRollLine extends UdoDomainObject2<RentRollLine> implements Importable{

    public RentRollLine() {
        super("objektsNummer, evdInSd");
    }

    public RentRollLine(
            final String status,
            final String klientNummer,
            final String klientNamn,
            final String fastighetsNummer,
            final String fastighetsBeteckning,
            final String objektsNummer,
            final String kontraktNr,
            final String kundNr,
            final String forvaltaransvarig,
            final String objektTyp,
            final String objektTypKod,
            final String beskrivning,
            final String typeOfPremises,
            final BigDecimal yta,
            final String betpertyp,
            final BigDecimal periodhyra,
            final BigDecimal arshyra,
            final BigDecimal arshyraPerKvm,
            final BigDecimal arsmoms,
            final String hyresgast,
            final String uppsagd,
            final String inflyttningsDatum,
            final String avflyttningsDatum,
            final String senastuppsagd,
            final String kontraktFrom,
            final String kontraktTom,
            final String uppsagningstidHv,
            final String forlangningstidHv,
            final String moms,
            final BigDecimal indextal,
            final BigDecimal indexandel,
            final BigDecimal bashyra,
            final BigDecimal indextillagg,
            final BigDecimal hyrainklindex,
            final BigDecimal fastighetsskatt,
            final BigDecimal fskattproc,
            final BigDecimal varme,
            final BigDecimal el,
            final BigDecimal kyla,
            final BigDecimal va,
            final BigDecimal kabeltv,
            final BigDecimal ovrigt,
            final BigDecimal saTillagg,
            final BigDecimal okand,
            final BigDecimal total,
            final BigDecimal totalKvm,
            final BigDecimal rabatt,
            final BigDecimal procAndring,
            final String notering,
            final String inflyttningsKod,
            final String utflyttningsKod,
            final String utskrivetDatum,
            final String uppsagningstidHg,
            final String forlangningstidHg,
            final String senastuppsagdHg,
            final String uppsagDav,
            final String regDatum,
            final String vakantFrom,
            final BigDecimal omsattning,
            final BigDecimal omsattProc,
            final BigDecimal omsattningHyra,
            final BigDecimal omsMinHyra,
            final String omsBasDat,
            final BigDecimal omsBasIndex,
            final BigDecimal omsAndelProc,
            final BigDecimal omsIndexBel,
            final BigDecimal omsHyraInklIndex,
            final BigDecimal omsOverskut,
            final BigDecimal marknBidrag,
            final String extraUpps1SenastHg,
            final String extraUpps1UtflkodHg,
            final String extraUpps1KontraktTomHg,
            final String extraUppstid1Hg,
            final String extraVillkor1Hg,
            final String extraUpps2SenastHg,
            final String extraUpps2UtflkodHg,
            final String extraUpps2KontraktTomHg,
            final String extraUppstid2Hg,
            final String populärNamn,
            final String rentalUnitStartDate,
            final String rentalUnitEndDate,
            final String extraUpps3SenastHg,
            final String extraUpps3KontraktTomtHg,
            final String extraVillkor3Hg,
            final String typeOfDeposit,
            final BigDecimal depositDebit,
            final String depositRef,
            final String bankName,
            final String expiringDate,
            final String spaceUnitsStartDate,
            final LocalDateTime evdInSd){
        this();
        this.status = status;
        this.klientNummer = klientNummer;
        this.klientNamn = klientNamn;
        this.fastighetsNummer = fastighetsNummer;
        this.fastighetsBeteckning = fastighetsBeteckning;
        this.objektsNummer = objektsNummer;
        this.kontraktNr = kontraktNr;
        this.kundNr = kundNr;
        this.forvaltaransvarig = forvaltaransvarig;
        this.objektTyp = objektTyp;
        this.objektTypKod = objektTypKod;
        this.beskrivning = beskrivning;
        this.typeOfPremises = typeOfPremises;
        this.yta = yta;
        this.betpertyp = betpertyp;
        this.periodhyra = periodhyra;
        this.arshyra = arshyra;
        this.arshyraPerKvm = arshyraPerKvm;
        this.arsmoms = arsmoms;
        this.hyresgast = hyresgast;
        this.uppsagd = uppsagd;
        this.inflyttningsDatum = inflyttningsDatum;
        this.avflyttningsDatum = avflyttningsDatum;
        this.senastuppsagd = senastuppsagd;
        this.kontraktFrom = kontraktFrom;
        this.kontraktTom = kontraktTom;
        this.uppsagningstidHv = uppsagningstidHv;
        this.forlangningstidHv = forlangningstidHv;
        this.moms = moms;
        this.indextal = indextal;
        this.indexandel = indexandel;
        this.bashyra = bashyra;
        this.indextillagg = indextillagg;
        this.hyrainklindex = hyrainklindex;
        this.fastighetsskatt = fastighetsskatt;
        this.fskattproc = fskattproc;
        this.varme = varme;
        this.el = el;
        this.kyla = kyla;
        this.va = va;
        this.kabeltv = kabeltv;
        this.ovrigt = ovrigt;
        this.saTillagg = saTillagg;
        this.okand = okand;
        this.total = total;
        this.totalKvm = totalKvm;
        this.rabatt = rabatt;
        this.procAndring = procAndring;
        this.notering = notering;
        this.inflyttningsKod = inflyttningsKod;
        this.utflyttningsKod = utflyttningsKod;
        this.utskrivetDatum = utskrivetDatum;
        this.uppsagningstidHg = uppsagningstidHg;
        this.forlangningstidHg = forlangningstidHg;
        this.senastuppsagdHg = senastuppsagdHg;
        this.uppsagDav = uppsagDav;
        this.regDatum = regDatum;
        this.vakantFrom = vakantFrom;
        this.omsattning = omsattning;
        this.omsattProc = omsattProc;
        this.omsattningHyra = omsattningHyra;
        this.omsMinHyra = omsMinHyra;
        this.omsBasDat = omsBasDat;
        this.omsBasIndex = omsBasIndex;
        this.omsAndelProc = omsAndelProc;
        this.omsIndexBel = omsIndexBel;
        this.omsHyraInklIndex = omsHyraInklIndex;
        this.omsOverskut = omsOverskut;
        this.marknBidrag = marknBidrag;
        this.extraUpps1SenastHg = extraUpps1SenastHg;
        this.extraUpps1UtflkodHg = extraUpps1UtflkodHg;
        this.extraUpps1KontraktTomHg = extraUpps1KontraktTomHg;
        this.extraUppstid1Hg = extraUppstid1Hg;
        this.extraVillkor1Hg = extraVillkor1Hg;
        this.extraUpps2SenastHg = extraUpps2SenastHg;
        this.extraUpps2UtflkodHg = extraUpps2UtflkodHg;
        this.extraUpps2KontraktTomHg = extraUpps2KontraktTomHg;
        this.extraUppstid2Hg = extraUppstid2Hg;
        this.populärNamn = populärNamn;
        this.rentalUnitStartDate = rentalUnitStartDate;
        this.rentalUnitEndDate = rentalUnitEndDate;
        this.extraUpps3SenastHg = extraUpps3SenastHg;
        this.extraUpps3KontraktTomtHg = extraUpps3KontraktTomtHg;
        this.extraVillkor3Hg = extraVillkor3Hg;
        this.typeOfDeposit = typeOfDeposit;
        this.depositDebit = depositDebit;
        this.depositRef = depositRef;
        this.bankName = bankName;
        this.expiringDate = expiringDate;
        this.spaceUnitsStartDate = spaceUnitsStartDate;
        this.evdInSd = evdInSd;
    }

    @Getter @Setter
    @Column(allowsNull = "false")
    private String status;

    @Getter @Setter
    @Column(allowsNull = "false")
    private String klientNummer;

    @Getter @Setter
    @Column(allowsNull = "false")
    private String klientNamn;

    @Getter @Setter
    @Column(allowsNull = "false")
    private String fastighetsNummer;

    @Getter @Setter
    @Column(allowsNull = "false")
    private String fastighetsBeteckning;

    @Getter @Setter
    @Column(allowsNull = "false")
    private String objektsNummer;

    // lease number --> lease#externalReference
    @Getter @Setter
    @Column(allowsNull = "true")
    private String kontraktNr;

    @Getter @Setter
    @Column(allowsNull = "true")
    private String kundNr;

    @Getter @Setter
    @Column(allowsNull = "true")
    private String forvaltaransvarig;

    @Getter @Setter
    @Column(allowsNull = "false")
    private String objektTyp;

    @Getter @Setter
    @Column(allowsNull = "false")
    private String objektTypKod;

    @Getter @Setter
    @Column(allowsNull = "false")
    private String beskrivning;

    @Getter @Setter
    @Column(allowsNull = "false")
    @PropertyLayout(named = "TYPE_OF_PREMISES")
    private String typeOfPremises;

    @Getter @Setter
    @Column(allowsNull = "true")
    private BigDecimal yta;

    @Getter @Setter
    @Column(allowsNull = "true")
    private String betpertyp;

    @Getter @Setter
    @Column(allowsNull = "true")
    private BigDecimal periodhyra;

    @Getter @Setter
    @Column(allowsNull = "true")
    private BigDecimal arshyra;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "ARSHYRA_PER_KVM")
    private BigDecimal arshyraPerKvm;

    @Getter @Setter
    @Column(allowsNull = "true")
    private BigDecimal arsmoms;

    @Getter @Setter
    @Column(allowsNull = "true")
    private String hyresgast;

    @Getter @Setter
    @Column(allowsNull = "true")
    private String uppsagd;

    @Getter @Setter
    @Column(allowsNull = "true")
    // NOTE: We take the string here because the excel import file we have to handle does not use the date format
    private String inflyttningsDatum;

    // NOTE: this is a kind of work-a-round we could use for all dates imported as string
    public LocalDate getInflyttningsDatumAsDate(){
        return stringToDate(getInflyttningsDatum());
    }

    @Getter @Setter
    @Column(allowsNull = "true")
    private String avflyttningsDatum;

    @Getter @Setter
    @Column(allowsNull = "true")
    private String senastuppsagd;

    @Getter @Setter
    @Column(allowsNull = "true")
    private String kontraktFrom;

    @Getter @Setter
    @Column(allowsNull = "true")
    private String kontraktTom;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "UPPSAGNINGSTID_HV")
    private String uppsagningstidHv;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "FORLANGNINGSTID_HV")
    private String forlangningstidHv;

    @Getter @Setter
    @Column(allowsNull = "true")
    private String moms;

    @Getter @Setter
    @Column(allowsNull = "true")
    private BigDecimal indextal;

    @Getter @Setter
    @Column(allowsNull = "true")
    private BigDecimal indexandel;

    @Getter @Setter
    @Column(allowsNull = "true")
    private BigDecimal bashyra;

    @Getter @Setter
    @Column(allowsNull = "true")
    private BigDecimal indextillagg;

    @Getter @Setter
    @Column(allowsNull = "true")
    private BigDecimal hyrainklindex;

    @Getter @Setter
    @Column(allowsNull = "true")
    private BigDecimal fastighetsskatt;

    @Getter @Setter
    @Column(allowsNull = "true")
    private BigDecimal fskattproc;

    @Getter @Setter
    @Column(allowsNull = "true")
    private BigDecimal varme;

    @Getter @Setter
    @Column(allowsNull = "true")
    private BigDecimal el;

    @Getter @Setter
    @Column(allowsNull = "true")
    private BigDecimal kyla;

    @Getter @Setter
    @Column(allowsNull = "true")
    private BigDecimal va;

    @Getter @Setter
    @Column(allowsNull = "true")
    private BigDecimal kabeltv;

    @Getter @Setter
    @Column(allowsNull = "true")
    private BigDecimal ovrigt;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "SA_TILLAGG")
    private BigDecimal saTillagg;

    @Getter @Setter
    @Column(allowsNull = "true")
    private BigDecimal okand;

    @Getter @Setter
    @Column(allowsNull = "true")
    private BigDecimal total;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "TOTAL_KVM")
    private BigDecimal totalKvm;

    @Getter @Setter
    @Column(allowsNull = "true")
    private BigDecimal rabatt;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "PROC_ANDRING")
    private BigDecimal procAndring;

    @Getter @Setter
    @Column(allowsNull = "true", length = 512)
    private String notering;

    @Getter @Setter
    @Column(allowsNull = "true")
    private String inflyttningsKod;

    @Getter @Setter
    @Column(allowsNull = "true")
    private String utflyttningsKod;

    @Getter @Setter
    @Column(allowsNull = "true")
    private String utskrivetDatum;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "UPPSAGNINGSTID_HG")
    private String uppsagningstidHg;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "FORLANGNINGSTID_HG")
    private String forlangningstidHg;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "SENASTUPPSAGD_HG")
    private String senastuppsagdHg;

    @Getter @Setter
    @Column(allowsNull = "true")
    private String uppsagDav;

    @Getter @Setter
    @Column(allowsNull = "true")
    private String regDatum;

    @Getter @Setter
    @Column(allowsNull = "true")
    private String vakantFrom;

    @Getter @Setter
    @Column(allowsNull = "true")
    private BigDecimal omsattning;

    @Getter @Setter
    @Column(allowsNull = "true")
    private BigDecimal omsattProc;

    @Getter @Setter
    @Column(allowsNull = "true")
    private BigDecimal omsattningHyra;

    @Getter @Setter
    @Column(allowsNull = "true")
    private BigDecimal omsMinHyra;

    @Getter @Setter
    @Column(allowsNull = "true")
    private String omsBasDat;

    @Getter @Setter
    @Column(allowsNull = "true")
    private BigDecimal omsBasIndex;

    @Getter @Setter
    @Column(allowsNull = "true")
    private BigDecimal omsAndelProc;

    @Getter @Setter
    @Column(allowsNull = "true")
    private BigDecimal omsIndexBel;

    @Getter @Setter
    @Column(allowsNull = "true")
    private BigDecimal omsHyraInklIndex;

    @Getter @Setter
    @Column(allowsNull = "true")
    private BigDecimal omsOverskut;

    @Getter @Setter
    @Column(allowsNull = "true")
    private BigDecimal marknBidrag;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "Extra_upps_1_Senast_Hg")
    private String extraUpps1SenastHg;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "Extra_upps_1_Utflkod_Hg")
    private String extraUpps1UtflkodHg;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "Extra_upps_1_Kontrakt_tom_Hg")
    private String extraUpps1KontraktTomHg;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "Extra_uppstid_1_Hg")
    private String extraUppstid1Hg;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "Extra_villkor_1_Hg")
    private String extraVillkor1Hg;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "Extra_upps_2_Senast_Hg")
    private String extraUpps2SenastHg;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "Extra_upps_2_Utflkod_Hg")
    private String extraUpps2UtflkodHg;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "Extra_upps_2_Kontrakt_tom_Hg")
    private String extraUpps2KontraktTomHg;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "Extra_uppstid_2_Hg")
    private String extraUppstid2Hg;

    @Getter @Setter
    @Column(allowsNull = "true")
    private String populärNamn;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "rental_unit_start_date")
    private String rentalUnitStartDate;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "rental_unit_start_date")
    private String rentalUnitEndDate;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "extra_upps_3_senast_hg")
    private String extraUpps3SenastHg;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "extra_upps_3_kontrakt_tom_hg")
    private String extraUpps3KontraktTomtHg;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "extra_villkor_3_hg")
    private String extraVillkor3Hg;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "type_of_deposit")
    private String typeOfDeposit;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "deposit_debit")
    private BigDecimal depositDebit;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "deposit_ref")
    private String depositRef;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "bank_name")
    private String bankName;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "expiring_date")
    private String expiringDate;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "space_units_start_date")
    private String spaceUnitsStartDate;

    @Getter @Setter
    @Column(allowsNull = "true")
    @PropertyLayout(named = "evd-in-sd")
    private LocalDateTime evdInSd;

    @Getter @Setter
    @Column(allowsNull = "false")
    private boolean futureRentRollLine;

    @Override
    public ApplicationTenancy getApplicationTenancy() {
        return applicationTenancyRepository.findByPath("/SWE");
    }

    @Override
    public List<Object> importData(final Object previousRow) {
        if (rentRollLineRepository.findByObjektsNummerAndEvdInSd(getObjektsNummer(), getEvdInSd())==null){
            repositoryService.persistAndFlush(this);
        }
        return Collections.emptyList();
    }

    private LocalDate stringToDate(final String dateString) {
        return dateString != null ? LocalDate.parse(dateString) : null;
    }

    @Inject
    RentRollLineRepository rentRollLineRepository;

    @Inject
    ApplicationTenancyRepository applicationTenancyRepository;

    @Inject
    RepositoryService repositoryService;

}
