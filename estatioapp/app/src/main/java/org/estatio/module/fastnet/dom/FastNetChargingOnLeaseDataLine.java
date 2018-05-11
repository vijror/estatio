package org.estatio.module.fastnet.dom;

import java.math.BigDecimal;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;

import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.ViewModel;

import lombok.Getter;
import lombok.Setter;

/*
WITH
leaseData AS (
	SELECT

	l.externalReference
	, ch.reference as chargeReference

	, li.type as leaseItemType
	, li.startDate as leaseItemStartDate
	, li.endDate as leaseItemEndDate
	, li.invoicingFrequency
	, li.status as leaseItemStatus

	, a.reference as leaseReference
	, l.status as leaseStatus
	, a.startDate as leaseStartDate
	, a.endDate as leaseEndDate
	, l.tenancyStartDate
	, l.tenancyEndDate
	, p.name as tenantName
	, p.reference as tenantReference

	, lt.startDate as leaseTermStartDate
	, lt.endDate as leaseTermEndDate
	, lt.status as leaseTermStatus
	, lt.baseValue
	, lt.settledValue
	, lt.overrideTaxValue
	, lt.value
	FROM dbo.LeaseItem li
	INNER JOIN dbo.Lease l ON l.id = li.leaseId
	INNER JOIN dbo.Agreement a ON a.id = l.id
	INNER JOIN dbo.Charge ch ON ch.id = li.chargeId
	INNER JOIN dbo.LeaseTerm lt ON lt.leaseItemId = li.id
	LEFT OUTER JOIN (SELECT * FROM dbo.AgreementRole WHERE typeId = 6 AND endDate is null) AS agr ON agr.agreementId = a.id
	LEFT OUTER JOIN dbo.Party p ON p.id = agr.partyId
	WHERE li.atPath LIKE '/SWE%'
)
SELECT
-- keys
	cl.keyToLeaseExternalReference
, cl.keyToChargeReference
, cl.exportDate
-- charging
, cl.kontraktNr
, cl.kundNr
, cl.kod
, cl.kod2
, cl.kontText
, cl.kontText2
, cl.fromDat
, cl.tomDat
, cl.debPer
, cl.firstPosStart
, cl.arsBel
, cl.applied
-- lease
, ld.leaseReference
, ld.externalReference
, ld.tenantName
, ld.tenantReference
, ld.leaseStatus
, ld.tenancyStartDate
, ld.tenancyEndDate
, ld.leaseStartDate
, ld.leaseEndDate
-- lease item
, ld.leaseItemType
, ld.invoicingFrequency
, ld.leaseItemStartDate
, ld.leaseItemEndDate
, ld.chargeReference
-- lease term
, ld.leaseTermStartDate
, ld.leaseTermEndDate
, ld.leaseTermStatus
, ld.baseValue
, ld.settledValue
, ld.value
FROM fastnet.ChargingLine cl
LEFT OUTER JOIN leaseData ld
ON ld.externalReference = cl.keyToLeaseExternalReference
AND ld.chargeReference = cl.keyToChargeReference

WHERE cl.exportDate = '2018-04-20'
*/
// NOTE: this view also is maintained by fly db

