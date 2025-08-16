package aut.ap.GUI;

import aut.ap.entity.Email;
import aut.ap.entity.User;
import aut.ap.services.EmailService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ForwardDialog extends JDialog {
    private final User currentUser;
    private final IUserDirectory userDir;
    private final EmailService service;
    private final Email original;
    private final java.util.function.Consumer<Email> onSent;

    private final JTextField toField = new JTextField();
    private final JTextField subjectField = new JTextField();
    private final JTextArea bodyArea = new JTextArea();

    public ForwardDialog(JFrame owner, User currentUser, IUserDirectory userDir, EmailService service, Email original, java.util.function.Consumer<Email> onSent) {
        super(owner, "Forward", true);
        this.currentUser = currentUser;
        this.userDir = userDir;
        this.service = service;
        this.original = original;
        this.onSent = onSent;

        setSize(700, 520);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        subjectField.setText("Fwd: " + (original.getSubject() == null ? "" : original.getSubject()));

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
        bodyArea.setText("\n\nForwarded message:\n" + safeBody(original));
        add(new JScrollPane(bodyArea), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton send = new JButton("Send Forward");
        JButton cancel = new JButton("Cancel");
        actions.add(cancel);
        actions.add(send);
        add(actions, BorderLayout.SOUTH);

        cancel.addActionListener(e -> dispose());
        send.addActionListener(e -> doSend());
    }

    private void doSend() {
        String to = toField.getText().trim();
        try {
            service.forwardEmail(original, to);
            if (onSent != null) {
                onSent.accept(original);
            }
            dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Forward failed: " + ex.getMessage());
        }
    }

    private static String safeBody(Email e) {
        String b = e.getBody();
        return b == null ? "" : b;
    }
}
