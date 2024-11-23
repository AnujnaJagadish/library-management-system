package com.librarysystem.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.librarysystem.dto.NotificationRequestDTO;

import javax.swing.*;
import java.awt.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

/* Class is related to member functionality to display member dashboard */

public class MemberDashboardFrame extends JFrame {

    private String memberName;
    private Long memberId;

    public MemberDashboardFrame(String memberName, Long memberId) {
        this.memberName = memberName;
        this.memberId = memberId;

        setTitle("Member Dashboard - Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        // Background Panel
        BackgroundPanel backgroundPanel = new BackgroundPanel();
        backgroundPanel.setLayout(new BorderLayout());
        setContentPane(backgroundPanel);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(255, 255, 255, 140));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel headerLabel = new JLabel("Welcome, " + memberName, JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 36));
        headerLabel.setForeground(Color.BLACK);
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        backgroundPanel.add(headerPanel, BorderLayout.NORTH);

        // Center Button Panel
        JPanel buttonPanel = createButtonPanel();
        buttonPanel.setOpaque(false);
        backgroundPanel.add(buttonPanel, BorderLayout.CENTER);

        // Logout and Notifications Panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setOpaque(false);

        // Logout Button
        JButton logoutButton = createButton("Logout", e -> logout());
        logoutButton.setPreferredSize(new Dimension(300, 50));
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoutPanel.setOpaque(false);
        logoutPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));
        logoutPanel.add(logoutButton);

        bottomPanel.add(logoutPanel, BorderLayout.NORTH);

        // Notifications Panel
        JPanel notificationsPanel = new JPanel();
        notificationsPanel.setBackground(new Color(255, 255, 255, 140));
        notificationsPanel.setLayout(new BoxLayout(notificationsPanel, BoxLayout.Y_AXIS));
        notificationsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                "Notifications",
                0,
                0,
                new Font("Arial", Font.BOLD, 14),
                Color.BLACK
        ));
        notificationsPanel.setPreferredSize(new Dimension(300, 100));
        fetchAndDisplayNotifications(notificationsPanel);
        bottomPanel.add(notificationsPanel, BorderLayout.CENTER);

        backgroundPanel.add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel() {
            try {
                backgroundImage = new ImageIcon(getClass().getResource("/images/member_dashboard.jpg")).getImage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout()); 
        buttonPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 0, 15, 0); 
        gbc.fill = GridBagConstraints.HORIZONTAL; 
        gbc.anchor = GridBagConstraints.CENTER;

        gbc.gridx = 0;
        gbc.gridy = 0;
        JButton viewProfileButton = createButton("View Profile", e -> openViewProfile());
        viewProfileButton.setPreferredSize(new Dimension(300, 50)); 
        buttonPanel.add(viewProfileButton, gbc);

        gbc.gridy = 1;
        JButton myBooksButton = createButton("My Books", e -> openMyBooks());
        myBooksButton.setPreferredSize(new Dimension(300, 50));
        buttonPanel.add(myBooksButton, gbc);

        gbc.gridy = 2;
        JButton discoverBooksButton = createButton("Discover Books", e -> openDiscoverBooks());
        discoverBooksButton.setPreferredSize(new Dimension(300, 50));
        buttonPanel.add(discoverBooksButton, gbc);

        gbc.gridy = 3;
        JButton reviewButton = createButton("Leave a Review", e -> openReviewBooks());
        reviewButton.setPreferredSize(new Dimension(300, 50));
        buttonPanel.add(reviewButton, gbc);

        return buttonPanel;
    }

    private void fetchAndDisplayNotifications(JPanel notificationsPanel) {
        notificationsPanel.removeAll();

        try {
            URL url = new URI("http://localhost:8080/api/members/" + memberId + "/notifications").toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Scanner scanner = new Scanner(conn.getInputStream());
                StringBuilder result = new StringBuilder();
                while (scanner.hasNextLine()) {
                    result.append(scanner.nextLine());
                }
                scanner.close();

                ObjectMapper objectMapper = new ObjectMapper();
                List<NotificationRequestDTO> notifications = objectMapper.readValue(
                        result.toString(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, NotificationRequestDTO.class)
                );

                if (notifications.isEmpty()) {
                    JLabel noNotificationsLabel = new JLabel("No new notifications.", JLabel.CENTER);
                    noNotificationsLabel.setFont(new Font("Arial", Font.BOLD, 18));
                    noNotificationsLabel.setForeground(Color.BLACK);
                    notificationsPanel.add(noNotificationsLabel);
                } else {
                    for (NotificationRequestDTO notification : notifications) {
                        JLabel notificationLabel = new JLabel(notification.getMessage());
                        notificationLabel.setFont(new Font("Arial", Font.BOLD, 16));
                        notificationLabel.setForeground(Color.BLACK);
                        notificationLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                        notificationsPanel.add(notificationLabel);
                    }
                }
            } else {
                JLabel errorLabel = new JLabel("Failed to fetch notifications. Please try again later.", JLabel.CENTER);
                errorLabel.setFont(new Font("Arial", Font.BOLD, 18));
                errorLabel.setForeground(Color.RED);
                notificationsPanel.add(errorLabel);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JLabel exceptionLabel = new JLabel("Error fetching notifications. Check your connection.", JLabel.CENTER);
            exceptionLabel.setFont(new Font("Arial", Font.BOLD, 18));
            exceptionLabel.setForeground(Color.RED);
            notificationsPanel.add(exceptionLabel);
        }

        notificationsPanel.revalidate();
        notificationsPanel.repaint();
    }

    private JButton createButton(String text, java.awt.event.ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setPreferredSize(new Dimension(300, 50));
        button.setFocusPainted(false);
        button.addActionListener(action);
        return button;
    }

    private void openViewProfile() {
        new ViewProfileFrame(memberName, memberId);
        dispose();
    }

    private void openMyBooks() {
        new MyBooksFrame(memberId, memberName);
        dispose();
    }

    private void openDiscoverBooks() {
        new DiscoverBooksFrame(memberName, memberId);
        dispose();
    }

    private void openReviewBooks() {
        new RateReviewBookFrame(memberId, memberName);
        dispose();
    }

    private void logout() {
        new LoginFrame();
        dispose();
    }

    public static void main(String[] args) {
        new MemberDashboardFrame("Test Member", 1L);
    }
}
