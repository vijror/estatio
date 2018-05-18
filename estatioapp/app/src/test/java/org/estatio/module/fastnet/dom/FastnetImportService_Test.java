package org.estatio.module.fastnet.dom;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.joda.time.LocalDate;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

import org.estatio.module.charge.dom.Charge;
import org.estatio.module.charge.dom.ChargeGroup;
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

import static org.assertj.core.api.Assertions.assertThat;

public class FastnetImportService_Test {

    @Test
    public void mapPartionalKontraktNr() throws Exception {

        // given
        FastnetImportService service = new FastnetImportService();

        // when
        String number = "1234-4567-02";

        // then
        assertThat(service.mapPartialExternalReference(number)).isEqualTo("1234-4567");

    }

    @Test
    public void reference_comparison_test() throws Exception {

        final String externalReference1 = "1234-5678-12";
        final String externalReference2 = "1234-5678-13";
        final String externalReference3 = "1234-5678-01";
        final String externalReference4 = "1234-5678-02";

        assertThat(externalReference1.compareTo(externalReference2)).isLessThan(0);
        assertThat(externalReference3.compareTo(externalReference4)).isLessThan(0);
        assertThat(externalReference3.compareTo(externalReference1)).isLessThan(0);

    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Test
    public void find_or_create_works_when_item_not_found() throws Exception {

        // given
        FastnetImportService service = new FastnetImportService();
        LeaseItem leaseItem = new LeaseItem();
        LeaseItemType itemType = LeaseItemType.RENT;
        Charge charge = new Charge();
        InvoicingFrequency frequency = InvoicingFrequency.QUARTERLY_IN_ADVANCE;
        LocalDate startdate = new LocalDate(2018, 01, 01);

        // expect
        context.checking(new Expectations(){{
            oneOf(mockLease).findItemsOfType(LeaseItemType.RENT);
            will(Expectations.returnValue(Arrays.asList()));
            oneOf(mockLease).newItem(itemType, LeaseAgreementRoleTypeEnum.LANDLORD,charge , frequency, PaymentMethod.BANK_TRANSFER, startdate);
            will(Expectations.returnValue(leaseItem));
        }});

        // when
        service.findOrCreateLeaseItemForTypeAndCharge(mockLease, itemType, charge, frequency, startdate);

        // then
        assertThat(leaseItem.getEndDate()).isEqualTo(null);

    }

    @Test
    public void find_or_create_works_when_one_item_is_found() throws Exception {

        // given
        FastnetImportService service = new FastnetImportService();
        Charge charge = new Charge();
        LeaseItem leaseItem = new LeaseItem();
        leaseItem.setCharge(charge);
        LocalDate startdate = new LocalDate(2018, 01, 01);
        leaseItem.setStartDate(startdate);
        LeaseItemType itemType = LeaseItemType.RENT;
        InvoicingFrequency frequency = InvoicingFrequency.QUARTERLY_IN_ADVANCE;

        // expect
        context.checking(new Expectations(){{
            oneOf(mockLease).findItemsOfType(LeaseItemType.RENT);
            will(Expectations.returnValue(Arrays.asList(leaseItem)));
        }});

        // when
        service.findOrCreateLeaseItemForTypeAndCharge(mockLease, itemType, charge, frequency, startdate);

    }

    @Test
    public void find_or_create_works_when_more_than_one_item_is_found() throws Exception {

        // given
        FastnetImportService service = new FastnetImportService();
        Charge charge = new Charge();
        charge.setReference("CH_REF");
        LocalDate startdate = new LocalDate(2018, 01, 01);
        LeaseItem leaseItem1 = new LeaseItem();
        LeaseItem leaseItem2 = new LeaseItem();
        leaseItem1.setCharge(charge);
        leaseItem2.setCharge(charge);
        leaseItem1.setStartDate(startdate);
        leaseItem2.setStartDate(startdate);
        LeaseItemType itemType = LeaseItemType.RENT;
        InvoicingFrequency frequency = InvoicingFrequency.QUARTERLY_IN_ADVANCE;

        // expect
        context.checking(new Expectations(){{
            oneOf(mockLease).findItemsOfType(LeaseItemType.RENT);
            will(Expectations.returnValue(Arrays.asList(leaseItem1, leaseItem2)));
            oneOf(mockLease).getReference();
            will(Expectations.returnValue("LEASE_REF"));
        }});

        // then
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Multiple lease items of type RENT and charge CH_REF found for lease LEASE_REF");

        // when
        service.findOrCreateLeaseItemForTypeAndCharge(mockLease, itemType, charge, frequency, startdate);
    }

    @Mock
    ChargeRepository mockChargeRepository;

    @Test
    public void same_dates_works() throws Exception {

        FastnetImportService service = new FastnetImportService();
        service.chargeRepository = mockChargeRepository;
        FastNetChargingOnLeaseDataLine cdl = new FastNetChargingOnLeaseDataLine();
        cdl.setLeaseTermStartDate(new LocalDate(2018,1,1));
        //when, then
        assertThat(service.sameDates(cdl)).isFalse();

        // and when
        cdl.setFromDat("2018-01-01");
        cdl.setTomDat("2019-01-01");

        // expect
        context.checking(new Expectations(){{
            oneOf(mockChargeRepository).findByReference(cdl.getKeyToChargeReference());
            will(returnValue(null));
        }});

        // then
        assertThat(service.sameDates(cdl)).isFalse();

        // and when
        cdl.setFromDat("2018-01-01");
        cdl.setTomDat(null);

        // then
        assertThat(service.sameDates(cdl)).isTrue();

        // and when
        cdl.setLeaseTermEndDate(new LocalDate(2019,01,01));
        cdl.setFromDat("2018-01-01");
        cdl.setTomDat("2019-01-01");

        // then
        assertThat(service.sameDates(cdl)).isTrue();
    }

    @Test
    public void samedate_exception_for_turnover_rent_fixed_works() throws Exception {

        // given
        FastnetImportService service = new FastnetImportService();
        service.chargeRepository = mockChargeRepository;
        Charge charge = new Charge();
        ChargeGroup group = new ChargeGroup();
        group.setReference("SE_TURNOVER_RENT_FIXED");
        charge.setGroup(group);

        FastNetChargingOnLeaseDataLine cdl = new FastNetChargingOnLeaseDataLine();
        cdl.setLeaseTermStartDate(new LocalDate(2018,1,1));
        cdl.setLeaseTermEndDate(new LocalDate(2018,12,31));

        // expect
        context.checking(new Expectations(){{
            oneOf(mockChargeRepository).findByReference(cdl.getKeyToChargeReference());
            will(returnValue(charge));
        }});

        // when
        cdl.setFromDat("2018-01-01");
        cdl.setTomDat(null);

        // then
        assertThat(service.sameDates(cdl)).isTrue();

    }

    @Test
    public void string_to_date_test() throws Exception {

        // given
        FastnetImportService service = new FastnetImportService();

        // when, then
        assertThat(service.stringToDate("2018-09-30")).isEqualTo(new LocalDate(2018, 9, 30));
        assertThat(service.stringToDate("2014-04-01")).isEqualTo(new LocalDate(2014, 4, 1));

    }

    @Test
    public void close_overlapping_existing_terms_works() throws Exception {

        // given
        FastnetImportService service = new FastnetImportService();
        LeaseItem leaseItem = new LeaseItem();
        LocalDate startDateNewTerm = new LocalDate(2018, 1,1);
        LocalDate endDateNewTerm = new LocalDate(2018, 7,1);

        LeaseTerm overlappingClosedTerm = new LeaseTermForIndexable();
        LeaseTerm overlappingOpenTerm = new LeaseTermForIndexable();
        LeaseTerm nonOverlappingOpenTerm = new LeaseTermForIndexable();

        overlappingClosedTerm.setStartDate(new LocalDate(2016,1,1));
        final LocalDate endDateOverlappingTerm = new LocalDate(2018, 4, 1);
        overlappingClosedTerm.setEndDate(endDateOverlappingTerm);

        overlappingOpenTerm.setStartDate(new LocalDate(2017, 1, 1));

        nonOverlappingOpenTerm.setStartDate(new LocalDate(2018,7,2));

        leaseItem.getTerms().addAll(Arrays.asList(overlappingClosedTerm, overlappingOpenTerm, nonOverlappingOpenTerm));


        // when
        service.closeOverlappingOpenEndedExistingTerms(leaseItem, startDateNewTerm, endDateNewTerm);

        // then
        assertThat(overlappingClosedTerm.getEndDate()).isEqualTo(endDateOverlappingTerm);
        assertThat(overlappingOpenTerm.getEndDate()).isEqualTo(startDateNewTerm.minusDays(1));
        assertThat(nonOverlappingOpenTerm.getEndDate()).isNull();

    }

    @Mock LeaseRepository mockLeaseRepository;

    @Test
    public void find_lease_by_external_reference_return_active_first_works() throws Exception {

        // given
        FastnetImportService service = new FastnetImportService();
        service.leaseRepository = mockLeaseRepository;
        Lease leaseActive = new Lease();
        leaseActive.setTenancyEndDate(service.EPOCH_DATE_FASTNET_IMPORT.plusDays(1));
        Lease leaseActive2 = new Lease();
        Lease leaseInactive = new Lease();
        leaseInactive.setTenancyEndDate(service.EPOCH_DATE_FASTNET_IMPORT);


        // expect
        context.checking(new Expectations(){{
            oneOf(mockLeaseRepository).matchLeaseByExternalReference("1234-5678-01");
            will(Expectations.returnValue(Arrays.asList(leaseActive, leaseActive2, leaseInactive)));
        }});

        // when
        List<Lease> result = service.findLeaseByExternalReferenceReturnActiveFirst("1234-5678-01");

        // then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).contains(leaseActive);
        assertThat(result).contains(leaseActive2);
        assertThat(result).doesNotContain(leaseInactive);

    }

    @Test
    public void find_lease_by_external_reference_return_active_first_when_no_active_found_works() throws Exception {

        // given
        FastnetImportService service = new FastnetImportService();
        service.leaseRepository = mockLeaseRepository;
        Lease leaseInactive = new Lease();
        leaseInactive.setTenancyEndDate(service.EPOCH_DATE_FASTNET_IMPORT);


        // expect
        context.checking(new Expectations(){{
            oneOf(mockLeaseRepository).matchLeaseByExternalReference("1234-5678-01");
            will(Expectations.returnValue(Arrays.asList(leaseInactive)));
        }});

        // when
        List<Lease> result = service.findLeaseByExternalReferenceReturnActiveFirst("1234-5678-01");

        // then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result).contains(leaseInactive);

    }

    @Mock MessageService mockMessageService;

    @Test
    public void update_item_and_term_when_lease_not_found() throws Exception {

        // given
        FastnetImportService service = new FastnetImportService();
        service.leaseRepository = mockLeaseRepository;
        service.messageService = mockMessageService;
        ChargingLine cLine = new ChargingLine();
        cLine.setKeyToLeaseExternalReference("ABCD");

        // expect
        context.checking(new Expectations(){{
            oneOf(mockLeaseRepository).matchLeaseByExternalReference(cLine.getKeyToLeaseExternalReference());
            will(returnValue(Arrays.asList()));
            oneOf(mockMessageService).warnUser("Lease with external reference ABCD not found.");
        }});

        // when
        service.updateOrCreateItemAndTerm(cLine);

    }

    @Test
    public void update_item_and_term_when_charge_not_found() throws Exception {

        // given
        FastnetImportService service = new FastnetImportService();
        service.leaseRepository = mockLeaseRepository;
        service.chargeRepository = mockChargeRepository;
        service.messageService = mockMessageService;
        ChargingLine cLine = new ChargingLine();
        cLine.setKeyToLeaseExternalReference("ABCD");
        cLine.setKeyToChargeReference("SE123-4");

        // expect
        context.checking(new Expectations(){{
            oneOf(mockLeaseRepository).matchLeaseByExternalReference(cLine.getKeyToLeaseExternalReference());
            will(returnValue(Arrays.asList(new Lease())));
            oneOf(mockChargeRepository).findByReference(cLine.getKeyToChargeReference());
            will(returnValue(null));
            oneOf(mockMessageService).warnUser("Charge with reference SE123-4 not found for lease ABCD.");
        }});

        // when
        service.updateOrCreateItemAndTerm(cLine);

    }

    @Mock
    Lease mockLease;

    @Mock
    LeaseItem mockLeaseItem;

    @Test
    public void update_item_and_term_when_term_not_found() throws Exception {

        // given
        FastnetImportService service = new FastnetImportService();
        service.leaseRepository = mockLeaseRepository;
        service.chargeRepository = mockChargeRepository;
        service.messageService = mockMessageService;
        ChargingLine cLine = new ChargingLine();
        cLine.setKeyToLeaseExternalReference("ABCD");
        cLine.setKeyToChargeReference("SE123-4");
        cLine.setFromDat("2018-01-01");
        Charge charge = new Charge();
        charge.setReference(cLine.getKeyToChargeReference());
        ChargeGroup chargeGroup = new ChargeGroup();
        chargeGroup.setReference("SE_RENT");
        charge.setGroup(chargeGroup);

        // expect
        context.checking(new Expectations(){{
            oneOf(mockLeaseRepository).matchLeaseByExternalReference(cLine.getKeyToLeaseExternalReference());
            will(returnValue(Arrays.asList(mockLease)));
            oneOf(mockChargeRepository).findByReference(cLine.getKeyToChargeReference());
            will(returnValue(charge));
            oneOf(mockLease).findFirstItemOfTypeAndCharge(service.mapToLeaseItemType(charge), charge);
            will(returnValue(mockLeaseItem));
            oneOf(mockLeaseItem).findTerm(service.stringToDate(cLine.getFromDat()));
            will(returnValue(null));
            oneOf(mockMessageService).warnUser("Term with start date 2018-01-01 not found for charge SE123-4 on lease ABCD.");
        }});

        // when
        service.updateOrCreateItemAndTerm(cLine);

    }

    @Test
    public void update_item_and_term_when_frequency_not_found() throws Exception {

        // given
        FastnetImportService service = new FastnetImportService();
        service.leaseRepository = mockLeaseRepository;
        service.chargeRepository = mockChargeRepository;
        service.messageService = mockMessageService;
        ChargingLine cLine = new ChargingLine();
        cLine.setKeyToLeaseExternalReference("ABCD");
        cLine.setKeyToChargeReference("SE123-4");
        cLine.setFromDat("2018-01-01");
        cLine.setDebPer("some_thing_not_recognized");
        Charge charge = new Charge();
        charge.setReference(cLine.getKeyToChargeReference());
        ChargeGroup chargeGroup = new ChargeGroup();
        chargeGroup.setReference("SE_RENT");
        charge.setGroup(chargeGroup);
        LeaseTerm leaseTerm = new LeaseTermForIndexable();

        // expect
        context.checking(new Expectations(){{
            oneOf(mockLeaseRepository).matchLeaseByExternalReference(cLine.getKeyToLeaseExternalReference());
            will(returnValue(Arrays.asList(mockLease)));
            oneOf(mockChargeRepository).findByReference(cLine.getKeyToChargeReference());
            will(returnValue(charge));
            oneOf(mockLease).findFirstItemOfTypeAndCharge(service.mapToLeaseItemType(charge), charge);
            will(returnValue(mockLeaseItem));
            oneOf(mockLeaseItem).findTerm(service.stringToDate(cLine.getFromDat()));
            will(returnValue(leaseTerm));
            oneOf(mockLeaseItem).getType();
            will(returnValue(LeaseItemType.RENT));
            oneOf(mockLeaseItem).getCharge();
            will(returnValue(charge));
            oneOf(mockMessageService).warnUser("Value debPer some_thing_not_recognized could not be mapped to invoicing frequency for charge SE123-4 on lease ABCD.");
        }});

        // when
        service.updateOrCreateItemAndTerm(cLine);

    }

    @Test
    public void update_item_and_term_when_value_not_found() throws Exception {

        // given
        FastnetImportService service = new FastnetImportService();
        service.leaseRepository = mockLeaseRepository;
        service.chargeRepository = mockChargeRepository;
        service.messageService = mockMessageService;
        ChargingLine cLine = new ChargingLine();
        cLine.setKeyToLeaseExternalReference("ABCD");
        cLine.setKeyToChargeReference("SE123-4");
        cLine.setFromDat("2018-01-01");
        cLine.setTomDat("2018-12-31");
        cLine.setDebPer("Månad");
        Charge charge = new Charge();
        charge.setReference(cLine.getKeyToChargeReference());
        ChargeGroup chargeGroup = new ChargeGroup();
        chargeGroup.setReference("SE_RENT");
        charge.setGroup(chargeGroup);
        LeaseTermForIndexable leaseTerm = new LeaseTermForIndexable();

        // expect
        context.checking(new Expectations(){{
            oneOf(mockLeaseRepository).matchLeaseByExternalReference(cLine.getKeyToLeaseExternalReference());
            will(returnValue(Arrays.asList(mockLease)));
            oneOf(mockChargeRepository).findByReference(cLine.getKeyToChargeReference());
            will(returnValue(charge));
            oneOf(mockLease).findFirstItemOfTypeAndCharge(service.mapToLeaseItemType(charge), charge);
            will(returnValue(mockLeaseItem));
            oneOf(mockLeaseItem).findTerm(service.stringToDate(cLine.getFromDat()));
            will(returnValue(leaseTerm));
            oneOf(mockLeaseItem).getType();
            will(returnValue(LeaseItemType.RENT));
            oneOf(mockLeaseItem).getCharge();
            will(returnValue(charge));
            oneOf(mockLeaseItem).setInvoicingFrequency(InvoicingFrequency.MONTHLY_IN_ADVANCE);
        }});

        // when
        service.updateOrCreateItemAndTerm(cLine);

        // then
        assertThat(leaseTerm.getSettledValue()).isEqualTo(BigDecimal.ZERO.setScale(2));
    }

    @Test
    public void update_item_and_term_when_all_is_fine() throws Exception {

        // given
        FastnetImportService service = new FastnetImportService();
        service.leaseRepository = mockLeaseRepository;
        service.chargeRepository = mockChargeRepository;
        service.messageService = mockMessageService;
        ChargingLine cLine = new ChargingLine();
        cLine.setKeyToLeaseExternalReference("ABCD");
        cLine.setKeyToChargeReference("SE123-4");
        cLine.setFromDat("2018-01-01");
        cLine.setTomDat("2018-12-31");
        cLine.setDebPer("Månad");
        final BigDecimal arsBel = new BigDecimal("1234.56");
        cLine.setArsBel(arsBel);
        Charge charge = new Charge();
        charge.setReference(cLine.getKeyToChargeReference());
        ChargeGroup chargeGroup = new ChargeGroup();
        chargeGroup.setReference("SE_RENT");
        charge.setGroup(chargeGroup);
        LeaseTermForIndexable leaseTerm = new LeaseTermForIndexable();

        // expect
        context.checking(new Expectations(){{
            oneOf(mockLeaseRepository).matchLeaseByExternalReference(cLine.getKeyToLeaseExternalReference());
            will(returnValue(Arrays.asList(mockLease)));
            oneOf(mockChargeRepository).findByReference(cLine.getKeyToChargeReference());
            will(returnValue(charge));
            oneOf(mockLease).findFirstItemOfTypeAndCharge(service.mapToLeaseItemType(charge), charge);
            will(returnValue(mockLeaseItem));
            oneOf(mockLeaseItem).findTerm(service.stringToDate(cLine.getFromDat()));
            will(returnValue(leaseTerm));
            oneOf(mockLeaseItem).getType();
            will(returnValue(LeaseItemType.RENT));
            oneOf(mockLeaseItem).getCharge();
            will(returnValue(charge));
            oneOf(mockLeaseItem).setInvoicingFrequency(InvoicingFrequency.MONTHLY_IN_ADVANCE);
        }});

        // when
        service.updateOrCreateItemAndTerm(cLine);

        // then
        assertThat(leaseTerm.getSettledValue()).isEqualTo(arsBel);
        assertThat(leaseTerm.getEndDate()).isEqualTo(new LocalDate(2018, 12, 31));
    }

    @Test
    public void create_item_and_term_when_lease_not_found() throws Exception {

        // given
        FastnetImportService service = new FastnetImportService();
        service.leaseRepository = mockLeaseRepository;
        service.messageService = mockMessageService;
        ChargingLine cLine = new ChargingLine();
        cLine.setKeyToLeaseExternalReference("ABCD");

        // expect
        context.checking(new Expectations(){{
            oneOf(mockLeaseRepository).matchLeaseByExternalReference(cLine.getKeyToLeaseExternalReference());
            will(returnValue(Arrays.asList()));
            oneOf(mockMessageService).warnUser("Lease with external reference ABCD not found.");
        }});

        // when
        service.updateOrCreateItemAndTerm(cLine);

    }

    @Test
    public void create_item_and_term_when_charge_not_found() throws Exception {

        // given
        FastnetImportService service = new FastnetImportService();
        service.leaseRepository = mockLeaseRepository;
        service.chargeRepository = mockChargeRepository;
        service.messageService = mockMessageService;
        ChargingLine cLine = new ChargingLine();
        cLine.setKeyToLeaseExternalReference("ABCD");
        cLine.setKeyToChargeReference("SE123-4");

        // expect
        context.checking(new Expectations(){{
            oneOf(mockLeaseRepository).matchLeaseByExternalReference(cLine.getKeyToLeaseExternalReference());
            will(returnValue(Arrays.asList(new Lease())));
            oneOf(mockChargeRepository).findByReference(cLine.getKeyToChargeReference());
            will(returnValue(null));
            oneOf(mockMessageService).warnUser("Charge with reference SE123-4 not found for lease ABCD.");
        }});

        // when
        service.updateOrCreateItemAndTerm(cLine);

    }

    @Test
    public void update_lease_term_value_works_for_indexable() throws Exception {

        // given
        FastnetImportService service = new FastnetImportService();
        LeaseItem item = new LeaseItem();
        Charge charge = new Charge();
        charge.setReference("123-1");
        item.setCharge(charge);
        item.setType(LeaseItemType.RENT);
        LeaseTermForIndexable term = new LeaseTermForIndexable();
        BigDecimal amount = new BigDecimal("123.45");

        // when
        service.updateLeaseTermValue(item, amount, term);

        // then
        assertThat(term.getBaseValue()).isEqualTo(amount);
        assertThat(term.getSettledValue()).isEqualTo(amount);

        // and when
        LeaseTermForIndexable term2 = new LeaseTermForIndexable();
        charge.setReference("123-X");
        service.updateLeaseTermValue(item, amount, term2);

        // then
        assertThat(term2.getBaseValue()).isNull();
        assertThat(term2.getSettledValue()).isEqualTo(amount);

    }

    @Test
    public void update_lease_term_value_works_for_service_charge() throws Exception {

        // given
        FastnetImportService service = new FastnetImportService();
        LeaseItem item = new LeaseItem();
        item.setType(LeaseItemType.SERVICE_CHARGE);
        LeaseTermForServiceCharge term = new LeaseTermForServiceCharge();
        BigDecimal amount = new BigDecimal("123.45");

        // when
        service.updateLeaseTermValue(item, amount, term);

        // then
        assertThat(term.getBudgetedValue()).isEqualTo(amount);

    }

    @Test
    public void update_lease_term_value_works_for_fixed() throws Exception {

        // given
        FastnetImportService service = new FastnetImportService();
        LeaseItem item = new LeaseItem();
        item.setType(LeaseItemType.RENT_FIXED);
        LeaseTermForFixed term = new LeaseTermForFixed();
        BigDecimal amount = new BigDecimal("123.45");

        // when
        service.updateLeaseTermValue(item, amount, term);

        // then
        assertThat(term.getValue()).isEqualTo(amount);

    }

    @Test
    public void close_all_items_of_type_active_on_epoch_date_works() throws Exception {

        // given
        FastnetImportService service = new FastnetImportService();
        final LocalDate epochDateFastnetImport = service.EPOCH_DATE_FASTNET_IMPORT;

        LeaseItemType leaseItemType = LeaseItemType.RENT;
        Lease lease = new Lease();

        LeaseItem itemToBeClosed = new LeaseItem();
        itemToBeClosed.setSequence(BigInteger.valueOf(1));
        itemToBeClosed.setType(leaseItemType);
        itemToBeClosed.setEndDate(epochDateFastnetImport);
        lease.getItems().add(itemToBeClosed);

        LeaseItem itemNotToBeClosed = new LeaseItem();
        itemNotToBeClosed.setSequence(BigInteger.valueOf(2));
        itemNotToBeClosed.setType(leaseItemType);
        itemNotToBeClosed.setStartDate(epochDateFastnetImport);
        lease.getItems().add(itemNotToBeClosed);

        LeaseItem itemClosedInPast = new LeaseItem();
        itemClosedInPast.setSequence(BigInteger.valueOf(3));
        itemClosedInPast.setType(leaseItemType);
        itemClosedInPast.setEndDate(epochDateFastnetImport.minusDays(2));
        lease.getItems().add(itemClosedInPast);

        // when
        service.closeAllItemsOfTypeActiveOnEpochDate(lease, leaseItemType);

        // then
        assertThat(itemToBeClosed.getEndDate()).isEqualTo(epochDateFastnetImport.minusDays(1));
        assertThat(itemNotToBeClosed.getEndDate()).isNull();
        assertThat(itemClosedInPast.getEndDate()).isEqualTo(epochDateFastnetImport.minusDays(2));

    }

}