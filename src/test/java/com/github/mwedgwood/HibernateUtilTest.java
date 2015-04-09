package com.github.mwedgwood;

import com.github.mwedgwood.model.Address;
import com.github.mwedgwood.model.Person;
import org.hibernate.Hibernate;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class HibernateUtilTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateUtilTest.class);

    @Test
    public void testAuditPerson() throws Exception {
        Person initialPerson = Transactable.execute(session -> {
            Address address = new Address().setStreetName("1111 Nowhere Lane");
            session.save(address);

            Person newPerson = new Person()
                    .setName("Matt")
                    .setSurname("Wedgwood")
                    .setAddress(address);

            session.save(newPerson);
            return newPerson;
        });

        assertEquals("Matt", initialPerson.getName());
        assertEquals("1111 Nowhere Lane", initialPerson.getAddress().getStreetName());

        Person updatedPerson = Transactable.execute(session -> {
            session.update(initialPerson.setName("John"));
            session.update(initialPerson.getAddress().setStreetName("1700 Montgomery Street"));
            return initialPerson;
        });

        assertEquals("John", updatedPerson.getName());
        assertEquals("1700 Montgomery Street", updatedPerson.getAddress().getStreetName());

        Person versionOnePerson = Transactable.execute(session -> {
            AuditReader auditReader = AuditReaderFactory.get(session);
            Person v1person = auditReader.find(Person.class, initialPerson.getId(), 1);

            Hibernate.initialize(v1person.getAddress());
            return v1person;
        });

        Person currentPerson = findById(initialPerson.getId());

        assertEquals("Matt", versionOnePerson.getName());
        assertEquals("1111 Nowhere Lane", versionOnePerson.getAddress().getStreetName());

        assertEquals("John", currentPerson.getName());
        assertEquals("1700 Montgomery Street", currentPerson.getAddress().getStreetName());

        Transactable.execute(session -> {
            session.delete(currentPerson);
            return null;
        });

        List<Number> revisions = Transactable.execute(session -> {
            AuditReader auditReader = AuditReaderFactory.get(session);
            return auditReader.getRevisions(Person.class, initialPerson.getId());
        });

        Person deletedPerson = findById(initialPerson.getId());
        assertNull(deletedPerson);

        Person versionTwoPerson = Transactable.execute(session -> {
            AuditReader auditReader = AuditReaderFactory.get(session);
            return auditReader.find(Person.class, initialPerson.getId(), 2);
        });

        assertEquals("John", versionTwoPerson.getName());

        LOGGER.debug("Revisions: {}", revisions);
        assertEquals(3, revisions.size());

        Transactable.execute(session -> {
            AuditReader auditReader = AuditReaderFactory.get(session);
            revisions.stream().forEach(revision -> LOGGER.debug("Revision {} date: {}", revision, auditReader.getRevisionDate(revision)));
            return null;
        });
    }

    private Person findById(Integer id) {
        return Transactable.execute(session -> (Person) session.get(Person.class, id));
    }
}
