package de.jadenk.springcloud.controller;

import de.jadenk.springcloud.config.SecurityConfig;
import de.jadenk.springcloud.model.User;
import de.jadenk.springcloud.repository.UserRepository;
import de.jadenk.springcloud.service.UserService;
import de.jadenk.springcloud.util.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Controller
public class UserSettingController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private MessageService messageService;

    private Map<String, Object> getUserAttributes(User user) {
        Map<String, Object> attributes = new HashMap<>();
        for (Field field : User.class.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(user);
                if (field.getName().equals("profileImageData") && value != null) {
                    byte[] imageBytes = (byte[]) value;
                    String base64 = Base64.getEncoder().encodeToString(imageBytes);
                    String dataUri = "data:image/png;base64," + base64;
                    attributes.put("profileImageData", dataUri);
                } else {
                    attributes.put(field.getName(), value);
                }
            } catch (IllegalAccessException e) {
                attributes.put(field.getName(), "ACCESS ERROR");
            }
        }
        return attributes;
    }


    @GetMapping("/settings")
    public String userSettingsPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userService.getUserByName(username);
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        model.addAttribute("userAttributes", getUserAttributes(user));
        return "user-settings";
    }

    @PostMapping("/user/settings/update")
    public String updateUserSettings(@RequestParam("username") String username,
                                     @RequestParam("email") String email,
                                     @RequestParam(value = "currentPassword", required = false) String currentPassword,
                                     @RequestParam(value = "newPassword", required = false) String newPassword,
                                     @RequestParam(value = "newPasswordConfirm", required = false) String newPasswordConfirm,
                                     @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
                                     @RequestParam(value = "notificationsEnabled", required = false, defaultValue = "false") boolean notificationsEnabled,
                                     Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User user = userService.getUserByName(currentUsername);

        if (username != null && !user.getUsername().equals(username)) {
            user.setUsername(username);
        }

        if (email != null && !user.getEmail().equals(email)) {
            user.setEmail(email);
        }

        boolean wantsToChangePassword =
                currentPassword != null && !currentPassword.trim().isEmpty() &&
                        newPassword != null && !newPassword.trim().isEmpty() &&
                        newPasswordConfirm != null && !newPasswordConfirm.trim().isEmpty();


        if (wantsToChangePassword) {

            if (!securityConfig.passwordEncoder().matches(currentPassword, user.getPassword())) {
                model.addAttribute("error", messageService.getError("changepassword.current.invalid"));
                model.addAttribute("userAttributes", getUserAttributes(user));
                return "user-settings";
            }

            if (!newPassword.equals(newPasswordConfirm)) {
                model.addAttribute("error", messageService.getError("changepassword.different.passwords"));
                model.addAttribute("userAttributes", getUserAttributes(user));
                return "user-settings";
            }

            if (securityConfig.passwordEncoder().matches(newPassword, user.getPassword())) {
                model.addAttribute("error", messageService.getError("changepassword.same.password"));
                model.addAttribute("userAttributes", getUserAttributes(user));
                return "user-settings";
            }

            user.setPassword(securityConfig.passwordEncoder().encode(newPassword));
        }

        user.setNotificationsEnabled(notificationsEnabled);

        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                InputStream is = profileImage.getInputStream();
                BufferedImage originalImage = ImageIO.read(is);

                int targetWidth = 64;
                int targetHeight = 64;

                BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = resizedImage.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
                g.dispose();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(resizedImage, "png", baos);
                baos.flush();
                byte[] imageBytes = baos.toByteArray();
                baos.close();

                user.setProfileImageData(imageBytes);
            } catch (IOException e) {
                model.addAttribute("error", messageService.getError("settings.upload.error"));
                model.addAttribute("userAttributes", getUserAttributes(user));
                return "user-settings";
            }
        }

        userRepository.save(user);

        model.addAttribute("userAttributes", getUserAttributes(user));
        return "user-settings";
    }


}
