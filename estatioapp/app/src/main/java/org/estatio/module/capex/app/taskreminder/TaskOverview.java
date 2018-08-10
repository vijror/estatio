package org.estatio.module.capex.app.taskreminder;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.isis.applib.ViewModel;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService2;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.metamodel.MetaModelService5;

import org.estatio.module.capex.dom.task.Task;
import org.estatio.module.capex.dom.task.TaskRepository;
import org.estatio.module.party.dom.Person;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@DomainObject(
        nature = Nature.VIEW_MODEL,
        objectType = "org.estatio.module.capex.app.taskreminder.TaskOverview"
)
@NoArgsConstructor
public class TaskOverview implements ViewModel {

    public TaskOverview(
            final Person person) {
        this.person = person;
    }

    public String title() {
        return "Task Overview";
    }

    @Getter @Setter
    private Person person;

    @Property
    public long getTasksOverdue() {
        return taskRepository.findIncompleteByPersonAssignedTo(person).stream()
                .map(Task::getCreatedOn)
                .filter(ld -> ld.plusDays(5).isBefore(clockService.nowAsLocalDateTime()))
                .count();
    }

    @Property
    public long getTasksNotYetOverdue() {
        return taskRepository.findIncompleteByPersonAssignedTo(person).size() - getTasksOverdue();
    }

    @Collection
    @CollectionLayout(named = "Tasks Not Yet Overdue")
    public List<Task> getListOfTasksNotYetOverdue() {
        return taskRepository.findIncompleteByPersonAssignedTo(person).stream()
                .filter(t -> t.getCreatedOn().plusDays(5).isAfter(clockService.nowAsLocalDateTime()))
                .collect(Collectors.toList());
    }

    @Collection
    @CollectionLayout(named = "Tasks Overdue")
    public List<Task> getListOfTasksOverdue() {
        return taskRepository.findIncompleteByPersonAssignedTo(person).stream()
                .filter(t -> t.getCreatedOn().plusDays(5).isBefore(clockService.nowAsLocalDateTime()))
                .collect(Collectors.toList());
    }

    @Action(semantics = SemanticsOf.NON_IDEMPOTENT_ARE_YOU_SURE)
    public TaskOverview sendReminder() {
        taskReminderService.sendReminder(person, getListOfTasksOverdue());
        return this;
    }

    public String disableSendReminder() {
        return taskReminderService.disableSendReminder(person, getListOfTasksOverdue(), viewModelMemento());
    }

    @Inject
    private ClockService clockService;

    @Inject
    private TaskReminderService taskReminderService;

    @Inject
    private BookmarkService2 bookmarkService;

    @Inject
    private TaskRepository taskRepository;

    @Inject
    private MetaModelService5 metaModelService;

    /**
     * This is a bit hacky: in order to look up whether sendReminder has been invoked today, the viewmodel identifier is built up using the ID of the Person. This ensures that the identifier is always identical for a single person.
     * However, generating the memento based on the bookmark of the Person, we lose the ObjectState's code for a viewmodel ('*'), and thus the query for a command fails because the identifiers do not match.
     * Adding in the code manually resolves this.
     */
    @Override
    public String viewModelMemento() {
        return Bookmark.ObjectState.VIEW_MODEL.getCode() + bookmarkService.bookmarkFor(getPerson()).getIdentifier();
    }

    /**
     * Conversely for #viewModelMemento(): strip the viewmodel ObjectState code in order to successfully create the bookmark
     */
    @Override
    public void viewModelInit(final String memento) {
        Bookmark bookmark = new Bookmark(metaModelService.toObjectType(Person.class), memento.replace(Bookmark.ObjectState.VIEW_MODEL.getCode(), Bookmark.ObjectState.PERSISTENT.getCode()));
        setPerson(bookmarkService.lookup(bookmark, BookmarkService2.FieldResetPolicy.DONT_RESET, Person.class));
    }
}

