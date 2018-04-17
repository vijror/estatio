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

@DomainService(
        nature = NatureOfService.DOMAIN,
        repositoryFor = RentRollLine.class
)
public class RentRollLineRepository {

    @Programmatic
    public List<RentRollLine> listAll() {
        return repositoryService.allInstances(RentRollLine.class);
    }

    @Programmatic
    public List<RentRollLine> findByKontraktNr(final String kontraktNr) {
        return repositoryService.allMatches(
                new QueryDefault<>(
                        RentRollLine.class,
                        "findByKontraktNr",
                        "KONTRAKTNR", kontraktNr));
    }

    @Programmatic
    public RentRollLine findByObjektsNummerAndEvdInSd(final String objektsNummer, final LocalDateTime evdInSd) {
        return repositoryService.uniqueMatch(
                new QueryDefault<>(
                        RentRollLine.class,
                        "findByObjektsNummerAndEvdInSd",
                        "objektsNummer", objektsNummer,
                        "evdInSd", evdInSd));
    }

    @Programmatic
    public RentRollLine create(
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
            final LocalDate inflyttningsDatum,
            final LocalDate avflyttningsDatum,
            final LocalDate senastuppsagd,
            final LocalDate kontraktFrom,
            final LocalDate kontraktTom,
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
            final LocalDate senastuppsagdHg,
            final String uppsagDav,
            final LocalDate regDatum,
            final LocalDate vakantFrom,
            final BigDecimal omsattning,
            final BigDecimal omsattProc,
            final BigDecimal omsattningHyra,
            final BigDecimal omsMinHyra,
            final LocalDate omsBasDat,
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
            final LocalDate rentalUnitStartDate,
            final LocalDate rentalUnitEndDate,
            final LocalDate spaceUnitsStartDate,
            final LocalDateTime evdInSd) {

        final RentRollLine line = new RentRollLine(
                status,
                klientNummer,
                klientNamn,
                fastighetsNummer,
                fastighetsBeteckning,
                objektsNummer,
                kontraktNr,
                kundNr,
                forvaltaransvarig,
                objektTyp,
                objektTypKod,
                beskrivning,
                typeOfPremises,
                yta,
                betpertyp,
                periodhyra,
                arshyra,
                arshyraPerKvm,
                arsmoms,
                hyresgast,
                uppsagd,
                inflyttningsDatum,
                avflyttningsDatum,
                senastuppsagd,
                kontraktFrom,
                kontraktTom,
                uppsagningstidHv,
                forlangningstidHv,
                moms,
                indextal,
                indexandel,
                bashyra,
                indextillagg,
                hyrainklindex,
                fastighetsskatt,
                fskattproc,
                varme,
                el,
                kyla,
                va,
                kabeltv,
                ovrigt,
                saTillagg,
                okand,
                total,
                totalKvm,
                rabatt,
                procAndring,
                notering,
                inflyttningsKod,
                utflyttningsKod,
                utskrivetDatum,
                uppsagningstidHg,
                forlangningstidHg,
                senastuppsagdHg,
                uppsagDav,
                regDatum,
                vakantFrom,
                omsattning,
                omsattProc,
                omsattningHyra,
                omsMinHyra,
                omsBasDat,
                omsBasIndex,
                omsAndelProc,
                omsIndexBel,
                omsHyraInklIndex,
                omsOverskut,
                marknBidrag,
                extraUpps1SenastHg,
                extraUpps1UtflkodHg,
                extraUpps1KontraktTomHg,
                extraUppstid1Hg,
                extraVillkor1Hg,
                extraUpps2SenastHg,
                extraUpps2UtflkodHg,
                extraUpps2KontraktTomHg,
                extraUppstid2Hg,
                populärNamn,
                rentalUnitStartDate,
                rentalUnitEndDate,
                spaceUnitsStartDate,
                evdInSd
        );
        serviceRegistry2.injectServicesInto(line);
        repositoryService.persistAndFlush(line);
        return line;
    }

    @Inject
    RepositoryService repositoryService;

    @Inject
    ServiceRegistry2 serviceRegistry2;

}
