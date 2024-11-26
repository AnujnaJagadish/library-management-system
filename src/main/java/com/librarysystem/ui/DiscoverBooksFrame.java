package com.librarysystem.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.librarysystem.dto.BookDetailsDTO;
import com.librarysystem.dto.BookDetailsDTO.ReviewDTO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Scanner;

/* Class is related to member functionality to search for books by title,author or genre */

public class DiscoverBooksFrame extends JFrame {

    private JTextField searchField;
    private JTable booksTable;
    private DefaultTableModel tableModel;
    private Long memberId;
    private String memberName;

    public DiscoverBooksFrame(String memberName, Long memberId) {
        this.memberId = memberId;
        this.memberName = memberName;

        setTitle("Discover Books - Library Management System");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        // Header
        JLabel headerLabel = new JLabel("Discover Books", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 40));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(headerLabel, BorderLayout.NORTH);

        // Search Panel
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        searchField = new JTextField();
        searchField.setFont(new Font("Arial", Font.PLAIN, 18));
        searchField.setPreferredSize(new Dimension(300, 50));
        searchPanel.add(searchField, BorderLayout.CENTER);

        JButton searchButton = new JButton("Search");
        searchButton.setFont(new Font("Arial", Font.BOLD, 20));
        searchButton.addActionListener(e -> searchBooks(searchField.getText()));
        searchPanel.add(searchButton, BorderLayout.EAST);

        add(searchPanel, BorderLayout.NORTH);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        booksTable = new JTable() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5 || column == 6;
            }
        };
        tableModel = new DefaultTableModel(new String[]{"ID", "Title", "Author", "Genre", "Count", "Reserve", "View Reviews"}, 0);
        booksTable.setModel(tableModel);
        booksTable.setFont(new Font("Arial", Font.PLAIN, 18)); 
        booksTable.setRowHeight(35); 

        booksTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 18));

        booksTable.getColumn("Reserve").setCellRenderer(new ButtonRenderer("Reserve"));
        booksTable.getColumn("Reserve").setCellEditor(new ReserveButtonEditor(new JCheckBox()));

        booksTable.getColumn("View Reviews").setCellRenderer(new ButtonRenderer("View Reviews"));
        booksTable.getColumn("View Reviews").setCellEditor(new ReviewButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(booksTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(tablePanel, BorderLayout.CENTER);

        JButton backButton = new JButton("Back to Dashboard");
        backButton.setFont(new Font("Arial", Font.BOLD, 24)); 
        backButton.addActionListener(e -> {
            dispose();
            new MemberDashboardFrame(memberName, memberId);
        });
        add(backButton, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void searchBooks(String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a search query.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String encodedQuery = URLEncoder.encode(query.trim(), "UTF-8");
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
                objectMapper.registerModule(new JavaTimeModule()); 
                List<BookDetailsDTO> books = objectMapper.readValue(
                        result.toString(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, BookDetailsDTO.class)
                );

                updateTable(books);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to fetch books.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error occurred while searching for books.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable(List<BookDetailsDTO> books) {
        tableModel.setRowCount(0);
        for (BookDetailsDTO book : books) {
            tableModel.addRow(new Object[]{
                    book.getId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getGenre(),
                    book.getCount(),
                    "Reserve",
                    "View Reviews"
            });
        }
    }

    private static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer(String label) {
            setText(label);
            setFont(new Font("Arial", Font.BOLD, 18)); 
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    private class ReserveButtonEditor extends DefaultCellEditor {
        private JButton button;
        private Long bookId;

        public ReserveButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("Reserve");
            button.setFont(new Font("Arial", Font.BOLD, 18)); 
            button.setOpaque(true);
            button.addActionListener(e -> reserveBook());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            bookId = (Long) tableModel.getValueAt(row, 0);
            return button;
        }

        private void reserveBook() {
            try {
                String json = String.format("{\"bookId\":%d,\"memberId\":%d,\"actionType\":\"RESERVE\"}", bookId, memberId);
                URL url = new URI("http://localhost:8080/api/reservations/add").toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.getOutputStream().write(json.getBytes());

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    JOptionPane.showMessageDialog(null, "Book reserved successfully. Admin will approve the borrow request.");
                    searchBooks(searchField.getText());
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to reserve book.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error occurred while reserving the book.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Review button editor
    private class ReviewButtonEditor extends DefaultCellEditor {
        private JButton button;
        private Long bookId;

        public ReviewButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("View Reviews");
            button.setFont(new Font("Arial", Font.BOLD, 18)); 
            button.setOpaque(true);
            button.addActionListener(e -> viewReviews());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            bookId = (Long) tableModel.getValueAt(row, 0);
            return button;
        }

        private void viewReviews() {
            try {
                URL url = new URI("http://localhost:8080/api/books/" + bookId + "/reviews").toURL();
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
                    objectMapper.registerModule(new JavaTimeModule());
                    List<ReviewDTO> reviews = objectMapper.readValue(
                            result.toString(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, ReviewDTO.class)
                    );

                    showReviews(reviews);
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to fetch reviews.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error occurred while fetching reviews.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void showReviews(List<ReviewDTO> reviews) {
            StringBuilder message = new StringBuilder("Reviews:\n");
            for (ReviewDTO review : reviews) {
                message.append(String.format("Member: %s\nRating: %d\nReview: %s\n\n",
                        review.getMemberName(), review.getRating(), review.getReviewText()));
            }
            JOptionPane.showMessageDialog(null, message.toString());
        }
    }

    public static void main(String[] args) {
        new DiscoverBooksFrame("Test User", 1L);
    }
}
