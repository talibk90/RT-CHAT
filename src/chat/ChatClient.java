package chat;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatClient {
    JFrame frame = new JFrame("Chat");
    JPanel msgPanel = new JPanel() {
        Image img = new ImageIcon("src/chat/image.jpg").getImage();

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
        }
    };
    JTextField tf = new JTextField(30);
    JButton sendBtn = new JButton("Send");
    JLabel status = new JLabel("Connecting...", SwingConstants.CENTER);
    PrintWriter out;
    String name, ip = "localhost";
    int port = 12345;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatClient().start());
    }

    void start() {
        // Login Dialog
        JTextField nF = new JTextField(10), iF = new JTextField("localhost", 10), pF = new JTextField("12345", 5);
        JPanel p = new JPanel();
        p.add(new JLabel("Name:"));
        p.add(nF);
        p.add(new JLabel("IP:"));
        p.add(iF);
        p.add(new JLabel("Port:"));
        p.add(pF);
        if (JOptionPane.showConfirmDialog(null, p, "Login", JOptionPane.OK_CANCEL_OPTION) != 0)
            System.exit(0);
        name = nF.getText();
        ip = iF.getText();
        try {
            port = Integer.parseInt(pF.getText());
        } catch (Exception e) {
        }

        // GUI Setup
        frame.setTitle("Chat - " + name);
        frame.setSize(400, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        msgPanel.setLayout(new BoxLayout(msgPanel, BoxLayout.Y_AXIS));

        JPanel bot = new JPanel();
        bot.add(tf);
        bot.add(sendBtn);
        frame.add(status, BorderLayout.NORTH);
        frame.add(new JScrollPane(msgPanel), BorderLayout.CENTER);
        frame.add(bot, BorderLayout.SOUTH);

        tf.setEditable(false);
        sendBtn.setEnabled(false); // Wait for peer
        sendBtn.addActionListener(e -> {
            if (!tf.getText().trim().isEmpty() && out != null) {
                out.println(tf.getText());
                addMsg(tf.getText(), true);
                tf.setText("");
            }
        });
        tf.addActionListener(sendBtn.getActionListeners()[0]);

        frame.setVisible(true);
        connect();
    }

    void connect() {
        new Thread(() -> {
            try {
                Socket s = new Socket(ip, port);
                out = new PrintWriter(s.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                out.println("NAME:" + name);

                String line;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("JOIN:") || line.startsWith("PRESENT:")) {
                        String peer = line.split(":")[1];
                        SwingUtilities.invokeLater(() -> {
                            status.setText("Connected to " + peer);
                            tf.setEditable(true);
                            sendBtn.setEnabled(true);
                        });
                    } else if (!line.startsWith("ID:")) {
                        String msg = line;
                        SwingUtilities.invokeLater(() -> addMsg(msg, false));
                    }
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> addMsg("Connection Failed", false));
            }
        }).start();
    }

    void addMsg(String text, boolean me) {
        JPanel b = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
            }
        };
        b.setOpaque(false);
        b.setLayout(new BoxLayout(b, BoxLayout.Y_AXIS));
        b.setBackground(me ? new Color(255, 200, 200) : new Color(200, 255, 200));
        b.setBorder(new EmptyBorder(5, 10, 5, 10));
        b.add(new JLabel("<html><p style='width:150px'>" + text + "</p></html>"));
        JLabel t = new JLabel(new SimpleDateFormat("HH:mm").format(new Date()));
        t.setFont(new Font("Sans", 0, 10));
        b.add(t);

        JPanel row = new JPanel(new FlowLayout(me ? FlowLayout.RIGHT : FlowLayout.LEFT, 5, 1));
        row.setOpaque(false);
        row.add(b);
        msgPanel.add(row);
        msgPanel.revalidate();

        SwingUtilities.invokeLater(() -> {
            msgPanel.scrollRectToVisible(new Rectangle(0, msgPanel.getHeight(), 1, 1));
        });
    }
}
