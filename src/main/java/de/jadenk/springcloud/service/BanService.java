package de.jadenk.springcloud.service;

import de.jadenk.springcloud.model.Ban;
import de.jadenk.springcloud.model.User;
import de.jadenk.springcloud.repository.BanRepository;
import de.jadenk.springcloud.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BanService {

    @Autowired
    private BanRepository banRepo;

    @Autowired
    private UserRepository userRepo;

    public void banUser(Long userId, String reason) {
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Ban ban = new Ban();
        ban.setUser(user);
        ban.setReason(reason);
        banRepo.save(ban);
    }
}