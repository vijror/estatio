package org.estatio.module.fastnet.dom;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.services.message.MessageService;

import org.isisaddons.module.excel.dom.ExcelService;

import org.incode.module.base.dom.valuetypes.LocalDateInterval;
import org.incode.module.country.dom.impl.Country;
import org.incode.module.country.dom.impl.CountryRepository;

import org.estatio.module.asset.dom.Property;
import org.estatio.module.asset.dom.PropertyRepository;
import org.estatio.module.charge.dom.Charge;
import org.estatio.module.charge.dom.ChargeRepository;
import org.estatio.module.invoice.dom.PaymentMethod;
import org.estatio.module.lease.dom.InvoicingFrequency;
import org.estatio.module.lease.dom.Lease;
import org.estatio.module.lease.dom.LeaseAgreementRoleTypeEnum;
import org.estatio.module.lease.dom.LeaseItem;
import org.estatio.module.lease.dom.LeaseItemType;
import org.estatio.module.lease.dom.LeaseRepository;
import org.estatio.module.lease.dom.LeaseTerm;
import org.estatio.module.lease.dom.LeaseTermForFixed;
import org.estatio.module.lease.dom.LeaseTermForIndexable;
import org.estatio.module.lease.dom.LeaseTermForServiceCharge;
import org.estatio.module.lease.dom.LeaseTermRepository;

@DomainService(nature = NatureOfService.DOMAIN)
public class FastnetImportService {

    public static final LocalDate EPOCH_DATE_FASTNET_IMPORT = new LocalDate(2018, 01, 01);

    private static final Logger logger = LoggerFactory.getLogger(FastnetImportService.class);

