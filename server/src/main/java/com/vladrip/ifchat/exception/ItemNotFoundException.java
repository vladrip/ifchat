package com.vladrip.ifchat.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Entity not found")
public class ItemNotFoundException extends RuntimeException {
    private static final Logger log = LoggerFactory.getLogger("application");
    private static final String DEFAULT_MSG = "Entity not found: ";

    public ItemNotFoundException() {
        super(DEFAULT_MSG);
    }

    public ItemNotFoundException(String message) {
        super(message);
        log.error("ItemNotFoundException: " + message);
    }

    public ItemNotFoundException(Class<?> entityClass, Long entityId) {
        this(DEFAULT_MSG + String.format("%s id:%d", entityClass.getSimpleName(), entityId));
    }

    public ItemNotFoundException(Class<?> entityClass, String format, Object... args) {
        this(DEFAULT_MSG.concat(entityClass.getSimpleName()).concat(" ").concat(String.format(format, args)));
    }
}
