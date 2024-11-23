package com.librarysystem.ui;

import javax.swing.*;
import java.awt.*;

/* Class is related to UI part of Admin Dashboard */

public class AdminHomeFrame extends JFrame {

    public AdminHomeFrame() {
        setTitle("Admin Home - Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBackground(new Color(34, 40, 49));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 150));

        JLabel headerTitle = new JLabel("Admin Home");
        headerTitle.setFont(new Font("Arial", Font.BOLD, 48));
        headerTitle.setForeground(new Color(238, 238, 238));
        JLabel headerSubtitle = new JLabel("Welcome to the dashboard, admin");
        headerSubtitle.setFont(new Font("Arial", Font.PLAIN, 24));
        headerSubtitle.setForeground(new Color(136, 136, 136));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        headerPanel.add(headerTitle, gbc);
        gbc.gridy = 1;
        headerPanel.add(headerSubtitle, gbc);

        JPanel cardsContainer = new JPanel(new GridLayout(2, 2, 20, 20));
        cardsContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        cardsContainer.setBackground(new Color(57, 62, 70));

        cardsContainer.add(createCard("Manage Books", new String[]{"Add Book", "Delete/Update Book", "Search Books"}));
        cardsContainer.add(createCard("Manage Members", new String[]{"Add Member", "Delete/Update Member", "View Borrow History"}));
        cardsContainer.add(createCard("Manage Transactions", new String[]{"Approve Reservation", "Approve Return"}));
        cardsContainer.add(createCard("Manage Notifications", new String[]{"Manage Notifications"}));

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 16));
        logoutButton.setBackground(new Color(242, 38, 19));
        logoutButton.setForeground(Color.BLACK);
        logoutButton.setFocusPainted(false);
        logoutButton.setPreferredSize(new Dimension(120, 50));
        logoutButton.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(34, 40, 49));
        footerPanel.add(logoutButton);

        add(headerPanel, BorderLayout.NORTH);
        add(cardsContainer, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createCard(String title, String[] buttons) {
        JPanel cardPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, new Color(240, 240, 240), getWidth(), getHeight(), new Color(200, 200, 200));
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        cardPanel.setOpaque(false);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 50, 50), 3),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
    
        JLabel cardTitle = new JLabel(title, SwingConstants.CENTER);
        cardTitle.setFont(new Font("Arial", Font.BOLD, 24)); 
        cardTitle.setForeground(new Color(30, 30, 30));
        cardPanel.add(cardTitle, BorderLayout.NORTH);
    
        JPanel buttonsPanel = new JPanel(new GridLayout(buttons.length, 1, 10, 10));
        buttonsPanel.setOpaque(false);
    
        for (String buttonText : buttons) {
            JButton button = new JButton(buttonText);
            button.setFont(new Font("Arial", Font.BOLD, 18));
            button.setBackground(new Color(240, 240, 240));
            button.setForeground(new Color(30, 30, 30));
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(50, 50, 50), 2),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
    
            button.addActionListener(e -> handleCardAction(buttonText));
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    button.setBackground(new Color(220, 220, 220));
                    button.setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
    
                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    button.setBackground(new Color(240, 240, 240));
                    button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            });
    
            buttonsPanel.add(button);
        }
    
        cardPanel.add(buttonsPanel, BorderLayout.CENTER);
        return cardPanel;
    }
       

    private void handleCardAction(String action) {
        dispose();
        switch (action) {
            case "Add Book" -> new AddBookFrame();
            case "Delete/Update Book" -> new UpdateDeleteBookFrame();
            case "Search Books" -> new SearchBooksFrame();
            case "Add Member" -> new AddMemberFrame();
            case "Delete/Update Member" -> new UpdateDeleteMemberFrame();
            case "Approve Reservation" -> new ApproveBorrowFrame();
            case "Approve Return" -> new ApproveReturnFrame();
            case "View Borrow History" -> new BorrowHistoryFrame();
            case "Manage Notifications" -> new ManageNotificationFrame();
            default -> JOptionPane.showMessageDialog(this, "No action defined for: " + action);
        }
    }

    public static void main(String[] args) {
        new AdminHomeFrame();
    }
}

