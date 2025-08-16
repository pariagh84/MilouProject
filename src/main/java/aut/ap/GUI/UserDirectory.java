package aut.ap.GUI;

import aut.ap.entity.User;
import aut.ap.services.UserService;

import java.util.ArrayList;
import java.util.List;

public class UserDirectory implements IUserDirectory {

    private final User user;
    private final List<User> contacts;

    public UserDirectory(User user) {
        this.user = user;
        this.contacts = new ArrayList<>();
    }

    public User getUser() {
        return user;
    }

    public List<User> getContacts() {
        return contacts;
    }

    public void addContact(User contact) {
        if (!contacts.contains(contact)) {
            contacts.add(contact);
        }
    }

    @Override
    public User findByEmail(String email) throws Exception {
        return UserService.findUser(email);
    }
}
