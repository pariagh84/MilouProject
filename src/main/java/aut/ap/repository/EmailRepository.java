package aut.ap.repository;

import aut.ap.entity.Email;
import aut.ap.entity.User;

import java.util.List;

public class EmailRepository {
    public void sendEmail(Email email, User receiver) throws Exception {
        DbUtil.runInTransaction(session -> {
            session.persist(email);
            session.flush();
            session.createNativeMutationQuery("""
                            INSERT INTO email_recipients(email_id, receiver_id) 
                            values (:email_id, :receiver_id)""")
                    .setParameter("email_id", email.getId())
                    .setParameter("receiver_id", receiver.getId())
                    .executeUpdate();
        });
    }

    public void forwardEmail(Email email, User receiver) throws Exception{
        Email newEmail = email.clone();
        newEmail.setForward(email);
        try {
            sendEmail(newEmail, receiver);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public void replyEmail(Email oldEmail, Email replyEmail) throws Exception {
        replyEmail.setReply(oldEmail);
        try {
            sendEmail(replyEmail, oldEmail.getSender());
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public List<Email> getAllEmail(User emailOwner) throws Exception {
        String sqlCommand = """
                SELECT DISTINCT e.* 
                FROM emails e 
                LEFT JOIN email_recipients r ON e.id = r.email_id 
                WHERE e.sender_id = :emailOwner OR r.receiver_id = :emailOwner
                """;

        return DbUtil.runInTransaction(session -> {
            return session.createNativeQuery(sqlCommand, Email.class)
                    .setParameter("emailOwner", emailOwner.getId())
                    .getResultList();
        });
    }

    public List<Email> getContactEmails(User emailOwner, User contact) throws Exception {
        String sqlCommand = """
                SELECT DISTINCT e.* 
                FROM emails e 
                LEFT JOIN email_recipients r ON e.id = r.email_id 
                WHERE e.sender_id = :emailOwner AND r.receiver_id = :contact 
                UNION 
                SELECT DISTINCT e.* 
                FROM emails e 
                LEFT JOIN email_recipients r ON e.id = r.email_id 
                WHERE e.sender_id = :contact AND r.receiver_id = :emailOwner""";

        return DbUtil.runInTransaction(session -> {
            return session.createNativeQuery(sqlCommand, Email.class)
                    .setParameter("emailOwner", emailOwner.getId())
                    .setParameter("contact", contact.getId())
                    .getResultList();
        });
    }

    public List<Email> getUnreadEmail(User emailOwner) throws Exception {
        String sqlCommand = """
                SELECT DISTINCT e.* 
                FROM emails e 
                LEFT JOIN email_recipients r ON e.id = r.email_id 
                WHERE r.receiver_id = :emailOwner AND r.is_read = false""";

        return DbUtil.runInTransaction(session -> {
            return session.createNativeQuery(sqlCommand, Email.class)
                    .setParameter("emailOwner", emailOwner.getId())
                    .getResultList();
        });
    }

    public Email getEmailByCode(User emailOwner, int emailId) throws Exception {
        String sqlCommand = """
                SELECT DISTINCT e.* 
                FROM emails e 
                LEFT JOIN email_recipients r ON e.id = r.email_id 
                WHERE (e.sender_id = :emailOwner OR r.receiver_id = :emailOwner) AND 
                e.id = :emailId""";

        return DbUtil.runInTransaction(session -> {
            return session.createNativeQuery(sqlCommand, Email.class)
                    .setParameter("emailOwner", emailOwner.getId())
                    .setParameter("emailId", emailId)
                    .getSingleResultOrNull();
        });
    }

    public void readEmail(User emailOwner, Email email) throws Exception {
        String sqlCommand = """
                UPDATE email_recipients 
                SET is_read = TRUE 
                WHERE email_id = :emailId AND receiver_id = :emailOwner""";

        DbUtil.runInTransaction(session -> {
            session.createNativeMutationQuery(sqlCommand)
                    .setParameter("emailId", email.getId())
                    .setParameter("emailOwner", emailOwner.getId())
                    .executeUpdate();
        });
    }

    public boolean isRead(Email email) throws Exception {
        String sqlCommand = """
                SELECT e.is_read FROM email_recipients e 
                WHERE email_id = :email""";


        return DbUtil.runInTransaction(session -> {
            return session.createNativeQuery(sqlCommand, Boolean.class)
                    .setParameter("email", email.getId())
                    .getSingleResultOrNull();
        });
    }
}