    public FastnetImportManager importFastnetData(final LocalDate exportDate) {

        FastnetImportManager fastnetImportManager = new FastnetImportManager();
        fastnetImportManager.setExportDate(exportDate);
        long start = System.currentTimeMillis();

        List<FastNetRentRollOnLeaseDataLine> potentiallyPartialMatchingDataLines = rentRollOnLeaseDataLineRepo.nonMatchingRentRollLinesForExportDate(exportDate);
        long potentials = System.currentTimeMillis();

        List<FastNetRentRollOnLeaseDataLine> partialMatchingDataLines = getPartiallyMatchingDataLines(potentiallyPartialMatchingDataLines);
        fastnetImportManager.setPartialMatchingDataLines(partialMatchingDataLines);
        long partials = System.currentTimeMillis();

        List<FastNetRentRollOnLeaseDataLine> nonMatchingDataLines = getNonMatchingDataLines(potentiallyPartialMatchingDataLines, partialMatchingDataLines);
        fastnetImportManager.setNonMatchingDataLines(nonMatchingDataLines);
        long nonmatching = System.currentTimeMillis();

        List<FastNetRentRollOnLeaseDataLine> linesWithoutKontraktNumber = rentRollOnLeaseDataLineRepo.findByExternalReferenceAndExportDate(null, exportDate);
        fastnetImportManager.setLinesWithoutKontraktNr(linesWithoutKontraktNumber);
        long nokontraktnr = System.currentTimeMillis();

        List<FastNetRentRollOnLeaseDataLine> matchingRentRollDataLines = rentRollOnLeaseDataLineRepo.matchingRentRollLinesForExportDate(exportDate);
        long matchinglines = System.currentTimeMillis();

        // a bit ugly, but might save some time because we iterate once over matching rent roll data lines
        determineDoubleExternalReferencesAndRentRollLinesWithoutChargingDetails(fastnetImportManager, matchingRentRollDataLines, exportDate);
        long nochargingdetailsanddoubles = System.currentTimeMillis();

        // end of rent roll datalines analysis //////////////////////

        List<FastNetChargingOnLeaseDataLine> chargingDataLines = chargingOnLeaseDataLineRepo.findByExportDate(exportDate);
        long chargingDatalines = System.currentTimeMillis();

        List<Lease> activeLeasesNotInImport = getLeasesNotInImport(chargingDataLines);
        List<LeaseViewModel> toViewmodels = activeLeasesNotInImport.stream().filter(x -> !x.getReference().startsWith("Z-")).map(x -> new LeaseViewModel(x.getReference(), x.getExternalReference())).collect(Collectors.toList());
        fastnetImportManager.getActiveLeasesNotInImport().addAll(toViewmodels);
        long activeleasesnotinimport = System.currentTimeMillis();

        // cleaning
        // again ugly, but might save some time because we iterate once over charging data lines
        List<FastNetChargingOnLeaseDataLine> chargingDataLinesChargeNotFound = new ArrayList<>();
        List<String> matchingKeys = matchingRentRollDataLines.stream().map(l -> l.getKeyToLeaseExternalReference()).collect(Collectors.toList());
        List<FastNetChargingOnLeaseDataLine> notInMatchingRentRollDataLines = new ArrayList<>();
        List<FastNetChargingOnLeaseDataLine> noUpdateNeeded = new ArrayList<>();
        Map<String, List<FastNetChargingOnLeaseDataLine>> chargeRefLineMap = new HashMap<>();
        chargingDataLines.forEach(cdl -> {
            // check on charge
            if (chargeRepository.findByReference(cdl.getKeyToChargeReference()) == null) {
                chargingDataLinesChargeNotFound.add(cdl);
            }
            // check against matchingRentRollDataLines
            if (!matchingKeys.contains(cdl.getKeyToLeaseExternalReference())) {
                notInMatchingRentRollDataLines.add(cdl);
            }

            if (!chargeRefLineMap.containsKey(getChargeRefMapKey(cdl))) {
                chargeRefLineMap.put(getChargeRefMapKey(cdl), new ArrayList<>(Arrays.asList(cdl)));
            } else {
                chargeRefLineMap.get(getChargeRefMapKey(cdl)).add(cdl);
            }

            // determine no update needed
            if (hasNoEffectAtAll(cdl)) {
                noUpdateNeeded.add(cdl);
            }

        });

        chargeRefLineMap.entrySet().removeIf(pair -> pair.getValue().size() == 1);
        List<FastNetChargingOnLeaseDataLine> duplicateChargeReferences = chargeRefLineMap.values().stream().flatMap(List::stream).collect(Collectors.toList());

        chargingDataLines.removeAll(duplicateChargeReferences);
        chargingDataLines.removeAll(chargingDataLinesChargeNotFound);
        chargingDataLines.removeAll(notInMatchingRentRollDataLines);
        chargingDataLines.removeAll(noUpdateNeeded);

        noUpdateNeeded.removeAll(duplicateChargeReferences);

        duplicateChargeReferences.removeAll(notInMatchingRentRollDataLines);

        fastnetImportManager.setDuplicateChargeReferences(duplicateChargeReferences);
        fastnetImportManager.setChargeNotFound(chargingDataLinesChargeNotFound);
        fastnetImportManager.setNoUpdateNeeded(noUpdateNeeded);

        List<FastNetChargingOnLeaseDataLine> chargingDataLinesForItemUpdate = chargingDataLines.stream().filter(cl -> cl.getLeaseTermStartDate() != null).collect(Collectors.toList());
        List<FastNetChargingOnLeaseDataLine> chargingDataLinesForItemCreation = chargingDataLines.stream().filter(cl -> cl.getLeaseTermStartDate() == null).collect(Collectors.toList());
        fastnetImportManager.setLinesForItemUpdate(chargingDataLinesForItemUpdate);
        fastnetImportManager.setLinesForItemCreation(chargingDataLinesForItemCreation);
        long chargenotfoundandnotinmatchingdatalines = System.currentTimeMillis();


        logger.info(String.format("Potentials: %d", (potentials - start) / 1000));
        logger.info(String.format("Partials: %d", (partials - potentials) / 1000));
        logger.info(String.format("Nonmatching lines: %d", (nonmatching - partials) / 1000));
        logger.info(String.format("Nokontraktnr lines: %d", (nokontraktnr - nonmatching) / 1000));
        logger.info(String.format("Matching lines: %d", (matchinglines - nokontraktnr) / 1000));
        logger.info(String.format("No charging details and doubles: %d", (nochargingdetailsanddoubles - matchinglines) / 1000));
        logger.info(String.format("Charging data lines: %d", (chargingDatalines - nochargingdetailsanddoubles) / 1000));
        logger.info(String.format("Charge not found: %d", (chargenotfoundandnotinmatchingdatalines - chargingDatalines) / 1000));
        logger.info(String.format("Active leases not in import: %d", (activeleasesnotinimport - chargenotfoundandnotinmatchingdatalines) / 1000));

        return fastnetImportManager;
    }

