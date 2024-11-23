package com.librarysystem.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;

/* Class is related to register new people for library */

public class RegistrationFrame extends JFrame {
    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JLabel errorLabel;

    public RegistrationFrame() {
        setTitle("Library Management System - Register");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create the background panel
        BackgroundPanel backgroundPanel = new BackgroundPanel("images/register.jpg");
        backgroundPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Outer Panel for Form with Border
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 3), 
            BorderFactory.createEmptyBorder(20, 20, 20, 20) 
        ));
        formPanel.setBackground(new Color(245, 245, 245)); 

        // Add padding and alignment
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        Font labelFont = new Font("Arial", Font.BOLD, 20);
        Font fieldFont = new Font("Arial", Font.PLAIN, 18);
        Font buttonFont = new Font("Arial", Font.BOLD, 22);

        // Add Name Label and Field
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setFont(labelFont);
        formPanel.add(nameLabel, gbc);

        gbc.gridx = 1;
        nameField = new JTextField(20);
        nameField.setFont(fieldFont);
        formPanel.add(nameField, gbc);

        // Add Email Label and Field
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(labelFont);
        formPanel.add(emailLabel, gbc);

        gbc.gridx = 1;
        emailField = new JTextField(20);
        emailField.setFont(fieldFont);
        formPanel.add(emailField, gbc);

        // Add Password Label and Field
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(labelFont);
        formPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        passwordField.setFont(fieldFont);
        formPanel.add(passwordField, gbc);

        // Add Register Button
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JButton registerButton = new JButton("Register");
        registerButton.setFont(buttonFont);
        formPanel.add(registerButton, gbc);

        // Add Error Label
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        errorLabel = new JLabel("", JLabel.CENTER);
        errorLabel.setFont(fieldFont);
        errorLabel.setForeground(Color.RED);
        formPanel.add(errorLabel, gbc);

        // Add Back to Login Page Button
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        JButton backButton = new JButton("Back to Login Page");
        backButton.setFont(buttonFont);
        formPanel.add(backButton, gbc);

        // Back Button Action
        backButton.addActionListener(e -> {
            dispose();
            new LoginFrame(); 
        });

        backgroundPanel.add(formPanel);

        setContentPane(backgroundPanel);

        // Register Button Action
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                String email = emailField.getText();
                String password = new String(passwordField.getPassword());

                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    errorLabel.setText("All fields are required.");
                    return;
                }

                if (!isValidEmail(email)) {
                    errorLabel.setText("Invalid email format.");
                    return;
                }

                try {
                    String jsonInputString = String.format("{\"name\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}", name, email, password);

                    // Send POST request to backend
                    URL url = new URI("http://localhost:8080/api/register").toURL();
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");

                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(jsonInputString.getBytes());
                        os.flush();
                    }

                    int responseCode = conn.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        JOptionPane.showMessageDialog(null, "Registration Successful! Please login.");
                        dispose();
                        new LoginFrame();
                    } else {
                        Scanner scanner = new Scanner(conn.getErrorStream());
                        String response = scanner.useDelimiter("\\A").next();
                        scanner.close();
                        errorLabel.setText(response);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    errorLabel.setText("Error occurred during registration.");
                }
            }
        });

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    public static void main(String[] args) {
        new RegistrationFrame();
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
