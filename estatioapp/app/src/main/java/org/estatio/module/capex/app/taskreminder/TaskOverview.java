package org.estatio.module.capex.app.taskreminder;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.CommandReification;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.clock.ClockService;

import org.estatio.module.capex.dom.task.Task;
import org.estatio.module.party.dom.Person;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@XmlRootElement(name = "TaskOverview")
@XmlType(
        propOrder = {
                "person",
                "assignedTasks"
        }
)
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(objectType = "org.estatio.module.capex.app.taskreminder.TaskOverview")
@NoArgsConstructor
public class TaskOverview {

    public TaskOverview(
            final Person person,
            final List<Task> assignedTasks,
            final ClockService clockService,
            final TaskReminderService taskReminderService) {
        this.person = person;
        this.assignedTasks = assignedTasks;
        this.clockService = clockService;
        this.taskReminderService = taskReminderService;
    }

    @Getter @Setter
    private Person person;

    private List<Task> assignedTasks;

    @Property
    public long getTasksOverdue() {
        return assignedTasks.stream()
                .map(Task::getCreatedOn)
                .filter(ld -> ld.plusDays(5).isBefore(clockService.nowAsLocalDateTime()))
                .count();
    }

    @Property
    public long getTasksNotYetOverdue() {
        return assignedTasks.size() - getTasksOverdue();
    }

    @Collection
    @CollectionLayout(named = "Tasks Not Yet Overdue")
    public List<Task> getListOfTasksNotYetOverdue() {
        return assignedTasks.stream()
                .filter(t -> t.getCreatedOn().plusDays(5).isAfter(clockService.nowAsLocalDateTime()))
                .collect(Collectors.toList());
    }

    @Collection
    @CollectionLayout(named = "Tasks Overdue")
    public List<Task> getListOfTasksOverdue() {
        return assignedTasks.stream()
                .filter(t -> t.getCreatedOn().plusDays(5).isBefore(clockService.nowAsLocalDateTime()))
                .collect(Collectors.toList());
    }

    @Action(
            semantics = SemanticsOf.NON_IDEMPOTENT_ARE_YOU_SURE,
            command = CommandReification.ENABLED
    )
    public TaskOverview sendReminder() {
        taskReminderService.sendReminder(person, getListOfTasksOverdue());
        return this;
    }

    public String disableSendReminder() {
        return taskReminderService.validateSendReminder(person, getListOfTasksOverdue());
    }

    @Inject
    @XmlTransient
    private ClockService clockService;

    @Inject
    @XmlTransient
    private TaskReminderService taskReminderService;
}

