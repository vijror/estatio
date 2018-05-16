package org.estatio.module.fastnet.dom;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.ViewModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ViewModel
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class FastNetRentRollOnLeaseDataLine2 {

        // key 2
        private String keyToLeaseExternalReference;

        private LocalDate exportDate;

        // rent roll 8
        private String kontraktNr;

        private String hyresgast;

        private String kundNr;

        private BigDecimal arshyra;

        private String kontraktFrom;

        private String kontraktTom;

        private boolean futureRentRollLine;

        private LocalDate applied;

        // lease 4
        private String leaseReference;

        private String externalReference;

        private LocalDate leaseStartDate;

        private LocalDate leaseEndDate;

}
