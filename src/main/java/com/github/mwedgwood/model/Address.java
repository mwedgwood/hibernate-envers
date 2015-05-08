package com.github.mwedgwood.model;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.Set;

@Entity
@Audited
public class Address {


    private int id;
    private String streetName;
    private Integer houseNumber;
    private Integer flatNumber;

    private Set<Person> persons;

    @Id
    @GeneratedValue
    public int getId() {
        return id;
    }

    public Address setId(int id) {
        this.id = id;
        return this;
    }

    public String getStreetName() {
        return streetName;
    }

    public Address setStreetName(String streetName) {
        this.streetName = streetName;
        return this;
    }

    public Integer getHouseNumber() {
        return houseNumber;
    }

    public Address setHouseNumber(Integer houseNumber) {
        this.houseNumber = houseNumber;
        return this;
    }

    public Integer getFlatNumber() {
        return flatNumber;
    }

    public Address setFlatNumber(Integer flatNumber) {
        this.flatNumber = flatNumber;
        return this;
    }

    @OneToMany(mappedBy = "address")
    @Cascade(value = CascadeType.ALL)
    public Set<Person> getPersons() {
        return persons;
    }

    public Address setPersons(Set<Person> persons) {
        this.persons = persons;
        return this;
    }
}
