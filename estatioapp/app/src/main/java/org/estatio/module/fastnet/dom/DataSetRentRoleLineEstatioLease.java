package org.estatio.module.fastnet.dom;

import java.math.BigDecimal;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;

import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.ViewModel;

import lombok.Getter;
import lombok.Setter;

//SELECT rrl.kontraktNr, rrl.hyresgast, a.reference as leaseReference, rrl.arshyra, rrl.futureRentRollLine, rrl.exportDate, *
//        FROM fastnet.RentRollLine rrl
//        LEFT OUTER JOIN dbo.Lease l ON CONCAT('35', l.externalReference) = rrl.kontraktNr
//        LEFT OUTER JOIN dbo.Agreement a ON a.id = l.id
//        WHERE NOT kontraktNr is null
//        AND a.reference is null
//        AND rrl.futureRentRollLine = 0 -- filter future rent role lines
//        AND rrl.applied is null -- filter applied lines (should be noop)
//        AND rrl.exportDate = '2018-04-20'

@javax.jdo.annotations.PersistenceCapable(
        identityType = IdentityType.NONDURABLE,
        table = "DataSetRentRoleLineEstatioLease",
        schema = "fastnet",
        extensions = {
                @Extension(vendorName = "datanucleus", key = "view-definition",
                        value = "CREATE VIEW \"fastnet\".\"DataSetRentRoleLineEstatioLease\" " +
                                "( " +
                                "  {this.kontraktNr}, " +
                                "  {this.hyresgast}, " +
                                "  {this.leaseReference}, " +
                                "  {this.arshyra}, " +
                                "  {this.futureRentRollLine}, " +
                                "  {this.exportDate} " +
                                ") AS " +
                                "SELECT " +
                                "  rrl.\"kontraktNr\", " +
                                "  rrl.\"hyresgast\" , " +
                                "  a.\"reference\" AS \"leaseReference\", " +
                                "  rrl.\"arshyra\", " +
                                "  rrl.\"futureRentRollLine\", " +
                                "  rrl.\"exportDate\" " +
                                "FROM \"fastnet\".\"RentRollLine\" rrl " +
                                "  LEFT OUTER JOIN \"dbo\".\"Lease\" l " +
                                "    ON CONCAT(\'35\', l.\"externalReference\") = rrl.\"kontraktNr\" " +
                                "  LEFT OUTER JOIN \"dbo\".\"Agreement\" a " +
                                "    ON a.\"id\" = l.\"id\"" )
        })
@javax.jdo.annotations.Queries({
        @javax.jdo.annotations.Query(
                name = "findByExportDate", language = "JDOQL",
                value = "SELECT " +
                        "FROM org.estatio.module.fastnet.dom.DataSetRentRoleLineEstatioLease " +
                        "WHERE exportDate == :exportDate "),
        @javax.jdo.annotations.Query(
                name = "estatioLeasesNotFoundByExportDate", language = "JDOQL",
                value = "SELECT " +
                        "FROM org.estatio.module.fastnet.dom.DataSetRentRoleLineEstatioLease " +
                        "WHERE exportDate == :exportDate "
                        + "&& futureRentRollLine == false "
                        + "&& leaseReference == null "),
        @javax.jdo.annotations.Query(
                name = "futureLeasesNotFoundByExportDate", language = "JDOQL",
                value = "SELECT " +
                        "FROM org.estatio.module.fastnet.dom.DataSetRentRoleLineEstatioLease " +
                        "WHERE exportDate == :exportDate "
                        + "&& futureRentRollLine == true "
                        + "&& leaseReference == null "),
})
@javax.jdo.annotations.Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
@ViewModel
@Getter @Setter
public class DataSetRentRoleLineEstatioLease {

        private String kontraktNr;

        private String hyresgast;

        private String leaseReference;

        private BigDecimal arshyra;

        private boolean futureRentRollLine;

        private LocalDate exportDate;

}
