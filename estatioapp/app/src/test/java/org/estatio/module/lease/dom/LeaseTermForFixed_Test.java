package org.estatio.module.lease.dom;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LeaseTermForFixed_Test {

    public static class ValidateChangeValue extends LeaseTermForFixed_Test {

        private LeaseTermForFixed leaseTerm;
        LeaseItem leaseItem;

        @Before
        public void setUp() throws Exception {
        }

        private static LeaseTermForFixed newLeaseTerm(LeaseItemType leaseItemType) {
            LeaseItem leaseItem = new LeaseItem();
            LeaseTermForFixed leaseTerm = new LeaseTermForFixed();
            leaseTerm.setLeaseItem(leaseItem);
            leaseItem.setType(leaseItemType);
            return leaseTerm;
        }

        @Test
        public void whenDiscountAndNegative() {
            leaseTerm = newLeaseTerm(LeaseItemType.RENT_DISCOUNT_FIXED);
            assertThat(leaseTerm.validateChangeValue(BigDecimal.valueOf(-1))).isNull();
        }

        @Test
        public void whenDiscountAndZero() {
            leaseTerm = newLeaseTerm(LeaseItemType.RENT_DISCOUNT_FIXED);
            assertThat(leaseTerm.validateChangeValue(BigDecimal.ZERO)).isNull();
        }

        @Test
        public void whenNotDiscountAndPositive() {
            leaseTerm = newLeaseTerm(LeaseItemType.ENTRY_FEE);
            assertThat(leaseTerm.validateChangeValue(BigDecimal.valueOf(+1))).isNull();
        }

        @Test
        public void whenDiscountAndPositive() {
            leaseTerm = newLeaseTerm(LeaseItemType.RENT_DISCOUNT_FIXED);
            assertThat(leaseTerm.validateChangeValue(BigDecimal.valueOf(+1))).isEqualTo("Discount should be negative or zero");
        }

    }

    public static class Align extends LeaseTermForFixed_Test {

        @Test
        public void align_works_when_autocreate_is_true() throws Exception {

            // given
            LeaseTermForFixed term = new LeaseTermForFixed();
            LeaseItem leaseItem = new LeaseItem();
            leaseItem.setType(LeaseItemType.RENT_INDEX);
            term.setLeaseItem(leaseItem);

            LeaseTermForFixed previousTerm = new LeaseTermForFixed();
            previousTerm.setValue(new BigDecimal("100.00"));
            term.setPrevious(previousTerm);

            // when
            assertThat(term.getLeaseItem().getType().autoCreateTerms()).isTrue();
            assertThat(term.getValue()).isNull(); // only then the value will be set when aligning
            term.doAlign();

            // then
            assertThat(term.getValue()).isEqualTo(previousTerm.getValue());

            // and when
            term.setValue(new BigDecimal("12.34"));
            term.doAlign();
            // then
            assertThat(term.getValue()).isEqualTo(new BigDecimal("12.34"));

        }

        @Test
        public void align_works_when_autocreate_is_false() throws Exception {

            // given
            LeaseTermForFixed term = new LeaseTermForFixed();
            LeaseItem leaseItem = new LeaseItem();
            leaseItem.setType(LeaseItemType.RENT_FIXED);
            term.setLeaseItem(leaseItem);

            LeaseTermForFixed previousTerm = new LeaseTermForFixed();
            previousTerm.setValue(new BigDecimal("100.00"));
            term.setPrevious(previousTerm);

            // when
            assertThat(term.getLeaseItem().getType().autoCreateTerms()).isFalse();
            term.doAlign();

            // then
            assertThat(term.getValue()).isNull();

        }

    }

}