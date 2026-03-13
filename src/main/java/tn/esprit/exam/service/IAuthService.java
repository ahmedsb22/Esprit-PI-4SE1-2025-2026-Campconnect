package tn.esprit.exam.service;

import tn.esprit.exam.dto.auth.*;
import tn.esprit.exam.entity.User;

import java.util.List;

public interface IAuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    User getCurrentUser();

    User getUserById(Long id);

    List<User> getAllUsers();

    User updateProfile(Long id, UpdateProfileRequest updated);

    void changePassword(Long id, String oldPassword, String newPassword);

    void logout();

    void forgotPassword(String email);

    void resetPassword(String token, String newPassword);
}
