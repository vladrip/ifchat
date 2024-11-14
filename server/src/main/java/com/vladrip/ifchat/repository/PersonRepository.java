package com.vladrip.ifchat.repository;

import com.vladrip.ifchat.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, String> {

}