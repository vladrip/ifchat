package com.vladrip.ifchat.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.gson.Gson;
import com.vladrip.ifchat.dto.PersonDto;
import com.vladrip.ifchat.entity.Chat;
import com.vladrip.ifchat.entity.ChatMember;
import com.vladrip.ifchat.entity.Device;
import com.vladrip.ifchat.entity.Message;
import com.vladrip.ifchat.exception.ItemNotFoundException;
import com.vladrip.ifchat.mapping.Mapper;
import com.vladrip.ifchat.repository.ChatMemberRepository;
import com.vladrip.ifchat.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class FirebaseService {
    private static final Logger log = LoggerFactory.getLogger(FirebaseService.class);
    private final FirebaseMessaging firebaseMessaging;
    private final FirebaseAuth firebaseAuth;
    private final ChatService chatService;
    private final DeviceRepository deviceRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final Mapper mapper;
    private final Gson gson;

    public void sendFirebaseMessage(Message message) {
        try {
            String messageJson = gson.toJson(mapper.toMessageDto(message));
            Chat chat = message.getChat();
            Long chatId = chat.getId();
            String chatTypeJson = chat.getType().name();
            String notificationBody = message.getContent().replaceAll("\n", " ").trim();
            switch (chat.getType()) {
                case PRIVATE -> {
                    String personUid = message.getSender().getUid();
                    PersonDto otherPerson = chatService.getChat(chatId, personUid).getOtherPerson();
                    String otherPersonUid = otherPerson.getUid();

                    if (chatMemberRepository.getByChatIdAndPersonUid(chatId, otherPersonUid)
                            .orElseThrow(() -> new ItemNotFoundException(
                                    ChatMember.class, "chatId:%d, otherPersonUid:%s", chatId, otherPersonUid
                            ))
                            .getIsChatMuted())
                        return;

                    MulticastMessage firebaseMessage = MulticastMessage.builder()
                            .addAllTokens(deviceRepository.getAllByPersonUid(personUid).stream()
                                    .map(Device::getDeviceToken)
                                    .toList()
                            )
                            .putData("message", messageJson)
                            .putData("chatType", chatTypeJson)
                            .setNotification(Notification.builder()
                                    .setTitle(otherPerson.getFirstName().concat(" ").concat(otherPerson.getLastName()))
                                    .setBody(notificationBody)
                                    .build()
                            )
                            .build();
                    firebaseMessaging.sendMulticast(firebaseMessage);
                    //TODO detect and delete inactive tokens
                }

                case GROUP -> {
                    com.google.firebase.messaging.Message firebaseMessage = com.google.firebase.messaging.Message.builder()
                            .setTopic("g".concat(message.getChat().getId().toString()))
                            .putData("message", messageJson)
                            .putData("chatType", chatTypeJson)
                            .setNotification(Notification.builder()
                                    .setTitle(message.getChat().getName())
                                    .setBody(notificationBody)
                                    .build()
                            )
                            .build();
                    firebaseMessaging.send(firebaseMessage);
                }
            }
        } catch (Exception e) {
            log.error(e.toString());
            throw new RuntimeException(e);
        }
    }

    public String uidFromToken(String authToken) {
        if (authToken.contains("Bearer "))
            authToken = authToken.replace("Bearer ", "");
        try {
            return firebaseAuth.verifyIdToken(authToken).getUid();
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        }
    }

    public void unsubscribeUserFromChat(String uid, Chat.ChatType chatType, Long chatId) {
        if (chatType == Chat.ChatType.PRIVATE) return;
        try {
            firebaseMessaging.unsubscribeFromTopic(deviceRepository.getAllByPersonUid(uid).stream()
                    .map(Device::getDeviceToken)
                    .toList(),
                    "g" + chatId
            );
        } catch (FirebaseMessagingException e) {
            log.error(e.toString());
            throw new RuntimeException(e);
        }
    }
}
