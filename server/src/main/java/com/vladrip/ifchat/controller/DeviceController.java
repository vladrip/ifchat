package com.vladrip.ifchat.controller;

import com.vladrip.ifchat.dto.StringWrapperDto;
import com.vladrip.ifchat.service.DeviceService;
import com.vladrip.ifchat.service.FirebaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {
    private final DeviceService deviceService;
    private final FirebaseService firebaseService;

    @PostMapping
    public void createOrUpdate(@RequestHeader(name = "Authorization") String authToken,
                               @RequestBody StringWrapperDto deviceToken) {
        deviceService.createOrUpdate(firebaseService.uidFromToken(authToken), deviceToken.getValue());
    }

    @DeleteMapping("/{deviceToken}")
    public void delete(@PathVariable String deviceToken) {
        deviceService.delete(deviceToken);
    }
}
