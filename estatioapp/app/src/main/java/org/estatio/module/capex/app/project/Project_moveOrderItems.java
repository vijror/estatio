package org.estatio.module.capex.app.project;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Mixin;

import org.estatio.module.capex.dom.project.Project;

@Mixin
public class Project_moveOrderItems {

    private final Project project;

    public Project_moveOrderItems(Project project) {
        this.project = project;
    }

    @Action()
    public ProjectOrderItemTransferManager $$(final Project target) {
        return new ProjectOrderItemTransferManager(target, project);
    }

}
