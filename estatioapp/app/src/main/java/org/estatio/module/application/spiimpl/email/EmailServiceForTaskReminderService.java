package org.estatio.module.application.spiimpl.email;

import java.util.List;

import javax.activation.DataSource;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.Programmatic;

@DomainService(menuOrder = "99")
public class EmailServiceForTaskReminderService extends EmailServiceForEstatio implements EmailService2 {

    @Programmatic
    public boolean send(final List<String> to, final List<String> cc, final List<String> bcc, final String overrideEmailFromKey, final String overridePasswordFromKey, final String subject, final String body, final DataSource... attachments) {
        return delegate.send(to, cc, bcc, overrideEmailFromKey, overridePasswordFromKey, subject, body, attachments);
    }

}
