package org.estatio.module.capex.app.taskreminder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService2;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.email.EmailService;
import org.apache.isis.applib.services.linking.DeepLinkService;
import org.apache.isis.applib.services.metamodel.MetaModelService5;
import org.apache.isis.applib.services.registry.ServiceRegistry2;

import org.isisaddons.module.command.dom.CommandServiceJdoRepository;

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

    public static final String OVERRIDE_FROM_EMAIL = "isis.service.email.override.sender.address.task";
    public static final String OVERRIDE_FROM_PASSWORD = "isis.service.email.override.sender.password.task";

    @Programmatic
    private List<Person> getPersonsWithAssignedTasks() {
        return taskRepository.findTasksIncomplete().stream()
                .filter(task -> task.getPersonAssignedTo() != null)
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
        return serviceRegistry.injectServicesInto(new TaskOverview(person));
    }

    @Programmatic
    public void sendReminder(final Person person, final List<Task> overdueTasks) {
        final EmailAddress address = (EmailAddress) communicationChannelRepository.findByOwnerAndType(person, CommunicationChannelType.EMAIL_ADDRESS).first();
        final String subject = overdueTasks.size() == 1 ? String.format("You have %d overdue task in Estatio", overdueTasks.size()) : String.format("You have %d overdue tasks in Estatio", overdueTasks.size());
        final String body = String.format("Dear %s,\n\nThis is a friendly reminder that you have %d overdue task(s) in Estatio:\n<ul>", person.getName(), overdueTasks.size())
                + overdueTasks.stream()
                .map(task -> String.format("<li>%s</li>", deepLinkService.deepLinkFor(task)))
                .collect(Collectors.joining())
                + "</ul>";

        emailService.send(Collections.singletonList(address.getEmailAddress()), Collections.emptyList(), Collections.emptyList(), OVERRIDE_FROM_EMAIL, OVERRIDE_FROM_PASSWORD, subject, body);
    }

    @Programmatic
    public String disableSendReminder(final Person person, final List<Task> overdueTasks, final String memento) {
        if (communicationChannelRepository.findByOwnerAndType(person, CommunicationChannelType.EMAIL_ADDRESS).isEmpty()) {
            return String.format("No email address is known for %s", person.getName());
        }

        Bookmark bookmark = new Bookmark(metaModelService.toObjectType(TaskOverview.class), memento);
        if (!commandRepository.findByTargetAndFromAndTo(bookmark, clockService.now(), clockService.now()).isEmpty()) {
            return String.format("A reminder has been sent to %s today already", person.getName());
        }

        return overdueTasks.isEmpty() ? String.format("%s does not have any overdue tasks", person.getName()) : null;
    }

    @Inject
    private TaskRepository taskRepository;

    @Inject
    CommunicationChannelRepository communicationChannelRepository;

    @Inject
    private ClockService clockService;

    // Does not inject interface emailService, since we expanded Estatio's impl with an override for the from address
    @Inject
    EmailServiceForEstatio emailService;

    @Inject
    DeepLinkService deepLinkService;

    @Inject
    private ServiceRegistry2 serviceRegistry;

    @Inject
    private BookmarkService2 bookmarkService;

    @Inject
    private CommandServiceJdoRepository commandRepository;

    @Inject
    private MetaModelService5 metaModelService;

}
