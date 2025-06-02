package de.jadenk.springcloud.service;

import de.jadenk.springcloud.model.CloudSetting;
import de.jadenk.springcloud.repository.CloudSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CloudSettingService {

    private final CloudSettingRepository repository;

    @Autowired
    public CloudSettingService(CloudSettingRepository repository) {
        this.repository = repository;
    }

    public Optional<CloudSetting> getSetting(String key) {
        return repository.findById(key);
    }

    public void updateSetting(String key, String value, String type) {
        Optional<CloudSetting> existing = repository.findById(key);
        CloudSetting setting;
        if (existing.isPresent()) {
            setting = existing.get();
            setting.setValue(value);
            setting.setType(type);
        } else {
            setting = new CloudSetting(key, value);
            setting.setType(type);
        }
        repository.save(setting);
    }



    public boolean getBooleanSetting(String key, boolean defaultValue) {
        return getSetting(key)
                .map(s -> Boolean.parseBoolean(s.getValue()))
                .orElse(defaultValue);
    }

    public Integer getNumberSetting(String key, int defaultValue) {
        return getSetting(key)
                .map(s -> Integer.parseInt(s.getValue()))
                .orElse(defaultValue);
    }


}
