package org.estatio.module.capex.fixtures.document.builders;

import java.io.IOException;
import java.net.URL;

import javax.inject.Inject;

import com.google.common.io.Resources;

import org.apache.isis.applib.fixturescripts.BuilderScriptAbstract;
import org.apache.isis.applib.services.sudo.SudoService;
import org.apache.isis.applib.value.Blob;

import org.incode.module.document.dom.impl.docs.Document;

import org.estatio.module.capex.app.DocumentMenu;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@EqualsAndHashCode(of={"contextClass", "resourceName"}, callSuper = false)
@ToString(of={"contextClass", "resourceName"})
@Accessors(chain = true)
public final class IncomingGenericPdfBuilder
        extends BuilderScriptAbstract<Document, IncomingGenericPdfBuilder> {

    @Getter @Setter
    private Class<?> contextClass;
    @Getter @Setter
    private String resourceName;
    @Getter @Setter
    private boolean barcodeInDocName;
    @Getter @Setter
    private String documentType;
    @Getter @Setter
    private String atPath;
    @Getter @Setter
    private String runAs;

    @Getter
    private Document object;

    @Override
    protected void execute(final ExecutionContext executionContext) {

        checkParam("contextClass", executionContext, Class.class);
        checkParam("resourceName", executionContext, String.class);
        checkParam("documentType", executionContext, String.class);
        checkParam("barcodeInDocName", executionContext, boolean.class);
        checkParam("atPath", executionContext, String.class);
        final String runAsParam = executionContext.getParameter("runAs");

        String runAs = runAsParam != null
                        ? runAsParam
                        : this.runAs;   // could still be null; that's ok

        final URL url = Resources.getResource(contextClass, resourceName);
        byte[] bytes;
        try {
            bytes = Resources.toByteArray(url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final Blob blob = new Blob(resourceName, "application/pdf", bytes);
        object = runAs != null
                        ? sudoService.sudo(runAs, () -> upload(blob))
                        : upload(blob);

    }

    private Document upload(final Blob blob) {
        return documentMenu.uploadGeneric(blob, getDocumentType(), isBarcodeInDocName(), getAtPath());
    }

    @Inject
    DocumentMenu documentMenu;
    @Inject
    SudoService sudoService;

}
