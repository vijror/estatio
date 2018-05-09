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
        table = "DataSetRentRollLineEstatioLease",
        schema = "fastnet",
        extensions = {
                @Extension(vendorName = "datanucleus", key = "view-definition",
                        value = "CREATE VIEW \"fastnet\".\"DataSetRentRollLineEstatioLease\" " +
                                "( " +
                                "  {this.kontraktNr}, " +
                                "  {this.hyresgast}, " +
                                "  {this.kundNr}, " +
                                "  {this.leaseReference}, " +
                                "  {this.externalReference}, " +
                                "  {this.arshyra}, " +
                                "  {this.futureRentRollLine}, " +
                                "  {this.exportDate}, " +
                                "  {this.applied} " +
                                ") AS " +
                                "WITH l AS ( " +
                                "SELECT * FROM \"dbo\".\"Lease\" WHERE \"atPath\" LIKE \'/SWE%\' " +
                                " ) " +
                                "SELECT " +
                                "  rrl.\"kontraktNr\", " +
                                "  rrl.\"hyresgast\", " +
                                "  rrl.\"kundNr\", " +
                                "  a.\"reference\" AS \"leaseReference\", " +
                                "  l.\"externalReference\", " +
                                "  rrl.\"arshyra\", " +
                                "  rrl.\"futureRentRollLine\", " +
                                "  rrl.\"exportDate\", " +
                                "  rrl.\"applied\" " +
                                "FROM \"fastnet\".\"RentRollLine\" rrl " +
                                "  LEFT OUTER JOIN l " +
                                "    ON CONCAT(\'35\', l.\"externalReference\") = rrl.\"kontraktNr\" " +
                                "  LEFT OUTER JOIN \"dbo\".\"Agreement\" a " +
                                "    ON a.\"id\" = l.\"id\" ")
        })
@javax.jdo.annotations.Queries({
        @javax.jdo.annotations.Query(
                name = "findByExportDate", language = "JDOQL",
                value = "SELECT " +
                        "FROM org.estatio.module.fastnet.dom.DataSetRentRollLineEstatioLease " +
                        "WHERE exportDate == :exportDate "),
        @javax.jdo.annotations.Query(
                name = "nonMatchingRentRollLinesForExportDate", language = "JDOQL",
                value = "SELECT " +
                        "FROM org.estatio.module.fastnet.dom.DataSetRentRollLineEstatioLease " +
                        "WHERE exportDate == :exportDate "
                        + "&& kontraktNr != null "
                        + "&& futureRentRollLine == false "
                        + "&& leaseReference == null "),
        @javax.jdo.annotations.Query(
                name = "nonMatchingFutureRentRollLinesForExportDate", language = "JDOQL",
                value = "SELECT " +
                        "FROM org.estatio.module.fastnet.dom.DataSetRentRollLineEstatioLease " +
                        "WHERE exportDate == :exportDate "
                        + "&& kontraktNr != null "
                        + "&& futureRentRollLine == true "
                        + "&& leaseReference == null "),

})
@javax.jdo.annotations.Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
@ViewModel
@Getter @Setter
public class DataSetRentRollLineEstatioLease {

        private String kontraktNr;

        private String hyresgast;

        private String kundNr;

        private String leaseReference;

        private String externalReference;

        private BigDecimal arshyra;

        private boolean futureRentRollLine;

        private LocalDate exportDate;

        private LocalDate applied;

}
