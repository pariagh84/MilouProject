package aut.ap.GUI;

import aut.ap.entity.User;
import aut.ap.repository.UserRepository;
import aut.ap.services.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginPage extends JFrame {
    private final JTextField emailField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();

    public LoginPage() {
        setTitle("Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 320);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Welcome to My Email App", JLabel.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        title.setBorder(new EmptyBorder(16, 0, 8, 0));
        add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        form.add(new JLabel("Email"), c);
        c.gridx = 1;
        c.weightx = 1;
        form.add(emailField, c);
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        form.add(new JLabel("Password"), c);
        c.gridx = 1;
        c.weightx = 1;
        form.add(passwordField, c);
        add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton login = new JButton("Login");
        JButton register = new JButton("Register");
        actions.add(register);
        actions.add(login);
        add(actions, BorderLayout.SOUTH);

        login.addActionListener(e -> doLogin());
        register.addActionListener(e -> {
            dispose();
            new RegisterPage().setVisible(true);
        });
    }

    private void doLogin() {
        String email = emailField.getText().trim();
        String pass = new String(passwordField.getPassword());
        if (email.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill email & password.");
            return;
        }
        try {
            UserService userService = new UserService(new UserRepository());
            User currentUser = userService.login(email, pass);

            if (currentUser == null) {
                return;
            }

            IUserDirectory userDir = new UserDirectory(currentUser);

            dispose();
            new HomePage(currentUser, userDir).setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Login failed: " + ex.getMessage());
        }
    }
}