    List<Lease> getLeasesNotInImport(final List<FastNetChargingOnLeaseDataLine> chargingDataLines) {
        final List<String> externalRefsInChargingDataLines = chargingDataLines.stream().map(FastNetChargingOnLeaseDataLine::getKeyToLeaseExternalReference).collect(Collectors.toList());
        List<Lease> activeLeasesNotInImport = activeSwedishLeases();
        activeLeasesNotInImport.removeIf(lease -> externalRefsInChargingDataLines.contains(lease.getExternalReference()));
        return activeLeasesNotInImport;
    }

    private String getChargeRefMapKey(final FastNetChargingOnLeaseDataLine cdl) {
        return cdl.getKeyToLeaseExternalReference() + "_" + cdl.getKeyToChargeReference();
    }

    boolean hasNoEffectAtAll(final FastNetChargingOnLeaseDataLine cdl) {

        if (cdl.getLeaseTermStartDate() != null) {

            if (sameDates(cdl) && sameValues(cdl) && sameInvoicingFrequency(cdl)) {
                return true;
            }

        }
        return false;
    }

    boolean sameDates(final FastNetChargingOnLeaseDataLine cdl) {
        if (cdl.getLeaseTermEndDate() != null && cdl.getTomDat()!=null){
            return cdl.getLeaseTermStartDate().equals(stringToDate(cdl.getFromDat())) && cdl.getLeaseTermEndDate().equals(stringToDate(cdl.getTomDat()));
        }
        if (cdl.getLeaseTermEndDate() == null && cdl.getTomDat()==null) {
            return cdl.getLeaseTermStartDate().equals(stringToDate(cdl.getFromDat()));
        }
        // special case for turnover rent fixed that does not allow open enddates
        Charge ch = chargeRepository.findByReference(cdl.getKeyToChargeReference());
        if (ch!=null && ch.getGroup().getReference().equals("SE_TURNOVER_RENT_FIXED")){
            if (cdl.getLeaseTermEndDate()!=null && cdl.getTomDat()==null){
                return true;
            }
        }
        return false;
    }

    boolean sameValues(final FastNetChargingOnLeaseDataLine cdl) {
        final BigDecimal scaledArsBel = cdl.getArsBel().setScale(2, RoundingMode.HALF_UP);
        return scaledArsBel.equals(cdl.getValue()) || scaledArsBel.equals(cdl.getSettledValue()) || scaledArsBel.equals(cdl.getBudgetedValue());
    }

    boolean sameInvoicingFrequency(final FastNetChargingOnLeaseDataLine cdl) {
        final InvoicingFrequency frequency = mapToFrequency(cdl.getDebPer());
        if (frequency != null) {
            return frequency.name().equals(cdl.getInvoicingFrequency());
        }
        return false;
    }

