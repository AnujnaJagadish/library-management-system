package com.librarysystem.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.librarysystem.dto.AdminBooksDTO;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.net.URLEncoder;

/* Class is related to admin functionality to update/delete books from the catalog */

public class UpdateDeleteBookFrame extends JFrame {
    private JTable booksTable;
    private JTextField searchField;

    public UpdateDeleteBookFrame() {
        setTitle("Update/Delete Books - Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Full-screen mode
        setLayout(new BorderLayout());

        // Header Panel
        JLabel headerLabel = new JLabel("Update/Delete Books", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 36));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(headerLabel, BorderLayout.NORTH);

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout());
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 20));
        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 20));
        JButton searchButton = new JButton("Search");
        searchButton.setFont(new Font("Arial", Font.BOLD, 20));
        searchButton.addActionListener(e -> searchBooks(searchField.getText()));
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        add(searchPanel, BorderLayout.NORTH);

        // Table Panel
        JPanel outerTablePanel = new JPanel(new BorderLayout());
        outerTablePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Margin around the table border
    
        booksTable = new JTable();
        booksTable.setFont(new Font("Arial", Font.PLAIN, 18)); 
        booksTable.setRowHeight(30);
        booksTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 18));
    
        JScrollPane tableScrollPane = new JScrollPane(booksTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2)); // Black border for the table
        outerTablePanel.add(tableScrollPane, BorderLayout.CENTER);
    
        add(outerTablePanel, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton updateButton = new JButton("Update");
        updateButton.setFont(new Font("Arial", Font.BOLD, 20));
        updateButton.addActionListener(e -> updateSelectedBook());
        JButton deleteButton = new JButton("Delete");
        deleteButton.setFont(new Font("Arial", Font.BOLD, 20));
        deleteButton.addActionListener(e -> deleteSelectedBook());
        JButton backButton = new JButton("Back to Dashboard");
        backButton.setFont(new Font("Arial", Font.BOLD, 20));
        backButton.addActionListener(e -> {
            dispose();
            new AdminHomeFrame();
        });
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void searchBooks(String query) {
        try {
            URL url = new URI("http://localhost:8080/api/admin/books/search?query=" +
                    URLEncoder.encode(query, StandardCharsets.UTF_8)).toURL();

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
                List<AdminBooksDTO> books = objectMapper.readValue(
                        result.toString(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, AdminBooksDTO.class)
                );

                updateTable(books);
            } else {
                JOptionPane.showMessageDialog(null, "Failed to fetch books. Try again later.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error occurred while searching books.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable(List<AdminBooksDTO> books) {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0;
            }
        };

        model.addColumn("ID");
        model.addColumn("Title");
        model.addColumn("Author");
        model.addColumn("Genre");
        model.addColumn("Count");
        model.addColumn("Reserved Count");
        model.addColumn("Borrowed Count");

        for (AdminBooksDTO book : books) {
            model.addRow(new Object[]{
                    book.getId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getGenre(),
                    book.getCount(),
                    book.getReservedCount(),
                    book.getBorrowedCount()
            });
        }

        booksTable.setModel(model);
    }

    private void updateSelectedBook() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Please select a book to update.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            DefaultTableModel model = (DefaultTableModel) booksTable.getModel();

            long id = Long.parseLong(model.getValueAt(selectedRow, 0).toString());
            String title = model.getValueAt(selectedRow, 1).toString();
            String author = model.getValueAt(selectedRow, 2).toString();
            String genre = model.getValueAt(selectedRow, 3).toString();
            int count = Integer.parseInt(model.getValueAt(selectedRow, 4).toString());

            String jsonInputString = String.format(
                    "{\"id\":%d,\"title\":\"%s\",\"author\":\"%s\",\"genre\":\"%s\",\"count\":%d}",
                    id, title, author, genre, count
            );

            URL url = new URI("http://localhost:8080/api/admin/books/update").toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInputString.getBytes());
                os.flush();
            }

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                JOptionPane.showMessageDialog(null, "Book updated successfully!");
            } else {
                JOptionPane.showMessageDialog(null, "Failed to update book. Try again later.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error occurred while updating the book.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedBook() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Please select a book to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            DefaultTableModel model = (DefaultTableModel) booksTable.getModel();
            long id = Long.parseLong(model.getValueAt(selectedRow, 0).toString());

            URL url = new URI("http://localhost:8080/api/admin/books/delete/" + id).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                JOptionPane.showMessageDialog(null, "Book deleted successfully!");
                model.removeRow(selectedRow);
            } else {
                JOptionPane.showMessageDialog(null, "Failed to delete book. Try again later.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error occurred while deleting the book.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new UpdateDeleteBookFrame();
    }
}
