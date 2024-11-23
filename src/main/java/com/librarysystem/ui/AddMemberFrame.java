package com.librarysystem.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.io.OutputStream;
import java.util.Scanner;

/* Class is related to admin functionality for adding members to the Library */

public class AddMemberFrame extends JFrame {
    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JLabel errorLabel;

    public AddMemberFrame() {
        setTitle("Library Management System - Add Member");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel backgroundPanel = new BackgroundPanel("images/add_book.jpg");
        backgroundPanel.setLayout(new GridBagLayout());
        add(backgroundPanel);

        GridBagConstraints gbc = new GridBagConstraints();

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 3),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        formPanel.setBackground(new Color(245, 245, 245, 200));

        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        Font labelFont = new Font("Arial", Font.BOLD, 20);
        Font fieldFont = new Font("Arial", Font.PLAIN, 18);
        Font buttonFont = new Font("Arial", Font.BOLD, 22);

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setFont(labelFont);
        formPanel.add(nameLabel, gbc);

        gbc.gridx = 1;
        nameField = new JTextField(20);
        nameField.setFont(fieldFont);
        formPanel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(labelFont);
        formPanel.add(emailLabel, gbc);

        gbc.gridx = 1;
        emailField = new JTextField(20);
        emailField.setFont(fieldFont);
        formPanel.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(labelFont);
        formPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        passwordField.setFont(fieldFont);
        formPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setFont(labelFont);
        formPanel.add(roleLabel, gbc);

        gbc.gridx = 1;
        roleComboBox = new JComboBox<>(new String[]{"member", "admin"});
        roleComboBox.setFont(fieldFont);
        formPanel.add(roleComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        JButton registerButton = new JButton("Add Member");
        registerButton.setFont(buttonFont);
        formPanel.add(registerButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        errorLabel = new JLabel("", JLabel.CENTER);
        errorLabel.setFont(fieldFont);
        errorLabel.setForeground(Color.RED);
        formPanel.add(errorLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        JButton backButton = new JButton("Back to Dashboard");
        backButton.setFont(buttonFont);
        formPanel.add(backButton, gbc);

        backgroundPanel.add(formPanel, gbc);

        backButton.addActionListener(e -> {
            System.out.println("Dashboard button clicked: " + e.getSource());
            dispose();
            new AdminHomeFrame(); 
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                String email = emailField.getText();
                String password = new String(passwordField.getPassword());
                String role = (String) roleComboBox.getSelectedItem();

                if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role == null) {
                    errorLabel.setText("All fields are required.");
                    return;
                }

                if (!isValidEmail(email)) {
                    errorLabel.setText("Invalid email format.");
                    return;
                }

                try {
                    String jsonInputString = String.format(
                            "{\"name\":\"%s\",\"email\":\"%s\",\"password\":\"%s\",\"role\":\"%s\"}",
                            name, email, password, role
                    );

                    // Send POST request to backend
                    URL url = new URI("http://localhost:8080/api/admin/addMember").toURL();
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
                        JOptionPane.showMessageDialog(null, "Member added successfully.");
                        clearForm();
                    } else {
                        Scanner scanner = new Scanner(conn.getErrorStream());
                        String response = scanner.useDelimiter("\\A").next();
                        scanner.close();
                        errorLabel.setText(response);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    errorLabel.setText("Error occurred during member addition.");
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

    private void clearForm() {
        nameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        roleComboBox.setSelectedIndex(0);
        errorLabel.setText("");
    }

    public static void main(String[] args) {
        new AddMemberFrame();
    }
}

class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    public BackgroundPanel(String imagePath) {
        try {
            backgroundImage = new ImageIcon(getClass().getClassLoader().getResource(imagePath)).getImage();
        } catch (Exception e) {
            System.err.println("Error loading background image: " + imagePath);
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
