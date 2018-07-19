package org.estatio.module.capex.app.taskreminder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.linking.DeepLinkService;

import org.incode.module.communications.dom.impl.commchannel.CommunicationChannelRepository;
import org.incode.module.communications.dom.impl.commchannel.CommunicationChannelType;
import org.incode.module.communications.dom.impl.commchannel.EmailAddress;

import org.estatio.module.application.spiimpl.email.EmailServiceForEstatio;
import org.estatio.module.capex.dom.task.Task;
import org.estatio.module.capex.dom.task.TaskRepository;
import org.estatio.module.party.dom.Person;

@DomainService(
        nature = NatureOfService.DOMAIN,
        objectType = "org.estatio.module.capex.app.taskreminder.TaskReminderService"
)
public class TaskReminderService {

    @Programmatic
    private List<Person> getPersonsWithAssignedTasks() {
        return taskRepository.findTasksIncomplete().stream()
                .map(Task::getPersonAssignedTo)
                .distinct()
                .collect(Collectors.toList());
    }

    @Programmatic
    public List<TaskOverview> getTaskOverviews() {
        final List<Person> personsWithTask = getPersonsWithAssignedTasks();
        return personsWithTask.stream()
                .map(this::getTaskOverviewForPerson)
                .collect(Collectors.toList());
    }

    @Programmatic
    public TaskOverview getTaskOverviewForPerson(final Person person) {
        return new TaskOverview(person, taskRepository.findIncompleteByPersonAssignedTo(person), clockService, this);
    }

    @Programmatic
    public void sendReminder(final Person person, final List<Task> overdueTasks) {
        final EmailAddress address = (EmailAddress) communicationChannelRepository.findByOwnerAndType(person, CommunicationChannelType.EMAIL_ADDRESS).first();
        final String subject = String.format("You have %d overdue tasks in Estatio", overdueTasks.size());
        final String body = String.format("Dear %s,\n\nThis is a friendly reminder that you have %d overdue tasks in Estatio:\n<ul>", person.getName(), overdueTasks.size())
                + overdueTasks.stream()
                .map(task -> String.format("<li>%s</li>", deepLinkService.deepLinkFor(task)))
                .collect(Collectors.joining())
                + "</ul>";

        emailService.send(Collections.singletonList(address.getEmailAddress()), Collections.emptyList(), Collections.emptyList(), subject, body);
    }

    @Programmatic
    public String validateSendReminder(final Person person, final List<Task> overdueTasks) {
        if (communicationChannelRepository.findByOwnerAndType(person, CommunicationChannelType.EMAIL_ADDRESS).isEmpty()) {
            return String.format("No email address is known for %s", person.getName());
        }

        return overdueTasks.isEmpty() ? String.format("%s does not have any overdue tasks", person.getName()) : null;
    }

    @Inject
    private TaskRepository taskRepository;

    @Inject
    private CommunicationChannelRepository communicationChannelRepository;

    @Inject
    private ClockService clockService;

    @Inject
    private EmailServiceForEstatio emailService;

    @Inject
    private DeepLinkService deepLinkService;

}
