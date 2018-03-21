package org.estatio.module.capex.app.credittransfer;

import java.math.BigDecimal;

import javax.annotation.Nullable;

import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.ViewModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@ViewModel()
@Getter @Setter
@AllArgsConstructor
public class CreditTransferExportLine {

    // transfer information

    @MemberOrder(sequence = "0")
    private String debtorBankAccount;

    @MemberOrder(sequence = "0")
    private String paymentBatchCreatedOn;

    @MemberOrder(sequence = "1")
    private String paymentId;

    @MemberOrder(sequence = "2")
    private String sellerBankAccount;

    @MemberOrder(sequence = "3")
    private String sellerName;

    @MemberOrder(sequence = "4")
    private String sellerReference;

    @MemberOrder(sequence = "5")
    private BigDecimal paymentAmount;

    @MemberOrder(sequence = "6")
    private String currency;

    // invoice information

    @MemberOrder(sequence = "6")
    @Nullable
    private String invoiceNumber;

    @MemberOrder(sequence = "7")
    @Nullable
    private LocalDate invoiceDate;

    @MemberOrder(sequence = "8")
    private BigDecimal invoiceGrossAmount;

    @MemberOrder(sequence = "9")
    private String approvals;

    @MemberOrder(sequence = "10")
    private String invoiceDescriptionSummary;

    @MemberOrder(sequence = "11")
    private String invoiceDocumentName;

    @MemberOrder(sequence = "12")
    private String invoiceType;

    @MemberOrder(sequence = "13")
    private String chargeSummary;

    @MemberOrder(sequence = "14")
    @Nullable
    private String projectSummary;

    @MemberOrder(sequence = "15")
    @Nullable
    private String budgetSummary;

    @MemberOrder(sequence = "16")
    @Nullable
    private String propertySummary;

}
