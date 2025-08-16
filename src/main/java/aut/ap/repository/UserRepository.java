package aut.ap.repository;

import aut.ap.entity.User;
import org.hibernate.Session;

import java.util.List;
import java.util.function.Consumer;

public class UserRepository {
    public void addUser(User user) throws Exception {
        DbUtil.runInTransaction((Consumer<Session>) session -> session.persist(user));
    }

    public void updateUser(User user) throws Exception {
        String sqlCommand = """
                update users set email = :email,
                password_hash = :password
                where id = :id
                """;
        DbUtil.runInTransaction(session -> {
            session.createNativeMutationQuery(sqlCommand)
                    .setParameter("email", user.getEmail())
                    .setParameter("password", user.getPasswordHash())
                    .setParameter("id", user.getId())
                    .executeUpdate();
        });
    }

    public void deleteUser(User user) throws Exception {
        DbUtil.runInTransaction((Consumer<Session>) session -> session.remove(user));
    }

    public User findUser(String email) throws Exception {
        String sqlCommand = "select * from users where email = :email";

        return DbUtil.runInTransaction(session -> {
            return session.createNativeQuery(sqlCommand, User.class)
                    .setParameter("email", email)
                    .getSingleResultOrNull();
        });
    }

    public List<User> findAllContact(User user) throws Exception {
        String sqlCommand = """
                SELECT DISTINCT u.* 
                FROM users AS u 
                         JOIN email_recipients er ON u.id = er.receiver_id 
                         JOIN emails e ON e.id = er.email_id 
                WHERE e.sender_id = (SELECT id FROM users WHERE id = :user) 
                UNION 
                SELECT DISTINCT u.* 
                FROM users AS u 
                         JOIN emails e ON u.id = e.sender_id 
                         JOIN email_recipients er ON er.email_id = e.id 
                WHERE er.receiver_id = (SELECT id FROM users WHERE id = :user)""";

        return DbUtil.runInTransaction(session -> {
            return session.createNativeQuery(sqlCommand, User.class)
                    .setParameter("user", user.getId())
                    .getResultList();
        });
    }
}
