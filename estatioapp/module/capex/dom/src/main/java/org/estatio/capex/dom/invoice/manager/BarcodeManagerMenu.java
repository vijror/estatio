package org.estatio.capex.dom.invoice.manager;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.services.registry.ServiceRegistry2;

@DomainService(
        nature = NatureOfService.VIEW_MENU_ONLY,
        objectType = "incomingInvoice.BarcodeManagerMenu"
)
@DomainServiceLayout(
        menuBar = DomainServiceLayout.MenuBar.PRIMARY,
        named = "Invoices in",
        menuOrder = "99"
)
public class BarcodeManagerMenu {

    public BarcodeManager printBarcodes() {
        final BarcodeManager barcodeManager = new BarcodeManager();
        serviceRegistry2.injectServicesInto(barcodeManager);

        return barcodeManager;
    }

    @Inject
    ServiceRegistry2 serviceRegistry2;

}
