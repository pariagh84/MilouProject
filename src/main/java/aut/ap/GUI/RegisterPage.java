package aut.ap.GUI;

import aut.ap.repository.UserRepository;
import aut.ap.services.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RegisterPage extends JFrame {
    private final JTextField emailField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JPasswordField confirmField = new JPasswordField();

    public RegisterPage() {
        setTitle("Register");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(440, 360);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Create Your Account", JLabel.CENTER);
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
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0;
        form.add(new JLabel("Confirm"), c);
        c.gridx = 1;
        c.weightx = 1;
        form.add(confirmField, c);
        add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton back = new JButton("Back");
        JButton create = new JButton("Create Account");
        actions.add(back);
        actions.add(create);
        add(actions, BorderLayout.SOUTH);

        back.addActionListener(e -> {
            dispose();
            new LoginPage().setVisible(true);
        });
        create.addActionListener(e -> doRegister());
    }

    private void doRegister() {
        String email = emailField.getText().trim();
        String p1 = new String(passwordField.getPassword());
        String p2 = new String(confirmField.getPassword());
        if (email.isEmpty() || p1.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fill required fields.");
            return;
        }
        if (!p1.equals(p2)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match");
            return;
        }

        if (p1.length() < 8) {
            JOptionPane.showMessageDialog(this, "Password at least 8 char.");
        }
        try {
            UserService userService = new UserService(new UserRepository());
            if (userService.register(email, p1)) {
                JOptionPane.showMessageDialog(this, "Account created for: " + email);
                dispose();
                new LoginPage().setVisible(true);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
}