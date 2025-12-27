package chat;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

// Main client class handling UI and networking
public class ChatClient {
    JFrame frame = new JFrame("Chat App");
    CardLayout cardLayout = new CardLayout();
    JPanel mainPanel = new JPanel(cardLayout);

    // Screens: Contact list (Lobby) and Chat view
    JPanel listPanel = new JPanel(new BorderLayout());
    JPanel chatContainer = new JPanel(new BorderLayout());

    // Connection data
    String myName, serverAddress = "localhost";
    int serverPort = 12345;

    String currentChatPeer = null;
    PrintWriter out;
    DefaultListModel<String> contactsModel = new DefaultListModel<>();
    JList<String> contactsList = new JList<>(contactsModel);
    Map<String, JPanel> chatPanels = new HashMap<>();
    Map<String, Integer> unreadMap = new HashMap<>(); // Unread message counts

    // UI Components
    JButton backBtn = createStyledButton("< Back", new Color(255, 69, 58));
    JLabel chatTitle = new JLabel("Chat", SwingConstants.CENTER);
    JTextField msgInput = new JTextField();
    JButton sendBtn = createStyledButton("Send", new Color(0, 122, 255));
    JButton emojiBtn = createStyledButton("😊", new Color(255, 204, 0));
    JButton themeBtn = new JButton("🌙");

    // Theme Data
    boolean isDarkMode = true;
    Color BG_DARK = new Color(28, 28, 30), BG_LIGHT = new Color(242, 242, 247);
    Color P_DARK = new Color(44, 44, 46), P_LIGHT = Color.WHITE;
    Color T_DARK = new Color(240, 240, 240), T_LIGHT = Color.BLACK;
    Color ACCENT = new Color(10, 132, 255);
    Font F_TITLE = new Font("Segoe UI Emoji", Font.BOLD, 20), F_NORM = new Font("Segoe UI Emoji", Font.PLAIN, 14);

