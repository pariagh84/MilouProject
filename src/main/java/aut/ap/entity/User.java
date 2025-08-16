package aut.ap.entity;

import aut.ap.security.PasswordSecurity;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User implements Cloneable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false, columnDefinition = "TEXT")
    private String passwordHash;

    public User() {}

    public User(String email, String plainPassword) {
        setEmail(email);
        setPasswordHash(plainPassword);
    }

    public Integer getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String plainPassword) {
        this.passwordHash = PasswordSecurity.hashPassword(plainPassword);
    }

    @Override
    public User clone() {
        try {
            return (User) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public String toString() {
        return String.format("Email: %s", getEmail());
    }
}
