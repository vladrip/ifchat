package com.vladrip.ifchat.repository;

import com.vladrip.ifchat.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceRepository extends JpaRepository<Device, String> {

    List<Device> getAllByPersonUid(String uid);
}
