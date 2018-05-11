package org.estatio.module.fastnet.dom;

import java.math.BigDecimal;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;

import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.ViewModel;

import lombok.Getter;
import lombok.Setter;

// NOTE: this view also is maintained by fly db
@javax.jdo.annotations.PersistenceCapable(
        identityType = IdentityType.NONDURABLE,
        table = "FastNetRentRollOnLeaseDataLine",
        schema = "fastnet",
        extensions = {
                @Extension(vendorName = "datanucleus", key = "view-definition",
                        value = "CREATE VIEW \"fastnet\".\"FastNetRentRollOnLeaseDataLine\" " +
                                "( " +
                                "  {this.keyToLeaseExternalReference}, " +
                                "  {this.exportDate}, " +

                                "  {this.kontraktNr}, " +
                                "  {this.hyresgast}, " +
                                "  {this.kundNr}, " +
                                "  {this.arshyra}, " +
                                "  {this.futureRentRollLine}, " +
                                "  {this.applied}, " +

                                "  {this.leaseReference}, " +
                                "  {this.externalReference} " +
                                ") AS " +
                                "WITH leaseData AS ( " +
                                "SELECT " +
                                "l.\"externalReference\"" +
                                ", a.\"reference\" as \"leaseReference\"" +
                                " FROM \"dbo\".\"Lease\" l " +
                                " INNER JOIN \"dbo\".\"Agreement\" a " +
                                " ON a.\"id\" = l.\"id\" " +
                                "WHERE l.\"atPath\" LIKE \'/SWE%\' " +
                                " ) " +
                                "SELECT " +
                                " rrl.\"keyToLeaseExternalReference\"" +
                                ", rrl.\"exportDate\" " +
                                ", rrl.\"kontraktNr\"" +
                                ", rrl.\"hyresgast\"" +
                                ", rrl.\"kundNr\"" +
                                ", rrl.\"arshyra\"" +
                                ", rrl.\"futureRentRollLine\" " +
                                ", rrl.\"applied\" " +
                                ", ld.\"leaseReference\" " +
                                ", ld.\"externalReference\" " +
                                "FROM \"fastnet\".\"RentRollLine\" rrl " +
                                "  LEFT OUTER JOIN leaseData ld " +
                                "    ON ld.\"externalReference\" = rrl.\"keyToLeaseExternalReference\" ")
        })
@javax.jdo.annotations.Queries({
        @javax.jdo.annotations.Query(
                name = "findByExportDate", language = "JDOQL",
                value = "SELECT " +
                        "FROM org.estatio.module.fastnet.dom.FastNetRentRollOnLeaseDataLine " +
                        "WHERE exportDate == :exportDate "),
        @javax.jdo.annotations.Query(
                name = "nonMatchingRentRollLinesForExportDate", language = "JDOQL",
                value = "SELECT " +
                        "FROM org.estatio.module.fastnet.dom.FastNetRentRollOnLeaseDataLine " +
                        "WHERE exportDate == :exportDate "
                        + "&& kontraktNr != null "
                        + "&& futureRentRollLine == false "
                        + "&& leaseReference == null "),
        @javax.jdo.annotations.Query(
                name = "nonMatchingFutureRentRollLinesForExportDate", language = "JDOQL",
                value = "SELECT " +
                        "FROM org.estatio.module.fastnet.dom.FastNetRentRollOnLeaseDataLine " +
                        "WHERE exportDate == :exportDate "
                        + "&& kontraktNr != null "
                        + "&& futureRentRollLine == true "
                        + "&& leaseReference == null "),

})
@javax.jdo.annotations.Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
@ViewModel
@Getter @Setter
public class FastNetRentRollOnLeaseDataLine {

        // key 2
        private String keyToLeaseExternalReference;

        private LocalDate exportDate;

        // rent roll 6
        private String kontraktNr;

        private String hyresgast;

        private String kundNr;

        private BigDecimal arshyra;

        private boolean futureRentRollLine;

        private LocalDate applied;

        // lease 2
        private String leaseReference;

        private String externalReference;

}
