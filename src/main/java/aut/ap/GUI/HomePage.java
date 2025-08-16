package aut.ap.GUI;

import aut.ap.entity.Email;
import aut.ap.entity.User;
import aut.ap.repository.EmailRepository;
import aut.ap.services.EmailService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;

public class HomePage extends JFrame {
    private final User currentUser;
    private final IUserDirectory userDir;
    private final EmailService emailService = new EmailService(new EmailRepository());

    private final DefaultListModel<Email> allModel = new DefaultListModel<>();
    private final DefaultListModel<Email> unreadModel = new DefaultListModel<>();

    private final JList<Email> allList = new JList<>(allModel);
    private final JList<Email> unreadList = new JList<>(unreadModel);

    private final Map<Integer, Boolean> readCache = new HashMap<>();
    private final DateTimeFormatter tsFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public HomePage(User currentUser, IUserDirectory userDir) {
        this.currentUser = currentUser;
        this.userDir = userDir;

        setTitle("Home — " + safe(currentUser));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTabs(), BorderLayout.CENTER);

        loadAll();
        loadUnread();
    }

    private JComponent buildToolbar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setBorder(new EmptyBorder(8, 8, 8, 8));
        JButton compose = new JButton("Compose");
        JButton reply = new JButton("Reply");
        JButton forward = new JButton("Forward");
        JButton markRead = new JButton("Mark as Read");
        JButton refresh = new JButton("Refresh");

        JTextField searchId = new JTextField(10);
        JButton searchBtn = new JButton("Search Code");
        JButton logout = new JButton("Logout");

        p.add(compose);
        p.add(reply);
        p.add(forward);
        p.add(markRead);
        p.add(refresh);
        p.add(new JLabel("  |  Code:"));
        p.add(searchId);
        p.add(searchBtn);
        p.add(logout);

        compose.addActionListener(e -> openCompose());
        reply.addActionListener(e -> openReply(getSelectedEmail()));
        forward.addActionListener(e -> openForward(getSelectedEmail()));
        markRead.addActionListener(e -> markSelectedAsRead());
        refresh.addActionListener(e -> {
            loadAll();
            loadUnread();
        });
        searchBtn.addActionListener(e -> doSearchByCode(searchId.getText().trim()));
        searchId.addActionListener(e -> doSearchByCode(searchId.getText().trim()));
        logout.addActionListener(e -> {
            dispose();
            new LoginPage().setVisible(true);
        });

        return p;
    }

    private JComponent buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Mailbox (All)", buildMailboxPanel(allList));
        tabs.addTab("Unread", buildMailboxPanel(unreadList));
        tabs.addTab("Conversations", new ConversationsPanel(currentUser, userDir, emailService));
        return tabs;
    }

    private JComponent buildMailboxPanel(JList<Email> list) {
        list.setCellRenderer(new EmailCellRenderer(currentUser, emailService, readCache));

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JScrollPane left = new JScrollPane(list);
        JScrollPane right = new JScrollPane(area);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, left, right);
        split.setDividerLocation(320);

        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Email email = list.getSelectedValue();
                if (email == null) {
                    area.setText("");
                } else {
                    String meta = "From: " + safeUser(email.getSender()) + "\n" +
                            "Subject: " + nullSafe(email.getSubject()) + "\n" +
                            "Date: " + formatDate(email) + "\n" +
                            "-----------------------------\n";
                    area.setText(meta + nullSafe(email.getBody()));
                }
            }
        });

        return split;
    }

    private Email getSelectedEmail() {
        Email e = allList.getSelectedValue();
        if (e == null) e = unreadList.getSelectedValue();
        return e;
    }

    private void openCompose() {
        ComposeDialog d = new ComposeDialog(this, currentUser, userDir, emailService, sent -> {
            loadAll();
            loadUnread();
        });
        d.setVisible(true);
    }

    private void openReply(Email original) {
        if (original == null) {
            JOptionPane.showMessageDialog(this, "Select an email to reply.");
            return;
        }
        ReplyDialog d = new ReplyDialog(this, currentUser, emailService, original, sent -> {
            loadAll();
            loadUnread();
        });
        d.setVisible(true);
    }

    private void openForward(Email original) {
        if (original == null) {
            JOptionPane.showMessageDialog(this, "Select an email to forward.");
            return;
        }
        ForwardDialog d = new ForwardDialog(this, currentUser, userDir, emailService, original, sent -> {
            loadAll();
            loadUnread();
        });
        d.setVisible(true);
    }

    private void doSearchByCode(String s) {
        if (s.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter email code (ID)");
            return;
        }
        try {
            int id = Integer.parseInt(s);
            Email found = emailService.getEmailByCode(currentUser, id);
            if (found == null) {
                JOptionPane.showMessageDialog(this, "No email found.");
                return;
            }
            loadAll();
            loadUnread();

            selectEmailById(id);

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Invalid code.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Search failed: " + ex.getMessage());
        }
    }


    private void markSelectedAsRead() {
        Email e = getSelectedEmail();
        if (e == null) {
            JOptionPane.showMessageDialog(this, "Select an email first.");
            return;
        }
        try {
            emailService.readEmail(currentUser, e);
            readCache.put(e.getId(), true);
            allList.repaint();
            unreadList.repaint();
            loadUnread();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed: " + ex.getMessage());
        }
    }

    private void loadAll() {
        try {
            List<Email> emails = emailService.getAllEmail(currentUser);
            sortByDateDesc(emails);
            allModel.clear();
            emails.forEach(allModel::addElement);
            warmReadCache(emails);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Load all failed: " + ex.getMessage());
        }
    }

    private void loadUnread() {
        try {
            List<Email> emails = emailService.getUnreadEmail(currentUser);
            sortByDateDesc(emails);
            unreadModel.clear();
            emails.forEach(unreadModel::addElement);
            emails.forEach(e -> readCache.put(e.getId(), false));
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Load unread failed: " + ex.getMessage());
        }
    }

    private void sortByDateDesc(List<Email> emails) {
        try {
            emails.sort((a, b) -> {
                LocalDateTime da = a.getSendTime();
                LocalDateTime db = b.getSendTime();
                if (da == null && db == null) return 0;
                if (da == null) return 1;
                if (db == null) return -1;
                return db.compareTo(da);
            });
        } catch (Exception ignored) {
        }
    }

    private void warmReadCache(List<Email> emails) {
        for (Email e : emails) {
            try {
                if (!readCache.containsKey(e.getId())) {
                    Boolean r = emailService.isRead(e);
                    readCache.put(e.getId(), r != null && r);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private String formatDate(Email e) {
        try {
            LocalDateTime d = e.getSendTime();
            if (d == null) return "";
            return d.atZone(ZoneId.systemDefault()).format(tsFmt);
        } catch (Exception ex) {
            return "";
        }
    }

    private static String safe(User u) {
        return u == null ? "" : (u.getEmail() != null ? u.getEmail() : String.valueOf(u.getId()));
    }

    private static String safeUser(User u) {
        return safe(u);
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }

    private int indexOfEmail(DefaultListModel<Email> model, int id) {
        for (int i = 0; i < model.size(); i++) {
            Email e = model.get(i);
            if (e != null && e.getId() == id) return i;
        }
        return -1;
    }

    private void selectEmailById(int id) {
        int idx = indexOfEmail(allModel, id);
        if (idx != -1) {
            allList.setSelectedIndex(idx);
            allList.ensureIndexIsVisible(idx);
            allList.requestFocusInWindow();
            return;
        }

        idx = indexOfEmail(unreadModel, id);
        if (idx != -1) {
            unreadList.setSelectedIndex(idx);
            unreadList.ensureIndexIsVisible(idx);
            unreadList.requestFocusInWindow();
        }
    }

}
