package com.librarysystem.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.librarysystem.dto.BookDetailsDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/* Class is related to member functionality to get a book details*/

public class BookDetailsFrame extends JFrame {

    public BookDetailsFrame(Long bookId) {
        setTitle("Book Details - Library Management System");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); 
        setLayout(new BorderLayout());

        JLabel headerLabel = new JLabel("Book Details", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 46));
        headerLabel.setBorder(new EmptyBorder(20, 0, 20, 0));
        add(headerLabel, BorderLayout.NORTH);

        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));
        mainContentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        add(new JScrollPane(mainContentPanel), BorderLayout.CENTER);

        try {
            URL url = new URI("http://localhost:8080/api/books/details/" + bookId).toURL();
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
                objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                objectMapper.findAndRegisterModules();

                BookDetailsDTO bookDetails = objectMapper.readValue(result.toString(), BookDetailsDTO.class);

                JPanel bookDetailsPanel = new JPanel(new GridLayout(6, 2, 10, 10));
                bookDetailsPanel.setBorder(BorderFactory.createTitledBorder("Book Details"));
                bookDetailsPanel.setBackground(new Color(245, 245, 245));

                bookDetailsPanel.add(createLabel("Title:"));
                bookDetailsPanel.add(createLabel(bookDetails.getTitle()));
                bookDetailsPanel.add(createLabel("Author:"));
                bookDetailsPanel.add(createLabel(bookDetails.getAuthor()));
                bookDetailsPanel.add(createLabel("Genre:"));
                bookDetailsPanel.add(createLabel(bookDetails.getGenre()));
                bookDetailsPanel.add(createLabel("Total Count:"));
                bookDetailsPanel.add(createLabel(String.valueOf(bookDetails.getCount())));
                bookDetailsPanel.add(createLabel("Reserved Count:"));
                bookDetailsPanel.add(createLabel(String.valueOf(bookDetails.getReservedCount())));
                bookDetailsPanel.add(createLabel("Borrowed Count:"));
                bookDetailsPanel.add(createLabel(String.valueOf(bookDetails.getBorrowedCount())));
                mainContentPanel.add(bookDetailsPanel);

                JPanel borrowedPanel = new JPanel(new BorderLayout());
                borrowedPanel.setBorder(BorderFactory.createTitledBorder("Borrowed Members"));
                JTable borrowedTable = new JTable();
                borrowedTable.setModel(new BorrowedMembersTableModel(bookDetails.getBorrowedMembers()));
                borrowedTable.setFont(new Font("Arial", Font.PLAIN, 16));
                borrowedTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 18));
                borrowedTable.setRowHeight(30);
                borrowedPanel.add(new JScrollPane(borrowedTable), BorderLayout.CENTER);
                mainContentPanel.add(borrowedPanel);

                JPanel reviewsPanel = new JPanel(new BorderLayout());
                reviewsPanel.setBorder(BorderFactory.createTitledBorder("Reviews and Ratings"));
                JTable reviewsTable = new JTable();
                reviewsTable.setModel(new ReviewsTableModel(bookDetails.getReviews()));
                reviewsTable.setFont(new Font("Arial", Font.PLAIN, 16));
                reviewsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 18));
                reviewsTable.setRowHeight(30);
                reviewsTable.setDefaultRenderer(Object.class, new RatingCellRenderer());
                reviewsPanel.add(new JScrollPane(reviewsTable), BorderLayout.CENTER);
                mainContentPanel.add(reviewsPanel);

            } else {
                JOptionPane.showMessageDialog(this, "Failed to fetch book details.", "Error", JOptionPane.ERROR_MESSAGE);
                dispose();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error occurred while fetching book details.", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
        }

        JButton backButton = new JButton("Back to Search");
        backButton.setFont(new Font("Arial", Font.BOLD, 20));
        backButton.setPreferredSize(new Dimension(200, 50));
        backButton.addActionListener(e -> {
            System.out.println("Back button clicked. Closing BookDetailsFrame." + e);
            new SearchBooksFrame();
            dispose();
        });

        add(backButton, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 22));
        return label;
    }

    private static class BorrowedMembersTableModel extends AbstractTableModel {
        private final List<BookDetailsDTO.BorrowedMemberDTO> borrowedMembers;
        private final String[] columnNames = {"Member Name", "Borrowed Date", "Due Date"};
        private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");

        public BorrowedMembersTableModel(List<BookDetailsDTO.BorrowedMemberDTO> borrowedMembers) {
            this.borrowedMembers = borrowedMembers != null ? borrowedMembers : List.of();
        }

        @Override
        public int getRowCount() {
            return borrowedMembers.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            BookDetailsDTO.BorrowedMemberDTO member = borrowedMembers.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> member.getName();
                case 1 -> member.getBorrowedDate() != null ? member.getBorrowedDate().format(dateFormatter) : "";
                case 2 -> member.getDueDate() != null ? member.getDueDate().format(dateFormatter) : "";
                default -> null;
            };
        }
    }

    private static class ReviewsTableModel extends AbstractTableModel {
        private final List<BookDetailsDTO.ReviewDTO> reviews;
        private final String[] columnNames = {"Member Name", "Rating", "Review", "Review Date"};
        private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");

        public ReviewsTableModel(List<BookDetailsDTO.ReviewDTO> reviews) {
            this.reviews = reviews != null ? reviews : List.of();
        }

        @Override
        public int getRowCount() {
            return reviews.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            BookDetailsDTO.ReviewDTO review = reviews.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> review.getMemberName();
                case 1 -> review.getRating();
                case 2 -> review.getReviewText();
                case 3 -> review.getReviewDate() != null ? review.getReviewDate().format(dateFormatter) : ""; 
                default -> null;
            };
        }
    }

    private static class RatingCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (column == 1 && value instanceof Integer rating) {
                if (rating >= 4) {
                    c.setBackground(Color.GREEN);
                } else if (rating == 3) {
                    c.setBackground(Color.YELLOW);
                } else {
                    c.setBackground(Color.RED);
                }
                c.setForeground(Color.BLACK);
            } else {
                c.setBackground(Color.WHITE);
                c.setForeground(Color.BLACK);
            }
            return c;
        }
    }

    public static void main(String[] args) {
        new BookDetailsFrame(1L); 
    }
}
