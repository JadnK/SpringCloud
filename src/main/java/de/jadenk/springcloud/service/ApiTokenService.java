package de.jadenk.springcloud.service;

import de.jadenk.springcloud.model.ApiToken;
import de.jadenk.springcloud.model.Webhook;
import de.jadenk.springcloud.repository.ApiTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ApiTokenService {

    @Autowired
    private ApiTokenRepository apiTokenRepository;

    public List<ApiToken> getAll() {
        return apiTokenRepository.findAll();
    }

    public Optional<ApiToken> getFirst() {
        return apiTokenRepository.findAll().stream().findFirst();
    }

    public void deleteApiToken(Long id) {
        apiTokenRepository.deleteById(id);
    }
}
