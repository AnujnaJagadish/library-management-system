package com.librarysystem.ui;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


/* Class is related to admin functionality to fetch borrowed book details*/

public class BorrowHistoryFrame extends JFrame {

    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public BorrowHistoryFrame() {
        setTitle("Borrow History - Admin Dashboard");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        JLabel headerLabel = new JLabel("View Borrow History", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 36));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(headerLabel, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        searchField = new JTextField();
        searchField.setFont(new Font("Arial", Font.PLAIN, 18));
        searchField.setPreferredSize(new Dimension(300, 40));
        searchPanel.add(searchField, BorderLayout.CENTER);

        JButton searchButton = new JButton("Search");
        searchButton.setFont(new Font("Arial", Font.BOLD, 18));
        searchButton.addActionListener(e -> searchHistory(searchField.getText()));
        searchPanel.add(searchButton, BorderLayout.EAST);

        add(searchPanel, BorderLayout.NORTH);

        JPanel outerTablePanel = new JPanel(new BorderLayout());
        outerTablePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));     

        historyTable = new JTable();
        tableModel = new DefaultTableModel(new String[]{"Member Name", "Book Title", "Author", "Action Type", "Date"}, 0);
        historyTable.setModel(tableModel);
        historyTable.setFont(new Font("Arial", Font.PLAIN, 16));
        historyTable.setRowHeight(30);
        historyTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 18));
        JScrollPane tableScrollPane = new JScrollPane(historyTable);
        outerTablePanel.add(tableScrollPane, BorderLayout.CENTER);

        add(outerTablePanel, BorderLayout.CENTER);

        JButton backButton = new JButton("Back to Dashboard");
        backButton.setFont(new Font("Arial", Font.BOLD, 20));
        backButton.setPreferredSize(new Dimension(100,50));
        backButton.addActionListener(e -> {
            dispose();
            new AdminHomeFrame();
        });
        add(backButton, BorderLayout.SOUTH);

        fetchAllHistory();
        setVisible(true);
    }

    private void fetchAllHistory() {
        try {
            URL url = new URI("http://localhost:8080/api/admin/borrow-history").toURL();
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
                List<BorrowHistoryDTO> historyRecords = objectMapper.readValue(
                        result.toString(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, BorrowHistoryDTO.class)
                );

                updateTable(historyRecords);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to fetch history records.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error occurred while fetching history records.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchHistory(String query) {
        if (query == null || query.trim().isEmpty()) {
            fetchAllHistory();
            return;
        }

        try {
        String encodedQuery = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8.toString());
        URL url = new URI("http://localhost:8080/api/admin/borrow-history?query=" + encodedQuery).toURL();
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
                List<BorrowHistoryDTO> historyRecords = objectMapper.readValue(
                        result.toString(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, BorrowHistoryDTO.class)
                );

                updateTable(historyRecords);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to search history records.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error occurred while searching history records.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable(List<BorrowHistoryDTO> historyRecords) {
        tableModel.setRowCount(0); // Clear existing rows
        for (BorrowHistoryDTO record : historyRecords) {
            tableModel.addRow(new Object[]{
                    record.getMemberName(),
                    record.getBookTitle(),
                    record.getAuthor(),
                    record.getActionType(),
                    record.getDate()
            });
        }
    }

    public static void main(String[] args) {
        new BorrowHistoryFrame();
    }
}

// DTO Class
class BorrowHistoryDTO {
    private String memberName;
    private String bookTitle;
    private String author;
    private String actionType;
    private String date;

    // Getters and Setters
    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
