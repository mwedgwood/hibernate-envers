package com.github.mwedgwood;

import com.github.mwedgwood.model.Address;
import com.github.mwedgwood.model.Person;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class HibernateUtilTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateUtilTest.class);

    @Test
    public void testAuditPerson() throws Exception {
        Person person = Transactable.execute(session -> {
            Address address = new Address()
                    .setStreetName("1111 Nowhere Lane");

            session.save(address);

            Person newPerson = new Person()
                    .setName("Matt")
                    .setSurname("Wedgwood")
                    .setAddress(address);

            session.save(newPerson);
            return newPerson;
        });

        assertEquals("Matt", person.getName());

        Person updatedPerson = Transactable.execute(session -> {
            session.update(person.setName("John"));
            return person;
        });

        assertEquals("John", updatedPerson.getName());

        Person previousPerson = Transactable.execute(session -> {
            AuditReader auditReader = AuditReaderFactory.get(session);
            return auditReader.find(Person.class, person.getId(), 1);
        });

        assertEquals("Matt", previousPerson.getName());

        List<Number> revisions = Transactable.execute(session -> {
            AuditReader auditReader = AuditReaderFactory.get(session);
            return auditReader.getRevisions(Person.class, person.getId());
        });

        assertEquals(2, revisions.size());
        LOGGER.debug("Revisions: {}", revisions);

        Transactable.execute(session -> {
            AuditReader auditReader = AuditReaderFactory.get(session);
            revisions.stream().forEach(revision -> LOGGER.debug("Revision {} date: {}", revision, auditReader.getRevisionDate(revision)));
            return null;
        });
    }
}
