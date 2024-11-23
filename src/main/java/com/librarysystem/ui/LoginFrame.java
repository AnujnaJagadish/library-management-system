package com.librarysystem.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;

/* Class is related to Login based on roles.*/

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel errorLabel;

    public LoginFrame() {
        setTitle("Library Management System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BackgroundPanel backgroundPanel = new BackgroundPanel("images/login.jpg");
        backgroundPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 2), 
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        formPanel.setBackground(new Color(245, 245, 245));

  
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);


        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Library Management System - Login", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        formPanel.add(titleLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        formPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 24));
        usernameField.setPreferredSize(new Dimension(300, 40));
        formPanel.add(usernameField, gbc);


        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        formPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 24));
        passwordField.setPreferredSize(new Dimension(300, 40));
        formPanel.add(passwordField, gbc);

  
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 24));
        loginButton.setPreferredSize(new Dimension(200, 50));
        formPanel.add(loginButton, gbc);


        gbc.gridy = 4;
        errorLabel = new JLabel("", JLabel.CENTER);
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        formPanel.add(errorLabel, gbc);

        gbc.gridy = 5;
        JButton registerButton = new JButton("New User? Click to Register");
        registerButton.setFont(new Font("Arial", Font.PLAIN, 22));
        registerButton.setPreferredSize(new Dimension(300, 50));
        formPanel.add(registerButton, gbc);

        gbc.gridy = 6;
        JButton guestButton = new JButton("Continue as a Guest");
        guestButton.setFont(new Font("Arial", Font.PLAIN, 22));
        guestButton.setPreferredSize(new Dimension(300, 50));
        formPanel.add(guestButton, gbc);

        backgroundPanel.add(formPanel);

        setContentPane(backgroundPanel);

        loginButton.addActionListener(e -> {
            errorLabel.setText(""); 
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Please provide both username and password.");
                return;
            }

            try {
                String jsonInputString = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);

                URL url = new URI("http://localhost:8080/api/auth/login").toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonInputString.getBytes());
                    os.flush();
                }

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Scanner scanner = new Scanner(conn.getInputStream());
                    String response = scanner.useDelimiter("\\A").next();
                    scanner.close();

                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode responseNode = objectMapper.readTree(response);

                    String role = responseNode.get("role").asText();
                    String name = responseNode.get("name").asText();
                    Long id = responseNode.get("Id").asLong();
                    if ("admin".equals(role)) {
                        JOptionPane.showMessageDialog(null, "Welcome Admin!");
                        dispose();
                        new AdminHomeFrame();
                    } else if ("member".equals(role)) {
                        JOptionPane.showMessageDialog(null, "Welcome Member!");
                        dispose();
                        new MemberDashboardFrame(name, id);
                    }
                } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    errorLabel.setText("Username/Password is incorrect");
                } else {
                    errorLabel.setText("Unexpected error occurred. Response code: " + responseCode);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error occurred while logging in.");
            }
        });

        registerButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(null, "Redirecting to Registration Page!");
            dispose();
            new RegistrationFrame();
        });

        guestButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(null, "Welcome, Guest! You can browse books.");
            dispose();
            new GuestPageFrame();
        });

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    public static void main(String[] args) {
        new LoginFrame();
    }

    /**
     * A custom panel for displaying a background image.
     */
    private static class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel(String imagePath) {
            try {
                backgroundImage = new ImageIcon(getClass().getClassLoader().getResource(imagePath)).getImage();
            } catch (Exception e) {
                e.printStackTrace();
                backgroundImage = null;
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
}
