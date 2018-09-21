package org.estatio.module.capex.fixtures.document.enums;

import java.util.List;

import org.apache.isis.applib.fixturescripts.PersonaWithBuilderScript;
import org.apache.isis.applib.fixturescripts.PersonaWithFinder;
import org.apache.isis.applib.services.registry.ServiceRegistry2;

import org.incode.module.document.dom.impl.docs.Document;

import org.estatio.module.capex.dom.documents.IncomingDocumentRepository;
import org.estatio.module.capex.fixtures.document.builders.IncomingGenericPdfBuilder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Getter
@Accessors(chain = true)
public enum IncomingGenericPdf_enum implements PersonaWithBuilderScript<Document, IncomingGenericPdfBuilder>, PersonaWithFinder<Document> {

    FakeTaxReceiptGbr(IncomingGenericPdf_enum.class, "fakeTaxReceiptGbr.pdf", "TAX_REGISTER", false, "/GBR");
    ;

    private final Class<?> contextClass;
    private final String resourceName;
    private final String documentType;
    private final boolean barcodeInDocName;
    private final String atPath;

    @Override
    public IncomingGenericPdfBuilder builder() {
        return new IncomingGenericPdfBuilder()
                .setContextClass(contextClass)
                .setResourceName(resourceName)
                .setDocumentType(documentType)
                .setBarcodeInDocName(barcodeInDocName)
                .setAtPath(atPath);
    }

    @Override
    public Document findUsing(final ServiceRegistry2 serviceRegistry) {

        final IncomingDocumentRepository incomingDocumentRepository = serviceRegistry
                .lookupService(IncomingDocumentRepository.class);
        final List<Document> documents = incomingDocumentRepository.matchAllIncomingDocumentsByName(resourceName);
        return documents.isEmpty() ? null : documents.get(0);
    }
}
