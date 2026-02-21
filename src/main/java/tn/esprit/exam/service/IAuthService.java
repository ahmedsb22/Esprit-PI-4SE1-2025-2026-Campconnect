package tn.esprit.exam.service;

import tn.esprit.exam.entity.User;

import java.util.List;

public interface IAuthService {

    User register(User user);

    String login(String email, String rawPassword);

    User getCurrentUser(String token);

    User getUserById(Long id);

    List<User> getAllUsers();

    User updateProfile(Long id, User updated);

    void changePassword(Long id, String oldPassword, String newPassword);

    void logout(String token);
}

