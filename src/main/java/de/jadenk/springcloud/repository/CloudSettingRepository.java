package de.jadenk.springcloud.repository;

import de.jadenk.springcloud.model.CloudSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CloudSettingRepository extends JpaRepository<CloudSetting, String> {
    default List<CloudSetting> getAllSettings() {
        return findAll();
    }
}