    void determineDoubleExternalReferencesAndRentRollLinesWithoutChargingDetails(final FastnetImportManager fastnetImportManager, final List<FastNetRentRollOnLeaseDataLine> matchingRentRollDataLines, final LocalDate exportDate) {
        List<FastNetRentRollOnLeaseDataLine> dataLinesWithoutChargingDetails = new ArrayList<>();
        List<FastNetRentRollOnLeaseDataLine> doubleExternalReferences;
        Map<String, List<FastNetRentRollOnLeaseDataLine>> externalRefLinePairs = new HashMap<>();
        matchingRentRollDataLines.forEach(line -> {
            ChargingLine cline = chargingLineRepository.findFirstByKeyToLeaseExternalReferenceAndExportDate(line.getKeyToLeaseExternalReference(), exportDate);
            if (cline == null) {
                dataLinesWithoutChargingDetails.add(line);
            }

            if (!externalRefLinePairs.containsKey(line.getKeyToLeaseExternalReference())) {
                externalRefLinePairs.put(line.getKeyToLeaseExternalReference(), new ArrayList<>(Arrays.asList(line)));
            } else {
                externalRefLinePairs.get(line.getKeyToLeaseExternalReference()).add(line);
            }
        });
        matchingRentRollDataLines.removeAll(dataLinesWithoutChargingDetails);
        externalRefLinePairs.entrySet().removeIf(pair -> pair.getValue().size() == 1);
        doubleExternalReferences = externalRefLinePairs.values().stream().flatMap(List::stream).collect(Collectors.toList());
        matchingRentRollDataLines.removeAll(doubleExternalReferences);
        ////////
        fastnetImportManager.setNoChargingDetails(dataLinesWithoutChargingDetails);
        fastnetImportManager.setDoubleExternalReferences(doubleExternalReferences);
        ///////
    }

    List<FastNetRentRollOnLeaseDataLine> getNonMatchingDataLines(final List<FastNetRentRollOnLeaseDataLine> potentiallyPartialMatchingDataLines, final List<FastNetRentRollOnLeaseDataLine> partialMatchingDataLines) {
        List<FastNetRentRollOnLeaseDataLine> nonMatchingDataLines = new ArrayList<>();
        List<String> partialKeys = partialMatchingDataLines.stream().map(FastNetRentRollOnLeaseDataLine::getKeyToLeaseExternalReference).collect(Collectors.toList());
        potentiallyPartialMatchingDataLines.forEach(nml -> {
            if (!partialKeys.contains(nml.getKeyToLeaseExternalReference())) {
                nonMatchingDataLines.add(nml);
            }
        });
        return nonMatchingDataLines;
    }

    List<FastNetRentRollOnLeaseDataLine> getPartiallyMatchingDataLines(final List<FastNetRentRollOnLeaseDataLine> potentiallyPartialMatchingDataLines) {
        List<FastNetRentRollOnLeaseDataLine> partialMatchingDataLines = new ArrayList<>();
        potentiallyPartialMatchingDataLines.forEach(line -> {
            final List<Lease> partialMatchedLeases = leaseRepository.matchLeaseByExternalReference(mapPartialExternalReference(line.getKeyToLeaseExternalReference()));
            if (!partialMatchedLeases.isEmpty()) {
                partialMatchedLeases.forEach(lease -> {
                    FastNetRentRollOnLeaseDataLine lineForPartial = new FastNetRentRollOnLeaseDataLine(
                            line.getKeyToLeaseExternalReference(),
                            line.getExportDate(),
                            line.getKontraktNr(),
                            line.getHyresgast(),
                            line.getKundNr(),
                            line.getArshyra(),
                            line.isFutureRentRollLine(),
                            line.getApplied(),
                            line.getLeaseReference(),
                            line.getExternalReference()
                    );
                    // TODO: finish
                    lineForPartial.setLeaseReference(lease.getReference());
                    lineForPartial.setExternalReference(lease.getExternalReference());
                    partialMatchingDataLines.add(lineForPartial);
                });
            }
        });
        return partialMatchingDataLines;
    }

    List<Lease> findLeaseByExternalReferenceReturnActiveFirst(final String kontraktNrToUse) {
        final List<Lease> candidates = leaseRepository.matchLeaseByExternalReference(kontraktNrToUse);
        // TODO: use max epoch date - startDate
        final List<Lease> activeCandidates = candidates.stream()
                .filter(x -> x.getTenancyEndDate() == null
                        || x.getTenancyEndDate().isAfter(EPOCH_DATE_FASTNET_IMPORT))
                .collect(Collectors.toList());
        return activeCandidates.isEmpty() ? candidates : activeCandidates;
    }

