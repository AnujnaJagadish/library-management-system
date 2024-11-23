package com.librarysystem.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.librarysystem.dto.BookDetailsDTO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

/* Class is related to member functionality to leave a review on books */

public class RateReviewBookFrame extends JFrame {

    private JTable searchResultsTable;
    private JTextField searchField;
    private JTextArea reviewArea;
    private JComboBox<Integer> ratingDropdown;

    public RateReviewBookFrame(Long memberId, String memberName) {
        setTitle("Rate/Review a Book");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        // Header
        JLabel headerLabel = new JLabel("Rate/Review a Book", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 36));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(headerLabel, BorderLayout.NORTH);

        // Search Panel
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        searchField = new JTextField();
        searchField.setFont(new Font("Arial", Font.PLAIN, 18));
        JButton searchButton = new JButton("Search");
        searchButton.setFont(new Font("Arial", Font.BOLD, 18));
        searchButton.addActionListener(e -> searchBooks());
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        // Search Results Table
        searchResultsTable = new JTable(new DefaultTableModel(new String[]{"Book ID", "Title", "Author"}, 0));
        searchResultsTable.setRowHeight(30);
        searchResultsTable.setFont(new Font("Arial", Font.PLAIN, 16));
        searchResultsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 18));
        JScrollPane tableScrollPane = new JScrollPane(searchResultsTable);
        tableScrollPane.setPreferredSize(new Dimension(800, 300));

        // Review and Footer Panel
        JPanel reviewAndFooterPanel = new JPanel(new BorderLayout());
        reviewAndFooterPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Review Section
        JPanel reviewPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel reviewLabel = new JLabel("Write a Review:");
        reviewLabel.setFont(new Font("Arial", Font.BOLD, 18));
        reviewPanel.add(reviewLabel, gbc);

        gbc.gridx = 1;
        reviewArea = new JTextArea(3, 20);
        reviewArea.setFont(new Font("Arial", Font.PLAIN, 30));
        reviewPanel.add(new JScrollPane(reviewArea), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel ratingLabel = new JLabel("Select Rating:");
        ratingLabel.setFont(new Font("Arial", Font.BOLD, 18));
        reviewPanel.add(ratingLabel, gbc);

        gbc.gridx = 1;
        ratingDropdown = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        ratingDropdown.setFont(new Font("Arial", Font.PLAIN, 16));
        reviewPanel.add(ratingDropdown, gbc);

        reviewAndFooterPanel.add(reviewPanel, BorderLayout.CENTER);

        // Footer Buttons
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton submitButton = new JButton("Submit Review");
        submitButton.setFont(new Font("Arial", Font.BOLD, 18));
        submitButton.addActionListener(e -> submitReview(memberId));
        footerPanel.add(submitButton);

        JButton backButton = new JButton("Back to Dashboard");
        backButton.setFont(new Font("Arial", Font.BOLD, 20));
        backButton.addActionListener(e -> {
            dispose();
            new MemberDashboardFrame(memberName, memberId);
        });
        footerPanel.add(backButton);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 20));
        logoutButton.addActionListener(e -> {
            dispose();
            new LoginFrame(); // Navigate to login screen
        });
        footerPanel.add(logoutButton);

        reviewAndFooterPanel.add(footerPanel, BorderLayout.SOUTH);

        // Combine Panels
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, reviewAndFooterPanel);
        splitPane.setResizeWeight(0.5);
        add(searchPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private void searchBooks() {
        String query = searchField.getText();
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            URL url = new URI("http://localhost:8080/api/books/search?query=" + encodedQuery).toURL();
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
                List<BookDetailsDTO> books = objectMapper.readValue(result.toString(), objectMapper.getTypeFactory().constructCollectionType(List.class, BookDetailsDTO.class));

                populateSearchResults(books);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to fetch books.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error occurred while fetching books.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateSearchResults(List<BookDetailsDTO> books) {
        DefaultTableModel model = (DefaultTableModel) searchResultsTable.getModel();
        model.setRowCount(0); // Clear existing rows
        for (BookDetailsDTO book : books) {
            model.addRow(new Object[]{book.getId(), book.getTitle(), book.getAuthor()});
        }
    }

    private void submitReview(Long memberId) {
        int selectedRow = searchResultsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to review.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Long bookId = (Long) searchResultsTable.getValueAt(selectedRow, 0);
        String reviewText = reviewArea.getText();
        Integer rating = (Integer) ratingDropdown.getSelectedItem();

        try {
            URL url = new URI("http://localhost:8080/api/books/review").toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            String payload = String.format(
                    "{\"bookId\":%d,\"memberId\":%d,\"rating\":%d,\"reviewText\":\"%s\"}",
                    bookId, memberId, rating, reviewText
            );
            conn.getOutputStream().write(payload.getBytes());

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                JOptionPane.showMessageDialog(this, "Review submitted successfully.");
                reviewArea.setText("");
                ratingDropdown.setSelectedIndex(0);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to submit review.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error occurred while submitting review.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new RateReviewBookFrame(1L, "John Doe");
    }
}
