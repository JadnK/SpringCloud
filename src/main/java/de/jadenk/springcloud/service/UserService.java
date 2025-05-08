package de.jadenk.springcloud.service;

import de.jadenk.springcloud.model.Role;
import de.jadenk.springcloud.model.User;
import de.jadenk.springcloud.repository.RoleRepository;
import de.jadenk.springcloud.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private PasswordEncoder encoder;

    public void register(User user) {
        user.setPassword(encoder.encode(user.getPassword()));

        Role userRole = roleRepo.findById(1L).orElseThrow(() -> new RuntimeException("Role not found"));

        user.setRoles(Set.of(userRole));

        userRepo.save(user);
    }
}