    private InvoicingFrequency mapToFrequency(final String debper) {

        switch (debper) {

            case "Månad":
                return InvoicingFrequency.MONTHLY_IN_ADVANCE;

            case "Kvartal":
                return InvoicingFrequency.QUARTERLY_IN_ADVANCE;

            case "Helår":
                return InvoicingFrequency.YEARLY_IN_ADVANCE;
        }
        return null;
    }

    LocalDate stringToDate(final String dateString) {
        return dateString != null ? LocalDate.parse(dateString) : null;
    }

    public Lease closeAllItemsOfTypeActiveOnEpochDate(final Lease lease, final LeaseItemType leaseItemType) {
        throwExceptionIfLeaseItemTypeIsNotYetImplemented(leaseItemType);
        lease.findItemsOfType(leaseItemType).stream().filter(x -> x.getInterval().contains(EPOCH_DATE_FASTNET_IMPORT.minusDays(1))).forEach(x -> x.setEndDate(EPOCH_DATE_FASTNET_IMPORT.minusDays(1)));
        return lease;
    }

    public void updateItem(final FastNetChargingOnLeaseDataLine cdl, final LocalDate exportDate) {

        final ChargingLine cLine = chargingLineRepository.findFirstByKeyToLeaseExternalReferenceAndExportDate(cdl.getKeyToLeaseExternalReference(), exportDate);

        final Lease lease = leaseRepository.findLeaseByReference(cdl.getLeaseReference());
        final Charge charge = chargeRepository.findByReference(cdl.getKeyToChargeReference());
        LeaseItem itemToUpdate = lease.findFirstItemOfTypeAndCharge(mapToLeaseItemType(charge), charge);
        LeaseTerm termToUpdate = itemToUpdate.findTerm(stringToDate(cdl.getFromDat()));

        // TODO: somehow the dates on cdl for lease, lease item and lease term are all set to LocalDate.now() while
        // they are right when the lines are set on the fastnet import manager
        // maybe because FastNetChargingOnLeaseDataLine is mapped to a DN view??
        if (termToUpdate!=null) {
            //        if (!sameValues(cdl)) {
            updateLeaseTermValue(itemToUpdate, cdl.getArsBel(), termToUpdate);
            //        }
            //        if (!sameDates(cdl)){
            termToUpdate.setEndDate(stringToDate(cdl.getTomDat()));
            //        }
            //        if (!sameInvoicingFrequency(cdl)){
            itemToUpdate.setInvoicingFrequency(mapToFrequency(cdl.getDebPer()));
            //        }
            cLine.setApplied(LocalDate.now());
        } else {
            messageService.warnUser(String.format("Term with charge %s and start date %s could not be found.", charge.getReference(), cdl.getFromDat()));
        }

    }

    public void createItem(final FastNetChargingOnLeaseDataLine cdl) {
        final Lease lease = leaseRepository.findLeaseByReference(cdl.getLeaseReference());
        final Charge charge = chargeRepository.findByReference(cdl.getKeyToChargeReference()); // getChargeReference is null unless an item is found

        LeaseItemType leaseItemType = mapToLeaseItemType(charge);
        closeAllItemsOfTypeActiveOnEpochDate(lease, leaseItemType);

        final LeaseItem leaseItem = findOrCreateLeaseItemForTypeAndCharge(lease, leaseItemType, charge, mapToFrequency(cdl.getDebPer()), stringToDate(cdl.getFromDat()));
        createNewTermAndCloseExistingIfOverlappingAndOpenEnded(leaseItem, cdl.getArsBel(), stringToDate(cdl.getFromDat()), stringToDate(cdl.getTomDat()));
    }

    LeaseItemType mapToLeaseItemType(final Charge charge) {
        return LeaseItemType.valueOf(charge.getGroup().getReference().replace("SE_", "")); // by convention
    }