@javax.jdo.annotations.PersistenceCapable(
        identityType = IdentityType.NONDURABLE,
        table = "FastNetChargingOnLeaseDataLine",
        schema = "fastnet",
        extensions = {
                @Extension(vendorName = "datanucleus", key = "view-definition",
                        value = "CREATE VIEW \"fastnet\".\"FastNetChargingOnLeaseDataLine\" " +
                                "( " +
                                "  {this.keyToLeaseExternalReference}, " +
                                "  {this.keyToChargeReference}, " +
                                "  {this.exportDate}, " +

                                "  {this.kontraktNr}, " +
                                "  {this.kundNr}, " +
                                "  {this.kod}, " +
                                "  {this.kod2}, " +
                                "  {this.kontText}, " +
                                "  {this.kontText2}, " +
                                "  {this.fromDat}, " +
                                "  {this.tomDat}, " +
                                "  {this.debPer}, " +
                                "  {this.firstPosStart}, " +
                                "  {this.arsBel}, " +
                                "  {this.applied}, " +

                                "  {this.leaseReference}, " +
                                "  {this.externalReference}, " +
                                "  {this.tenantName}, " +
                                "  {this.tenantReference}, " +
                                "  {this.leaseStatus}, " +
                                "  {this.tenancyStartDate}, " +
                                "  {this.tenancyEndDate}, " +
                                "  {this.leaseStartDate}, " +
                                "  {this.leaseEndDate}, " +

                                "  {this.leaseItemType}, " +
                                "  {this.invoicingFrequency}, " +
                                "  {this.leaseItemStartDate}, " +
                                "  {this.leaseItemEndDate}, " +
                                "  {this.chargeReference}, " +

                                "  {this.leaseTermStartDate}, " +
                                "  {this.leaseTermEndDate}, " +
                                "  {this.leaseTermStatus}, " +
                                "  {this.baseValue}, " +
                                "  {this.settledValue}, " +
                                "  {this.value} " +
                                ") AS " +

                                "WITH leaseData AS ( " +
                                "SELECT " +

                                "l. \"externalReference\"" +
                                ", ch.\"reference\" as \"chargeReference\"" +

                                ", li.\"type\" as \"leaseItemType\"" +
                                ", li.\"startDate\" as \"leaseItemStartDate\"" +
                                ", li.\"endDate\" as \"leaseItemEndDate\"" +
                                ", li.\"invoicingFrequency\"" +
                                ", li.\"status\" as \"leaseItemStatus\"" +

                                ", a.\"reference\" as \"leaseReference\"" +
                                ", l.\"status\" as \"leaseStatus\"" +
                                ", a.\"startDate\" as \"leaseStartDate\"" +
                                ", a.\"endDate\" as \"leaseEndDate\"" +
                                ", l.\"tenancyStartDate\"" +
                                ", l.\"tenancyEndDate\"" +
                                ", p.\"name\" as \"tenantName\"" +
                                ", p.\"reference\" as \"tenantReference\"" +

                                ", lt.\"startDate\" as \"leaseTermStartDate\"" +
                                ", lt.\"endDate\" as \"leaseTermEndDate\"" +
                                ", lt.\"status\" as \"leaseTermStatus\"" +
                                ", lt.\"baseValue\"" +
                                ", lt.\"settledValue\"" +
                                ", lt.\"value\"" +

                                "FROM \"dbo\".\"LeaseItem\" li " +
                                "INNER JOIN \"dbo\".\"Lease\" l ON l.\"id\" = li.\"leaseId\" " +
                                "INNER JOIN \"dbo\".\"Agreement\" a ON a.\"id\" = l.\"id\" " +
                                "INNER JOIN \"dbo\".\"Charge\" ch ON ch.\"id\" = li.\"chargeId\" " +
                                "INNER JOIN \"dbo\".\"LeaseTerm\" lt ON lt.\"leaseItemId\" = li.\"id\" " +
                                "LEFT OUTER JOIN (SELECT * FROM \"dbo\".\"AgreementRole\" WHERE \"typeId\" = 6 AND \"endDate\" is null) AS agr ON agr.\"agreementId\" = a.\"id\" " +
                                "LEFT OUTER JOIN \"dbo\".\"Party\" p ON p.\"id\" = agr.\"partyId\" " +
                                "WHERE li.\"atPath\" LIKE \'/SWE%\' " +
                                " ) " +

                                "SELECT " +
                                // keys
                                "  cl.\"keyToLeaseExternalReference\"" +
                                ", cl.\"keyToChargeReference\"" +
                                ", cl.\"exportDate\"" +
                                // charging
                                ", cl.\"kontraktNr\"" +
                                ", cl.\"kundNr\"" +
                                ", cl.\"kod\"" +
                                ", cl.\"kod2\"" +
                                ", cl.\"kontText\"" +
                                ", cl.\"kontText2\"" +
                                ", cl.\"fromDat\"" +
                                ", cl.\"tomDat\"" +
                                ", cl.\"debPer\"" +
                                ", cl.\"firstPosStart\"" +
                                ", cl.\"arsBel\"" +
                                ", cl.\"applied\"" +
                                // lease
                                ", ISNUll(ld.\"leaseReference\", a2.\"reference\") as \"leaseReference\"" +
                                ", ISNUll(ld.\"externalReference\", l2.\"externalReference\") as \"externalReference\"" +
                                ", ISNUll(ld.\"tenantName\", p2.\"name\") as \"tenantName\"" +
                                ", ISNUll(ld.\"tenantReference\", p2.\"reference\") as \"tenantReference\"" +
                                ", ISNUll(ld.\"leaseStatus\", l2.\"status\") as \"leaseStatus\"" +
                                ", ISNUll(ld.\"tenancyStartDate\", l2.\"tenancyStartDate\") as \"tenancyStartDate\"" +
                                ", ISNUll(ld.\"tenancyEndDate\", l2.\"tenancyEndDate\") as \"tenancyEndDate\"" +
                                ", ISNUll(ld.\"leaseStartDate\", a2.\"startDate\") as \"leaseStartDate\"" +
                                ", ISNUll(ld.\"leaseEndDate\", a2.\"endDate\") as \"leaseEndDate\"" +
                                // lease item
                                ", ld.\"leaseItemType\"" +
                                ", ld.\"invoicingFrequency\"" +
                                ", ld.\"leaseItemStartDate\"" +
                                ", ld.\"leaseItemEndDate\"" +
                                ", ld.\"chargeReference\"" +
                                // lease term
                                ", ld.\"leaseTermStartDate\"" +
                                ", ld.\"leaseTermEndDate\"" +
                                ", ld.\"leaseTermStatus\"" +
                                ", ld.\"baseValue\"" +
                                ", ld.\"settledValue\"" +
                                ", ld.\"value\"" +
                                "FROM \"fastnet\".\"ChargingLine\" cl " +
                                "  LEFT OUTER JOIN leaseData ld " +
                                "  ON ld.\"externalReference\" = cl.\"keyToLeaseExternalReference\" " +
                                "  AND ld.\"chargeReference\" = cl.\"keyToChargeReference\" " +
                                "  LEFT OUTER JOIN \"dbo\".\"Lease\" l2 " +
                                "  ON l2.\"externalReference\" = cl.\"keyToLeaseExternalReference\" " +
                                "  LEFT OUTER JOIN \"dbo\".\"Agreement\" a2 " +
                                "  ON l2.\"id\" = a2.\"id\"" +
                                "  LEFT OUTER JOIN (SELECT * FROM \"dbo\".\"AgreementRole\" WHERE \"typeId\" = 6 AND \"endDate\" is null) AS agr2 " +
                                "  ON agr2.\"agreementId\" = a2.\"id\" " +
                                "  LEFT OUTER JOIN \"dbo\".\"Party\" p2 " +
                                "  ON p2.\"id\" = agr2.\"partyId\" "
                )
        })
