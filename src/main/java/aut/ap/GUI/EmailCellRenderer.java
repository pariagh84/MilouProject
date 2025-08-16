package aut.ap.GUI;

import aut.ap.entity.Email;
import aut.ap.entity.User;
import aut.ap.services.EmailService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.*;

public class EmailCellRenderer extends JPanel implements ListCellRenderer<Email> {
    private final JLabel line1 = new JLabel();
    private final JLabel line2 = new JLabel();
    private final User currentUser;
    private final EmailService service;
    private final Map<Integer, Boolean> readCache;

    public EmailCellRenderer(User currentUser, EmailService service, Map<Integer, Boolean> readCache) {
        this.currentUser = currentUser;
        this.service = service;
        this.readCache = readCache;
        setLayout(new BorderLayout(6, 2));
        JPanel top = new JPanel(new BorderLayout());
        top.add(line1, BorderLayout.WEST);
        add(top, BorderLayout.NORTH);
        add(line2, BorderLayout.SOUTH);
        setBorder(new EmptyBorder(6, 8, 6, 8));
        line2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Email> list, Email e, int index, boolean isSelected, boolean cellHasFocus) {
        String subject = e.getSubject() != null ? e.getSubject() : "(no subject)";
        String from = e.getSender() != null ? e.getSender().getEmail() : "?";
        String date = "";
        try {
            LocalDateTime d = e.getSendTime();
            if (d != null) date = d.toString();
        } catch (Exception ignored) {
        }

        line1.setText(subject);
        line2.setText(from + "  ·  " + date + "  ·  id=" + e.getId());

        boolean unread = isUnread(e);
        Font f = getFont();
        if (f == null) {
            f = list.getFont();
        }
        Font bold = f.deriveFont(unread ? Font.BOLD : Font.PLAIN);
        line1.setFont(bold);

        setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
        setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
        line1.setForeground(getForeground());
        line2.setForeground(getForeground().darker());
        return this;
    }

    private boolean isUnread(Email e) {
        try {
            Boolean cached = readCache.get(e.getId());
            if (cached != null) return !cached;
            Boolean isRead = service.isRead(e);
            boolean read = isRead != null && isRead;
            readCache.put(e.getId(), read);
            return !read;
        } catch (Exception ex) {
            return false;
        }
    }
}
