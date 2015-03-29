package com.github.mwedgwood;

import com.github.mwedgwood.model.Address;
import com.github.mwedgwood.model.Person;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HibernateUtilTest {

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
    }
}
