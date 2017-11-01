package org.estatio.capex.dom.invoice.manager;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.SemanticsOf;

import org.isisaddons.module.security.app.user.MeService;

@DomainObject(
        objectType = "org.estatio.capex.dom.invoice.manager.BarcodeManager",
        nature = Nature.VIEW_MODEL
)
public class BarcodeManager {

    @ActionLayout(named = "ECP France SAS")
    @Action(semantics = SemanticsOf.NON_IDEMPOTENT)
    public void ecpFranceSas() {

    }

    public boolean hideEcpFranceSas() {
        return doesNotHaveFrenchAtPath();
    }

    @ActionLayout(named = "ECP Taverny SARL")
    @Action(semantics = SemanticsOf.NON_IDEMPOTENT)
    public void ecpTavernySarl() {

    }

    public boolean hideEcpTavernySarl() {
        return doesNotHaveFrenchAtPath();
    }

    @ActionLayout(named = "ECP Caumartin SNC")
    @Action(semantics = SemanticsOf.NON_IDEMPOTENT)
    public void ecpCaumartinSnc() {

    }

    public boolean hideEcpCaumartinSnc() {
        return doesNotHaveFrenchAtPath();
    }

    @ActionLayout(named = "ECPNV French Branch")
    @Action(semantics = SemanticsOf.NON_IDEMPOTENT)
    public void ecpnvFrenchBranch() {

    }

    public boolean hideEcpnvFrenchBranch() {
        return doesNotHaveFrenchAtPath();
    }

    @ActionLayout(named = "SCI Val Commerces")
    @Action(semantics = SemanticsOf.NON_IDEMPOTENT)
    public void sciValCommerces() {

    }

    public boolean hideSciValCommerces() {
        return doesNotHaveFrenchAtPath();
    }

    @ActionLayout(named = "SCI Winter")
    @Action(semantics = SemanticsOf.NON_IDEMPOTENT)
    public void sciWinter() {

    }

    public boolean hideSciWinter() {
        return doesNotHaveFrenchAtPath();
    }

    @ActionLayout(named = "SCI Chasse Distribution")
    @Action(semantics = SemanticsOf.NON_IDEMPOTENT)
    public void sciChasseDistribution() {

    }

    public boolean hideSciChasseDistribution() {
        return doesNotHaveFrenchAtPath();
    }

    public boolean doesNotHaveFrenchAtPath() {
        return !meService.me().getAtPath().startsWith("/FRA");
    }

    @Inject
    private MeService meService;

}
