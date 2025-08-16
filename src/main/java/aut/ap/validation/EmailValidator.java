package aut.ap.validation;

import aut.ap.entity.Email;
import aut.ap.exceptions.InvalidEmailException;


import java.util.Objects;

public class EmailValidator implements Validator<Email> {
    @Override
    public void validate(Email email) throws Exception {
        if (email.getSubject().isBlank() || Objects.isNull(email.getSubject())) {
            throw new InvalidEmailException("Subject can't be null.");
        }

        if (email.getBody().isBlank() || Objects.isNull(email.getBody())) {
            throw new InvalidEmailException("Body can't be null.");
        }
    }
}