    public LeaseItem findOrCreateLeaseItemForTypeAndCharge(final Lease lease, final LeaseItemType leaseItemType, final Charge charge, final InvoicingFrequency invoicingFrequency, final LocalDate startDate) {

        throwExceptionIfLeaseItemTypeIsNotYetImplemented(leaseItemType);

        LocalDate startDateToUse = startDate == null || EPOCH_DATE_FASTNET_IMPORT.isAfter(startDate) ? EPOCH_DATE_FASTNET_IMPORT : startDate;

        List<LeaseItem> candidates = lease.findItemsOfType(leaseItemType).stream().filter(x -> x.getCharge().equals(charge)).collect(Collectors.toList());
        if (candidates.size() > 1) {
            throw new RuntimeException("Multiple lease items of type " + leaseItemType + " and charge " + charge.getReference() + " found for lease " + lease.getReference());
        }
        LeaseItem leaseItem;
        if (candidates.size() == 1) {
            leaseItem = candidates.get(0);
        } else {
            leaseItem = lease.newItem(leaseItemType, LeaseAgreementRoleTypeEnum.LANDLORD, charge, invoicingFrequency, PaymentMethod.BANK_TRANSFER, startDateToUse);
        }
        return leaseItem;
    }

    private void throwExceptionIfLeaseItemTypeIsNotYetImplemented(final LeaseItemType leaseItemType) {
        if (!Arrays.asList(LeaseItemType.RENT, LeaseItemType.TURNOVER_RENT_FIXED, LeaseItemType.SERVICE_CHARGE, LeaseItemType.PROPERTY_TAX, LeaseItemType.MARKETING, LeaseItemType.RENT_DISCOUNT_FIXED, LeaseItemType.RENT_FIXED).contains(leaseItemType)) {
            throw new RuntimeException("Type  " + leaseItemType + " not yet supported");
        }
    }

    LeaseTerm createNewTermAndCloseExistingIfOverlappingAndOpenEnded(final LeaseItem leaseItem, final BigDecimal amount, final LocalDate startDate, final LocalDate endDate) {

        if (endDate == null) {
            closeOverlappingOpenEndedExistingTerms(leaseItem, startDate, endDate);
        }
        LeaseTerm leaseTerm = leaseItem.newTerm(startDate, endDate);
        return updateLeaseTermValue(leaseItem, amount, leaseTerm);
    }

    LeaseTerm updateLeaseTermValue(final LeaseItem leaseItem, final BigDecimal amount, final LeaseTerm leaseTerm) {
        final BigDecimal value = amount.setScale(2, RoundingMode.HALF_UP);
        switch (leaseItem.getType()) {

            case RENT:
                LeaseTermForIndexable termForIndexable;

                termForIndexable = (LeaseTermForIndexable) leaseTerm;
                termForIndexable.setSettledValue(value);
                if (leaseItem.getCharge().getReference().endsWith("-1")) {
                    // we assume that the first line always contains the base rent
                    termForIndexable.setBaseValue(value);
                }

                return termForIndexable;

            case TURNOVER_RENT_FIXED:
                LeaseTermForFixed termForTurnoverRent;
                termForTurnoverRent = (LeaseTermForFixed) leaseTerm;
                termForTurnoverRent.setValue(value);

                return termForTurnoverRent;

            case SERVICE_CHARGE:
                LeaseTermForServiceCharge termForServiceCharge;
                termForServiceCharge = (LeaseTermForServiceCharge) leaseTerm;
                termForServiceCharge.setBudgetedValue(value);
                return termForServiceCharge;

            case PROPERTY_TAX:
                LeaseTermForServiceCharge termForPropertyTax;
                termForPropertyTax = (LeaseTermForServiceCharge) leaseTerm;
                termForPropertyTax.setBudgetedValue(value);
                return termForPropertyTax;

            case MARKETING:
                LeaseTermForServiceCharge termForMarketing;
                termForMarketing = (LeaseTermForServiceCharge) leaseTerm;
                termForMarketing.setBudgetedValue(value);
                return termForMarketing;

            case RENT_DISCOUNT_FIXED:
                LeaseTermForFixed termForRentDiscount;
                termForRentDiscount = (LeaseTermForFixed) leaseTerm;
                termForRentDiscount.setValue(value);

                return termForRentDiscount;

            case RENT_FIXED:
                LeaseTermForFixed termForRentFixed;
                termForRentFixed = (LeaseTermForFixed) leaseTerm;
                termForRentFixed.setValue(value);

                return termForRentFixed;

            default:
                // TODO: add support for other types when the time is right
        }
        return null;
    }

