package aut.ap.services;

import aut.ap.entity.User;
import aut.ap.exceptions.InvalidUserException;
import aut.ap.repository.UserRepository;
import aut.ap.security.PasswordSecurity;
import aut.ap.validation.UserValidator;
import aut.ap.validation.Validator;
import jakarta.persistence.EntityNotFoundException;

import javax.swing.*;
import java.util.List;
import java.util.Objects;

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean register(String email, String password) {
        User user = new User(email, password);
        Validator<User> userValidator = new UserValidator();
        try {
            userValidator.validate(user);
            if (password.length() < 8) {
                throw new InvalidUserException("Password should be bigger than 8 char.");
            }
            userRepository.addUser(user);
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            return false;
        }
    }

    public User login(String email, String password) {
        try {
            User usr = userRepository.findUser(email);

            if (email.isBlank()) {
                throw new IllegalArgumentException("Email can't be null.");
            }

            if (Objects.isNull(usr)) {
                throw new EntityNotFoundException("User not found.");
            }

            if (PasswordSecurity.checkPassword(password, usr.getPasswordHash())) {
                return usr;
            } else {
                throw new Exception("Password is not correct.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            return null;
        }
    }

    public List<User> showContact(User usr) {
        try {
            return userRepository.findAllContact(usr);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            return null;
        }
    }

    public static User findUser(String email) throws Exception{
        UserRepository userRepository1 = new UserRepository();
        return userRepository1.findUser(email);
    }
}
