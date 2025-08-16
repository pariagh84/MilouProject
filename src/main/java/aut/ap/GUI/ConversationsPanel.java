package aut.ap.GUI;

import aut.ap.entity.Email;
import aut.ap.entity.User;
import aut.ap.services.EmailService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

public class ConversationsPanel extends JPanel {
    private final User me;
    private final IUserDirectory userDir;
    private final EmailService service;

    private final DefaultListModel<User> contactsModel = new DefaultListModel<>();
    private final DefaultListModel<Email> threadModel = new DefaultListModel<>();

    private final JList<User> contactsList = new JList<>(contactsModel);
    private final JList<Email> threadList = new JList<>(threadModel);

    private final JTextField addByEmail = new JTextField();
    private final JButton addBtn = new JButton("Add/Load");

    public ConversationsPanel(User me, IUserDirectory userDir, EmailService service) {
        super(new BorderLayout());
        this.me = me;
        this.userDir = userDir;
        this.service = service;

        contactsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactsList.setCellRenderer(new ContactRenderer());
        threadList.setCellRenderer(new ConversationEmailRenderer(me));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildContactsPanel(), new JScrollPane(threadList));
        split.setDividerLocation(260);
        add(split, BorderLayout.CENTER);

        contactsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                User c = contactsList.getSelectedValue();
                if (c != null) loadThread(c);
            }
        });

        addBtn.addActionListener(e -> addOrLoadContact());
        addByEmail.addActionListener(e -> addOrLoadContact());

        bootstrapContactsBestEffort();
    }

    private JPanel buildContactsPanel() {
        JPanel left = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new BorderLayout(6, 6));
        top.setBorder(new EmptyBorder(8, 8, 8, 8));

        addByEmail.setColumns(22);
        addByEmail.setToolTipText("Type an email address here, e.g. user@example.com");

        top.add(new JLabel("Find contact by email:"), BorderLayout.WEST);
        top.add(addByEmail, BorderLayout.CENTER);
        top.add(addBtn, BorderLayout.EAST);

        left.add(top, BorderLayout.NORTH);
        left.add(new JScrollPane(contactsList), BorderLayout.CENTER);
        return left;
    }

    private void addOrLoadContact() {
        String email = addByEmail.getText().trim();

        if (email.isEmpty()) {
            String entered = JOptionPane.showInputDialog(
                    this,
                    "Enter an email address:",
                    "Add/Load Contact",
                    JOptionPane.QUESTION_MESSAGE
            );
            if (entered == null) return;
            email = entered.trim();
            if (email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a non-empty email address.");
                return;
            }
            addByEmail.setText(email);
        }

        try {
            User u = userDir.findByEmail(email);
            if (u == null) {
                u = createStubUser(email);
            }
            int idx = addIfMissingAndGetIndex(u);
            if (idx >= 0) {
                contactsList.setSelectedIndex(idx);
                contactsList.ensureIndexIsVisible(idx);
                contactsList.requestFocusInWindow();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add/select contact.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private User createStubUser(String email) {
        User u = new User();
        u.setEmail(email);
        return u;
    }

    private int addIfMissingAndGetIndex(User u) {
        if (u.getId() != null) {
            for (int i = 0; i < contactsModel.size(); i++) {
                User existing = contactsModel.get(i);
                if (existing != null && existing.getId() != null &&
                        Objects.equals(existing.getId(), u.getId())) {
                    return i;
                }
            }
        }
        String em = u.getEmail();
        if (em != null) {
            for (int i = 0; i < contactsModel.size(); i++) {
                User existing = contactsModel.get(i);
                String exEmail = existing != null ? existing.getEmail() : null;
                if (exEmail != null && exEmail.equalsIgnoreCase(em)) {
                    return i;
                }
            }
        }
        contactsModel.addElement(u);
        return contactsModel.size() - 1;
    }

    private void loadThread(User contact) {
        try {
            List<Email> conv = service.getContactEmails(me, contact);
            conv.sort(Comparator.comparing(Email::getSendTime)); // chronological
            threadModel.clear();
            conv.forEach(threadModel::addElement);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Load conversation failed: " + ex.getMessage());
        }
    }

    private void bootstrapContactsBestEffort() {
        try {
            List<Email> all = service.getAllEmail(me);
            Map<Integer, User> byId = new LinkedHashMap<>();
            for (Email e : all) {
                User s = e.getSender();
                if (s != null && s.getId() != null && !Objects.equals(s.getId(), me.getId())) {
                    byId.putIfAbsent(s.getId(), s);
                }
            }
            contactsModel.clear();
            byId.values().forEach(contactsModel::addElement);
        } catch (Exception ignored) {
        }
    }

    // Renderers
    static class ContactRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof User u) {
                setText(u.getEmail() != null ? u.getEmail() : String.valueOf(u.getId()));
            }
            return this;
        }
    }

    static class ConversationEmailRenderer extends JPanel implements ListCellRenderer<Email> {
        private final JLabel bubble = new JLabel();
        private final User me;

        ConversationEmailRenderer(User me) {
            this.me = me;
            setLayout(new BorderLayout());
            add(bubble, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Email> list, Email value, int index, boolean isSelected, boolean cellHasFocus) {
            removeAll();
            boolean mine = value.getSender() != null && Objects.equals(value.getSender().getId(), me.getId());
            String who = mine ? "You" : (value.getSender() != null ? value.getSender().getEmail() : "?");
            String text = "<html><b>" + who + ":</b> " + escapeHtml(value.getBody()) + "</html>";
            bubble.setText(text);
            JPanel wrap = new JPanel(new FlowLayout(mine ? FlowLayout.RIGHT : FlowLayout.LEFT));
            bubble.setOpaque(true);
            bubble.setBorder(new EmptyBorder(6, 10, 6, 10));
            bubble.setBackground(mine ? new Color(220, 245, 220) : new Color(230, 230, 255));
            wrap.add(bubble);
            add(wrap, BorderLayout.CENTER);
            setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            return this;
        }

        private static String escapeHtml(String s) {
            if (s == null) return "";
            return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }
    }
}