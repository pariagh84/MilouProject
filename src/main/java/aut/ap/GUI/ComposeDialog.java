package aut.ap.GUI;

import aut.ap.entity.Email;
import aut.ap.entity.User;
import aut.ap.services.EmailService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;

public class ComposeDialog extends JDialog {
    private final User currentUser;
    private final IUserDirectory userDir;
    private final EmailService service;
    private final java.util.function.Consumer<Email> onSent;

    private final JTextField toField = new JTextField();
    private final JTextField subjectField = new JTextField();
    private final JTextArea bodyArea = new JTextArea();

    public ComposeDialog(JFrame owner, User currentUser, IUserDirectory userDir, EmailService service, java.util.function.Consumer<Email> onSent) {
        super(owner, "New Email", true);
        this.currentUser = currentUser;
        this.userDir = userDir;
        this.service = service;
        this.onSent = onSent;

        setSize(700, 520);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new GridBagLayout());
        header.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        header.add(new JLabel("To"), c);
        c.gridx = 1;
        c.weightx = 1;
        header.add(toField, c);
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        header.add(new JLabel("Subject"), c);
        c.gridx = 1;
        c.weightx = 1;
        header.add(subjectField, c);
        add(header, BorderLayout.NORTH);

        bodyArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        add(new JScrollPane(bodyArea), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton send = new JButton("Send");
        JButton cancel = new JButton("Cancel");
        actions.add(cancel);
        actions.add(send);
        add(actions, BorderLayout.SOUTH);

        cancel.addActionListener(e -> dispose());
        send.addActionListener(e -> doSend());
    }

    private void doSend() {
        String to = toField.getText().trim();
        String subject = subjectField.getText().trim();
        String body = bodyArea.getText();
        if (to.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Receiver email is required");
            return;
        }
        try {
            User receiver = userDir.findByEmail(to);
            if (receiver == null) {
                JOptionPane.showMessageDialog(this, "Receiver not found: " + to);
                return;
            }

            Email email = new Email();
            email.setSender(currentUser);
            email.setSubject(subject);
            email.setBody(body);
            email.setSendTime(LocalDateTime.now());

            service.sendEmail(email, receiver);
            if (onSent != null) {
                onSent.accept(email);
            }
            dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Send failed: " + ex.getMessage());
        }
    }
}
