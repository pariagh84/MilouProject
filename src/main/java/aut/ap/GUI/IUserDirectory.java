package aut.ap.GUI;

import aut.ap.entity.User;

public interface IUserDirectory {
    User findByEmail(String email) throws Exception;

    static String emailOrId(User u) { return (u.getEmail()!=null?u.getEmail():String.valueOf(u.getId())); }

}
