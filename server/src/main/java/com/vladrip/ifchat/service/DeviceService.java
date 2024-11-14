package com.vladrip.ifchat.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.vladrip.ifchat.entity.Chat;
import com.vladrip.ifchat.entity.Device;
import com.vladrip.ifchat.entity.Person;
import com.vladrip.ifchat.exception.ItemNotFoundException;
import com.vladrip.ifchat.repository.ChatMemberRepository;
import com.vladrip.ifchat.repository.DeviceRepository;
import com.vladrip.ifchat.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final PersonRepository personRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final FirebaseMessaging firebaseMessaging;

    public void createOrUpdate(String personUid, String deviceToken) {
        deviceRepository.findById(deviceToken)
                .ifPresentOrElse(this::update, () -> create(personUid, deviceToken));
    }

    private void update(Device device) {
        device.setTokenTimestamp(LocalDateTime.now());
        deviceRepository.save(device);
    }

    private void create(String personUid, String deviceToken) {
        Device device = new Device();
        Person person = personRepository.findById(personUid)
                .orElseThrow(() -> new ItemNotFoundException(Person.class, personUid));
        device.setPerson(person);
        device.setDeviceToken(deviceToken);
        device.setTokenTimestamp(LocalDateTime.now());

        List<String> deviceTokenList = List.of(deviceToken);
        chatMemberRepository.findAllByPersonUid(personUid).forEach(cm -> {
            Chat chat = cm.getChat();
            if (chat.getType() == Chat.ChatType.GROUP)
                firebaseMessaging
                        .subscribeToTopicAsync(deviceTokenList, "g".concat(chat.getId().toString()));
        });

        deviceRepository.save(device);
    }

    public void delete(String token) {
        deviceRepository.deleteById(token);
    }
}
