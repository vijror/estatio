package org.estatio.module.fastnet.dom;

import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.joda.time.LocalDate;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
import org.estatio.module.lease.dom.LeaseTermForIndexable;

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

    @Mock
    Lease mockLease;

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

}