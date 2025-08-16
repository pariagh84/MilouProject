package aut.ap.validation;

import aut.ap.entity.User;
import aut.ap.exceptions.DuplicateUserException;
import aut.ap.exceptions.InvalidEmailException;
import aut.ap.repository.UserRepository;

import java.util.Objects;

public class UserValidator implements Validator<User> {
    @Override
    public void validate(User user) throws Exception {
        UserRepository userRepository = new UserRepository();

        if (user.getEmail().isEmpty() || user.getEmail().equals("@milou.com")) {
            throw new InvalidEmailException("Email can not be blank.");
        }

        if (!user.getEmail().endsWith("@milou.com")) {
            throw new InvalidEmailException();
        }

        if (!Objects.isNull(userRepository.findUser(user.getEmail()))) {
            throw new DuplicateUserException("User email is already exist.");
        }

    }
}
