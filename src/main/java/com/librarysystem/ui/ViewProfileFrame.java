package com.librarysystem.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.librarysystem.model.Member;

import javax.swing.*;
import java.awt.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

/* Class is related to member functionality to edit his profile */

public class ViewProfileFrame extends JFrame {

    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JLabel errorLabel;

    private String memberName;
    private Long memberId;

    public ViewProfileFrame(String memberName, Long memberId) {
        this.memberName = memberName;
        this.memberId = memberId;

        setTitle("View Profile - Library Management System");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Create a Background Panel
        BackgroundPanel backgroundPanel = new BackgroundPanel();
        backgroundPanel.setLayout(new GridBagLayout());
        setContentPane(backgroundPanel);

        // Form Container
        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setBackground(Color.WHITE); 
        formContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 3),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        formContainer.setPreferredSize(new Dimension(600, 400)); 

        // Header
        JLabel headerLabel = new JLabel("Welcome, " + memberName, JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 36));
        headerLabel.setForeground(Color.BLACK);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        formContainer.add(headerLabel, BorderLayout.NORTH);

        // Form Panel
        JPanel formTable = new JPanel(new GridBagLayout());
        formTable.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Arial", Font.BOLD, 20);
        Font fieldFont = new Font("Arial", Font.PLAIN, 18);

        // Name Label and Field
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setFont(labelFont);
        formTable.add(nameLabel, gbc);

        gbc.gridx = 1;
        nameField = new JTextField(20);
        nameField.setFont(fieldFont);
        formTable.add(nameField, gbc);

        // Email Label and Field
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(labelFont);
        formTable.add(emailLabel, gbc);

        gbc.gridx = 1;
        emailField = new JTextField(20);
        emailField.setFont(fieldFont);
        formTable.add(emailField, gbc);

        // Password Label and Field
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(labelFont);
        formTable.add(passwordLabel, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        passwordField.setFont(fieldFont);
        formTable.add(passwordField, gbc);

        // Show Password Checkbox
        gbc.gridx = 1;
        gbc.gridy = 3;
        JCheckBox showPasswordCheckBox = new JCheckBox("Show Password");
        showPasswordCheckBox.setFont(fieldFont);
        showPasswordCheckBox.setOpaque(false);
        showPasswordCheckBox.addActionListener(e -> {
            if (showPasswordCheckBox.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('•');
            }
        });
        formTable.add(showPasswordCheckBox, gbc);

        // Error Label
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        errorLabel = new JLabel("", JLabel.CENTER);
        errorLabel.setFont(fieldFont);
        errorLabel.setForeground(Color.RED);
        formTable.add(errorLabel, gbc);

        // Buttons
        gbc.gridy = 5;
        gbc.gridwidth = 1;

        JButton updateButton = new JButton("Update");
        updateButton.setFont(new Font("Arial", Font.BOLD, 20));
        updateButton.addActionListener(e -> updateProfile());
        formTable.add(updateButton, gbc);

        gbc.gridx = 1;
        JButton backButton = new JButton("Back to Dashboard");
        backButton.setFont(new Font("Arial", Font.BOLD, 20));
        backButton.addActionListener(e -> {
            dispose();
            new MemberDashboardFrame(memberName, memberId);
        });
        formTable.add(backButton, gbc);

        formContainer.add(formTable, BorderLayout.CENTER);
        backgroundPanel.add(formContainer);

        fetchProfileDetails();

        setVisible(true);
    }

    private void fetchProfileDetails() {
        try {
            URL url = new URI("http://localhost:8080/api/member/profile/id/" + memberId).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                ObjectMapper objectMapper = new ObjectMapper();
                Member member = objectMapper.readValue(conn.getInputStream(), Member.class);

                nameField.setText(member.getName());
                emailField.setText(member.getEmail());
                passwordField.setText(member.getPassword());
            } else {
                JOptionPane.showMessageDialog(this, "Failed to fetch profile details.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error occurred while fetching profile details.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateProfile() {
        try {
            String name = nameField.getText();
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                errorLabel.setText("All fields are required.");
                return;
            }

            Member updatedMember = new Member();
            updatedMember.setId(memberId);
            updatedMember.setName(name);
            updatedMember.setEmail(email);
            updatedMember.setPassword(password);

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(updatedMember);

            URL url = new URI("http://localhost:8080/api/member/update").toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            conn.getOutputStream().write(json.getBytes());

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                JOptionPane.showMessageDialog(this, "Profile updated successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update profile.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error occurred while updating profile.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel() {
            try {
                backgroundImage = new ImageIcon(getClass().getResource("/images/profile.jpg")).getImage();
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

    public static void main(String[] args) {
        new ViewProfileFrame("Test Member", 1L);
    }
}
