package org.estatio.module.fastnet.dom;

import org.assertj.core.api.Assertions;
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

}