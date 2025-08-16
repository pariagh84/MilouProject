package aut.ap.services;

import aut.ap.entity.Email;
import aut.ap.entity.User;
import aut.ap.repository.EmailRepository;
import aut.ap.validation.EmailValidator;

import javax.swing.*;
import java.util.List;

public class EmailService {
    private final EmailRepository emailRepository;

    public EmailService(EmailRepository emailRepository) {
        this.emailRepository = emailRepository;
    }


    public void sendEmail(Email email, User receiver) throws Exception {
        EmailValidator emailValidator = new EmailValidator();
        emailValidator.validate(email);
        emailRepository.sendEmail(email, receiver);
    }

    public void forwardEmail(Email email, String receiver) {
        try {
            User userReceiver = UserService.findUser(receiver);
            emailRepository.forwardEmail(email, userReceiver);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    public void replyEmail(Email repliedEmail, Email email) {
        try {
            emailRepository.replyEmail(repliedEmail, email);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    public List<Email> getAllEmail(User emailOwner) {
        try {
            return emailRepository.getAllEmail(emailOwner);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            return null;
        }
    }

    public List<Email> getContactEmails(User emailOwner, User contact) {
        try {
            return emailRepository.getContactEmails(emailOwner, contact);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            return null;
        }
    }

    public List<Email> getUnreadEmail(User emailOwner) {
        try {
            return emailRepository.getUnreadEmail(emailOwner);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            return null;
        }
    }

    public Email getEmailByCode(User emailOwner, int emailId) {
        try {
            return emailRepository.getEmailByCode(emailOwner, emailId);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            return null;
        }
    }

    public void readEmail(User emailOwner, Email email) {
        try {
            emailRepository.readEmail(emailOwner, email);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    public boolean isRead(Email email) {
        boolean read = false;
        try {
            read = emailRepository.isRead(email);
        } catch (Exception e) {
            return false;
        }
        return read;
    }
}