    // Entry point
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        SwingUtilities.invokeLater(() -> new ChatClient().loginAndStart());
    }

    // Capture user name/IP and connect
    void loginAndStart() {
        JPanel p = new JPanel(new GridLayout(0, 1, 5, 5));
        JTextField n = new JTextField(System.getProperty("user.name")), i = new JTextField("localhost"),
                po = new JTextField("12345");
        p.add(new JLabel("Name:"));
        p.add(n);
        p.add(new JLabel("IP:"));
        p.add(i);
        p.add(new JLabel("Port:"));
        p.add(po);
        if (JOptionPane.showConfirmDialog(null, p, "Connect", 2, -1) != 0)
            System.exit(0);

        myName = n.getText().trim().isEmpty() ? "User" + (int) (Math.random() * 1000) : n.getText().trim();
        serverAddress = i.getText().trim().isEmpty() ? "localhost" : i.getText().trim();
        try {
            serverPort = Integer.parseInt(po.getText().trim());
        } catch (Exception e) {
            serverPort = 12345;
        }

        setupUI();
        connect();
    }

    // Build the main UI components
    void setupUI() {
        frame.setTitle("RT-CHAT: " + myName);
        frame.setSize(450, 700);
        frame.setDefaultCloseOperation(3);

        contactsList.setFixedCellHeight(70);
        contactsList.setCellRenderer(new ContactRenderer());
        contactsList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && contactsList.getSelectedValue() != null)
                    openChat(contactsList.getSelectedValue());
            }
        });
        contactsModel.addElement("📢 Announcements");

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(new EmptyBorder(25, 20, 25, 20));
        JLabel title = new JLabel("Messages");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));

        themeBtn.setBorderPainted(false);
        themeBtn.setContentAreaFilled(false);
        themeBtn.setFont(new Font("Segoe UI Emoji", 0, 24));
        themeBtn.addActionListener(e -> {
            isDarkMode = !isDarkMode;
            themeBtn.setText(isDarkMode ? "🌙" : "☀️");
            updateTheme();
        });

        top.add(title, "Center");
        top.add(themeBtn, "East");
        listPanel.add(top, "North");
        listPanel.add(new JScrollPane(contactsList), "Center");

        JPanel head = new JPanel(new BorderLayout());
        head.setBorder(new EmptyBorder(10, 10, 10, 10));
        chatTitle.setFont(F_TITLE);
        head.add(backBtn, "West");
        head.add(chatTitle, "Center");

        JPanel inp = new JPanel(new BorderLayout(10, 10));
        inp.setBorder(new EmptyBorder(10, 10, 10, 10));
        msgInput.setBorder(new EmptyBorder(10, 15, 10, 15));
        msgInput.setFont(F_NORM);
        emojiBtn.setForeground(Color.BLACK);
        emojiBtn.setFont(new Font("Segoe UI Emoji", 0, 18));
        emojiBtn.addActionListener(e -> showEmoji());

        inp.add(emojiBtn, "West");
        inp.add(msgInput, "Center");
        inp.add(sendBtn, "East");

        ActionListener send = e -> sendMessage();
        sendBtn.addActionListener(send);
        msgInput.addActionListener(send);
        backBtn.addActionListener(e -> {
            currentChatPeer = null;
            cardLayout.show(mainPanel, "LIST");
            contactsList.repaint();
        });

        chatContainer.add(head, "North");
        chatContainer.add(inp, "South");
        mainPanel.add(listPanel, "LIST");
        mainPanel.add(chatContainer, "CHAT");
        frame.add(mainPanel);
        frame.setVisible(true);
        updateTheme();
    }

    // Apply colors for Dark/Light mode
    void updateTheme() {
        Color bg = isDarkMode ? BG_DARK : BG_LIGHT, pO = isDarkMode ? P_DARK : P_LIGHT,
                txt = isDarkMode ? T_DARK : T_LIGHT;
        frame.getContentPane().setBackground(bg);
        listPanel.setBackground(bg);
        chatContainer.setBackground(bg);
        contactsList.setBackground(bg);
        contactsList.setForeground(txt);
        ((JPanel) listPanel.getComponent(0)).setBackground(pO);
        ((JLabel) ((JPanel) listPanel.getComponent(0)).getComponent(0)).setForeground(txt);
        ((JPanel) chatContainer.getComponent(0)).setBackground(pO);
        chatTitle.setForeground(txt);
        ((JPanel) chatContainer.getComponent(1)).setBackground(pO);
        msgInput.setBackground(isDarkMode ? new Color(60, 60, 60) : new Color(230, 230, 230));
        msgInput.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        msgInput.setCaretColor(txt);
        chatPanels.values().forEach(p -> p.setBackground(bg));
        frame.repaint();
    }

    // Switch to chat view for a user
    void openChat(String peer) {
        currentChatPeer = peer;
        chatTitle.setText(peer);
        unreadMap.remove(peer);
        contactsList.repaint();
        chatPanels.putIfAbsent(peer, createPanel());

        BorderLayout l = (BorderLayout) chatContainer.getLayout();
        if (l.getLayoutComponent("Center") != null)
            chatContainer.remove(l.getLayoutComponent("Center"));

        JScrollPane s = new JScrollPane(chatPanels.get(peer));
        s.setBorder(null);
        s.getViewport().setBackground(isDarkMode ? BG_DARK : BG_LIGHT);
        s.getVerticalScrollBar().setUnitIncrement(16);
        chatContainer.add(s, "Center");
        chatContainer.revalidate();
        chatContainer.repaint();
        scrollToBot(s);
        cardLayout.show(mainPanel, "CHAT");
    }

    // Send message to server (PM or Global)
    void sendMessage() {
        String t = msgInput.getText().trim();
        if (!t.isEmpty() && currentChatPeer != null) {
            boolean isGlobal = currentChatPeer.equals("📢 Announcements");
            out.println(isGlobal ? t : "PM:" + currentChatPeer + ":" + t);
            if (isGlobal && chatPanels.containsKey(currentChatPeer)) {
                addMsg(chatPanels.get(currentChatPeer), t, true);
                chatContainer.revalidate();
                Arrays.stream(chatContainer.getComponents()).filter(c -> c instanceof JScrollPane)
                        .forEach(c -> scrollToBot((JScrollPane) c));
            }
            msgInput.setText("");
        }
    }

    // Connect to server and listen for messages
    void connect() {
        new Thread(() -> {
            try (Socket s = new Socket(serverAddress, serverPort)) {
                out = new PrintWriter(s.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                out.println("NAME:" + myName);
                String l;
                while ((l = in.readLine()) != null) {
                    final String line = l;
                    SwingUtilities.invokeLater(() -> process(line));
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, "Connection Error"));
            }
        }).start();
    }

    // Process incoming message lines
    void process(String line) {
        if (line.startsWith("JOIN:") || line.startsWith("PRESENT:")) {
            String p = line.split(":")[1];
            if (!contactsModel.contains(p) && !p.equals(myName))
                contactsModel.addElement(p);
        } else if (line.startsWith("LEFT:"))
            contactsModel.removeElement(line.split(":")[1]);
        else if (line.startsWith("PM:") || line.startsWith("MSG:")) {
            String[] p = line.split(":", 3);
            if (p.length >= 3) {
                String s = p[1], m = p[2],
                        target = line.startsWith("PM:") ? (s.equals(myName) ? currentChatPeer : s) : "📢 Announcements";
                if (target != null) {
                    chatPanels.putIfAbsent(target, createPanel());
                    addMsg(chatPanels.get(target),
                            (!line.startsWith("PM:") && !s.equals(myName)) ? "<b>" + s + ":</b> " + m : m,
                            s.equals(myName));
                    if (!s.equals(myName) && !target.equals(currentChatPeer)) {
                        unreadMap.merge(target, 1, Integer::sum);
                        contactsList.repaint();
                    } else if (target.equals(currentChatPeer)) {
                        chatContainer.revalidate();
                        Arrays.stream(chatContainer.getComponents()).filter(c -> c instanceof JScrollPane)
                                .forEach(c -> scrollToBot((JScrollPane) c));
                    }
                }
            }
        }
    }

    // Create a new chat scroll panel
    JPanel createPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(isDarkMode ? BG_DARK : BG_LIGHT);
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        return p;
    }

    // Add a bubble to the chat panel
    void addMsg(JPanel p, String txt, boolean me) {
        String tm = new java.text.SimpleDateFormat("HH:mm").format(new Date());
        JPanel row = new JPanel(new FlowLayout(me ? 2 : 0));
        row.setOpaque(false);
        row.add(new Bubble(txt, tm, me));
        p.add(row);
        p.add(Box.createVerticalStrut(5));
        p.revalidate();
    }

    // Auto-scroll to bottom
    void scrollToBot(JScrollPane s) {
        SwingUtilities.invokeLater(() -> s.getVerticalScrollBar().setValue(s.getVerticalScrollBar().getMaximum()));
    }

    // Show emoji popup
    void showEmoji() {
        JPopupMenu m = new JPopupMenu();
        m.setLayout(new GridLayout(4, 4, 5, 5));
        m.setBackground(Color.WHITE);
        for (String e : new String[] { "😀", "😂", "🥰", "😎", "😭", "😡", "👍", "👎", "🎉", "🔥", "❤️", "💔", "👋",
                "🤔", "👀", "✨" }) {
            JButton b = new JButton(e);
            b.setFont(new Font("Segoe UI Emoji", 0, 20));
            b.setBorderPainted(false);
            b.setContentAreaFilled(false);
            b.addActionListener(a -> {
                msgInput.setText(msgInput.getText() + e);
                m.setVisible(false);
                msgInput.requestFocus();
            });
            m.add(b);
        }
        m.show(emojiBtn, 0, -150);
    }

    // Helper to style buttons
    JButton createStyledButton(String t, Color c) {
        JButton b = new JButton(t);
        b.setBackground(c);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", 1, 12));
        b.setBorder(new EmptyBorder(8, 15, 8, 15));
        b.setBorderPainted(false);
        return b;
    }

    // Renders contact list items with avatars
    class ContactRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
            JPanel p = new JPanel(new BorderLayout(10, 0));
            p.setBorder(new EmptyBorder(10, 10, 10, 10));
            p.setBackground(s ? (isDarkMode ? new Color(60, 60, 60) : new Color(200, 200, 200))
                    : (isDarkMode ? BG_DARK : BG_LIGHT));
            JLabel ico = new JLabel() {
                protected void paintComponent(Graphics g) {
                    ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(new Color(100, 100, 100));
                    g.fillOval(0, 0, 40, 40);
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("SansSerif", 1, 16));
                    String lt = v.toString().startsWith("📢") ? "📢" : v.toString().substring(0, 1).toUpperCase();
                    g.drawString(lt, 20 - g.getFontMetrics().stringWidth(lt) / 2, 26);
                }
            };
            ico.setPreferredSize(new Dimension(40, 40));
            String txt = v.toString()
                    + (unreadMap.getOrDefault(v.toString(), 0) > 0 ? " [" + unreadMap.get(v.toString()) + "]" : "");
            JLabel nm = new JLabel(txt);
            nm.setFont(new Font("Segoe UI Emoji", 1, 16));
            nm.setForeground(unreadMap.getOrDefault(v.toString(), 0) > 0 ? new Color(255, 69, 58)
                    : (isDarkMode ? T_DARK : T_LIGHT));
            p.add(ico, "West");
            p.add(nm, "Center");
            return p;
        }
    }

    // Custom chat bubble component
    class Bubble extends JPanel {
        Bubble(String t, String tm, boolean me) {
            setLayout(new BorderLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(8, 10, 8, 10));
            JLabel m = new JLabel("<html><body style='width: 200px'>" + t + "</body></html>");
            m.setFont(F_NORM);
            m.setForeground(me ? Color.WHITE : Color.BLACK);
            JLabel time = new JLabel(tm);
            time.setFont(new Font("Segoe UI", 0, 10));
            time.setForeground(me ? new Color(200, 200, 200) : new Color(100, 100, 100));
            time.setHorizontalAlignment(4);
            add(m, "Center");
            add(time, "South");
        }

        protected void paintComponent(Graphics g) {
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(((JLabel) getComponent(0)).getForeground() == Color.WHITE ? ACCENT : new Color(229, 229, 234));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            super.paintComponent(g);
        }
    }
}
