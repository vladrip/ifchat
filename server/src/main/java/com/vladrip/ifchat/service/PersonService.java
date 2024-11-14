package com.vladrip.ifchat.service;

import com.vladrip.ifchat.dto.PersonDto;
import com.vladrip.ifchat.entity.Person;
import com.vladrip.ifchat.exception.ItemNotFoundException;
import com.vladrip.ifchat.mapping.Mapper;
import com.vladrip.ifchat.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PersonService {
    private final PersonRepository personRepository;
    private final Mapper mapper;

    public PersonDto get(String uid) {
        Person person = personRepository.findById(uid)
                .orElseThrow(() -> new ItemNotFoundException(Person.class, uid));
        return mapper.toPersonDto(person);
    }
}