    void closeOverlappingOpenEndedExistingTerms(final LeaseItem leaseItem, final LocalDate startDate, final LocalDate endDate) {
        for (LeaseTerm existingTerm : leaseItem.getTerms()) {
            if (existingTerm.getInterval().overlaps(new LocalDateInterval(startDate, endDate)) && existingTerm.getInterval().isOpenEnded()) {
                if (existingTerm.getStartDate().isBefore(startDate)) { // Extra guard for non realistic case - should only happen in integ test ;-)
                    existingTerm.setEndDate(startDate.minusDays(1));
                }
            }
        }
    }

    String mapKontraktNrToExternalReference(String kontraktnr) {
        return kontraktnr != null ? kontraktnr.trim().substring(2) : null;
    }

    String mapPartialExternalReference(String reference) {
        // 1234-4567-02 <=> 1234-4567 of ook 1234-4567-01
        return reference != null ? reference.trim().substring(0, 9) : null;
    }

    Charge mapToCharge(final String kod, final String kod2) {
        if (kod == null || kod2 == null) {
            return null;
        }
        return chargeRepository.findByReference("SE".concat(kod).concat("-").concat(kod2));
    }

    //    @Programmatic
    //    public List<Lease> activeLeasesNotInImport(final List<SweImportResult> results) {
    //
    //        List<Lease> notInImport = new ArrayList<>();
    //
    //        for (Lease lease : activeSwedishLeases()) {
    //            boolean found = false;
    //            for (SweImportResult result : results) {
    //
    //                if (result.getLease() != null && result.getLease().equals(lease)) {
    //                    found = true;
    //                    break;
    //                }
    //
    //            }
    //
    //            if (!found) {
    //                notInImport.add(lease);
    //            }
    //
    //        }
    //
    //        return notInImport.stream().filter(x -> !x.getReference().startsWith("Z-")).collect(Collectors.toList());
    //
    //    }

    List<Lease> activeSwedishLeases() {
        List<Lease> result = new ArrayList<>();
        Country sweden = countryRepository.findCountry("SWE");
        List<Property> propertiesForSweden = propertyRepository.allProperties().stream()
                .filter(
                        x -> x.getCountry().equals(sweden)
                                &&
                                (x.getDisposalDate() == null
                                        ||
                                        x.getDisposalDate().isAfter(LocalDate.now()))
                ).collect(Collectors.toList());
        propertiesForSweden.forEach(x -> result.addAll(leaseRepository.findByAssetAndActiveOnDate(x, LocalDate.now())));
        return result;
    }

    void setExternalReference(final String leaseReference, final String externalReference) {

        Lease leaseCandidate = leaseRepository.findLeaseByReference(leaseReference);
        if (leaseCandidate != null) {
            leaseCandidate.setExternalReference(externalReference);
        }

    }

    @Inject
    LeaseRepository leaseRepository;

    @Inject
    LeaseTermRepository leaseTermRepository;

    @Inject
    ExcelService excelService;

    @Inject
    ChargeRepository chargeRepository;

    @Inject PropertyRepository propertyRepository;

    @Inject CountryRepository countryRepository;

    @Inject RentRollLineRepository rentRollLineRepository;

    @Inject ChargingLineRepository chargingLineRepository;

    @Inject FastNetRentRollOnLeaseDataLineRepo rentRollOnLeaseDataLineRepo;

    @Inject FastNetChargingOnLeaseDataLineRepo chargingOnLeaseDataLineRepo;

    @Inject MessageService messageService;

}