@javax.jdo.annotations.Queries({
        @javax.jdo.annotations.Query(
                name = "findByExportDate", language = "JDOQL",
                value = "SELECT " +
                        "FROM org.estatio.module.fastnet.dom.FastNetChargingOnLeaseDataLine " +
                        "WHERE exportDate == :exportDate "),

})
@javax.jdo.annotations.Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
@ViewModel
@Getter @Setter
public class FastNetChargingOnLeaseDataLine {

        // keys 3
        private String keyToLeaseExternalReference;

        private String keyToChargeReference;

        private LocalDate exportDate;

        // charging 12

        private String kontraktNr;

        private String kundNr;

        private String kod;

        private String kod2;

        private String kontText;

        private String kontText2;

        private String fromDat;

        private String tomDat;

        private String debPer;

        private String firstPosStart;

        private BigDecimal arsBel;

        private LocalDate applied;

        // lease 9

        private String leaseReference;

        private String externalReference;

        private String tenantName;

        private String tenantReference;

        private String leaseStatus;

        private LocalDate tenancyStartDate;

        private LocalDate tenancyEndDate;

        private LocalDate leaseStartDate;

        private LocalDate leaseEndDate;

        // lease item 5

        private String leaseItemType;

        private String invoicingFrequency;

        private LocalDate leaseItemStartDate;

        private LocalDate leaseItemEndDate;

        private String chargeReference;

        // lease term 6

        private LocalDate leaseTermStartDate;

        private LocalDate leaseTermEndDate;

        private String leaseTermStatus;

        private BigDecimal baseValue;

        private BigDecimal settledValue;

        private BigDecimal value;

}
