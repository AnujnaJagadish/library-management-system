package com.librarysystem.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.io.OutputStream;

/* Class is related to admin functionality for adding books to the catalog */

public class AddBookFrame extends JFrame {

    private JTextField titleField;
    private JTextField authorField;
    private JTextField genreField;
    private JSpinner countSpinner;
    private JLabel errorLabel;

    public AddBookFrame() {
        setTitle("Add Book - Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JLabel backgroundLabel = new JLabel(new ImageIcon(getClass().getClassLoader().getResource("images/add_book.jpg")));
        backgroundLabel.setLayout(new BorderLayout());
        setContentPane(backgroundLabel);

        JLabel headerLabel = new JLabel("Add New Book", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 36));
        headerLabel.setOpaque(true);
        headerLabel.setBackground(Color.BLACK);
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setBorder(new EmptyBorder(20, 0, 20, 0));
        add(headerLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridBagLayout());
        centerPanel.setOpaque(false);
        backgroundLabel.add(centerPanel, BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 3),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        formPanel.setBackground(new Color(245, 245, 245, 200));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel titleLabel = new JLabel("Title:");
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        formPanel.add(titleLabel, gbc);

        gbc.gridx = 1;
        titleField = new JTextField(20);
        titleField.setFont(new Font("Arial", Font.PLAIN, 18));
        formPanel.add(titleField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel authorLabel = new JLabel("Author:");
        authorLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        formPanel.add(authorLabel, gbc);

        gbc.gridx = 1;
        authorField = new JTextField(20);
        authorField.setFont(new Font("Arial", Font.PLAIN, 18));
        formPanel.add(authorField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel genreLabel = new JLabel("Genre:");
        genreLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        formPanel.add(genreLabel, gbc);

        gbc.gridx = 1;
        genreField = new JTextField(20);
        genreField.setFont(new Font("Arial", Font.PLAIN, 18));
        formPanel.add(genreField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel countLabel = new JLabel("Count:");
        countLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        formPanel.add(countLabel, gbc);

        gbc.gridx = 1;
        countSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        countSpinner.setFont(new Font("Arial", Font.PLAIN, 18));
        formPanel.add(countSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        errorLabel = new JLabel("", JLabel.CENTER);
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        formPanel.add(errorLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        JButton submitButton = new JButton("Add Book");
        submitButton.setFont(new Font("Arial", Font.BOLD, 20));
        formPanel.add(submitButton, gbc);

        gbc.gridx = 1;
        JButton backButton = new JButton("Back to Dashboard");
        backButton.setFont(new Font("Arial", Font.BOLD, 20));
        formPanel.add(backButton, gbc);

        centerPanel.add(formPanel);

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String title = titleField.getText();
                String author = authorField.getText();
                String genre = genreField.getText();
                int count = (int) countSpinner.getValue();

                if (title.isEmpty() || author.isEmpty() || genre.isEmpty()) {
                    errorLabel.setText("All fields are required!");
                    return;
                }

                try {
                    String jsonInputString = String.format("{\"title\":\"%s\",\"author\":\"%s\",\"genre\":\"%s\",\"count\":%d}", title, author, genre, count);

                    URL url = new URI("http://localhost:8080/api/books/add").toURL();
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");

                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(jsonInputString.getBytes());
                        os.flush();
                    }

                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        JOptionPane.showMessageDialog(AddBookFrame.this, "Book added successfully!");
                        clearForm();
                    } else {
                        JOptionPane.showMessageDialog(AddBookFrame.this, "Failed to add book!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    errorLabel.setText("Error occurred while adding book.");
                }
            }
        });

        backButton.addActionListener(e -> {
            System.out.println("Admin Home button clicked: " + e.getSource());
            dispose();
            new AdminHomeFrame();
        });

        setVisible(true);
    }

    private void clearForm() {
        titleField.setText("");
        authorField.setText("");
        genreField.setText("");
        countSpinner.setValue(1);
        errorLabel.setText("");
    }

    public static void main(String[] args) {
        new AddBookFrame();
    }
}
