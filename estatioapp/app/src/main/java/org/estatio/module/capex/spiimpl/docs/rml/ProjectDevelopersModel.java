package org.estatio.module.capex.spiimpl.docs.rml;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import org.isisaddons.module.xdocreport.dom.service.XDocReportModel;

import lombok.Data;

@Data
public class ProjectDevelopersModel implements XDocReportModel {

    private final Project project;
    private final List<Developer> developers;

    @Override
    public Map<String, Data> getContextData() {
        return ImmutableMap.of(
                "project", Data.object(project),
                "developers", Data.list(developers, Developer.class));
    }

}
