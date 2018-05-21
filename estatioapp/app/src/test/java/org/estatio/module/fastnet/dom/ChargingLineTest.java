package org.estatio.module.fastnet.dom;

import org.assertj.core.api.Assertions;
import org.joda.time.LocalDate;
import org.junit.Test;

public class ChargingLineTest {

    @Test
    public void keyToChargeReference() throws Exception {

        // given
        ChargingLine line = new ChargingLine();
        // when
        line.setKod("123");
        line.setKod2("4");
        // then
        Assertions.assertThat(line.keyToChargeReference()).isEqualTo("SE123-4");

    }

    @Test
    public void discarded_or_applied() throws Exception {

        // given
        ChargingLine line = new ChargingLine();
        // then
        Assertions.assertThat(line.discardedOrApplied()).isFalse();

        // and when
        line.setImportStatus(ImportStatus.DISCARDED);
        // then
        Assertions.assertThat(line.discardedOrApplied()).isTrue();

        // and when
        line.setImportStatus(ImportStatus.LEASE_ITEM_CREATED);
        // then
        Assertions.assertThat(line.discardedOrApplied()).isFalse();

        // and when
        line.setImportStatus(null);
        line.setApplied(new LocalDate());
        // then
        Assertions.assertThat(line.discardedOrApplied()).isTrue();

        // and when
        line.setImportStatus(ImportStatus.LEASE_ITEM_CREATED);
        line.setApplied(new LocalDate());
        // then
        Assertions.assertThat(line.discardedOrApplied()).isTrue();

        // and when
        line.setImportStatus(ImportStatus.DISCARDED);
        line.setApplied(new LocalDate());
        // then
        Assertions.assertThat(line.discardedOrApplied()).isTrue();

    }

    @Test
    public void apply_discarded_works() throws Exception {

        // given
        ChargingLine line = new ChargingLine();
        // when
        line.setApplied(new LocalDate());
        line.setImportStatus(ImportStatus.LEASE_ITEM_CREATED);
        // then
        Assertions.assertThat(line.apply()).isEqualTo(ImportStatus.LEASE_ITEM_CREATED);

    }

}