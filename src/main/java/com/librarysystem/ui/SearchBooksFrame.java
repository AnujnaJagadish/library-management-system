package com.librarysystem.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.librarysystem.dto.AdminBooksDTO;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

/* Class is related to admin functionality to search books using ID, title, author in library catalog */

public class SearchBooksFrame extends JFrame {

    private JTable booksTable;

    public SearchBooksFrame() {
        setTitle("Search Books - Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); 
        setLayout(new BorderLayout());

        // Header Panel
        JLabel headerLabel = new JLabel("Search Books", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 48)); 
        headerLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(headerLabel, BorderLayout.NORTH);        

        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        booksTable = new JTable();
        booksTable.setFont(new Font("Arial", Font.PLAIN, 18));
        booksTable.setRowHeight(30); 
        JScrollPane scrollPane = new JScrollPane(booksTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(tablePanel, BorderLayout.CENTER);

        // Back Button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton backButton = new JButton("Back to Dashboard");
        backButton.setFont(new Font("Arial", Font.BOLD, 20));
        backButton.setPreferredSize(new Dimension(1400, 50));
        backButton.addActionListener(e -> {
            System.out.println("Admin-Home clicked: " + e.getSource());
            dispose();
            new AdminHomeFrame(); // Navigate to Admin Home
        });

        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Fetch books when the frame is loaded
        fetchBooks();

        setVisible(true);
    }

    private void fetchBooks() {
        try {
            URL url = new URI("http://localhost:8080/api/admin/books/list").toURL();
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
            JOptionPane.showMessageDialog(null, "Error occurred while fetching books.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable(List<AdminBooksDTO> books) {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Id");
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
    
        booksTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 18)); 
        booksTable.getTableHeader().setReorderingAllowed(false); 
    
        booksTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            {
                setHorizontalAlignment(SwingConstants.LEFT);
            }
        });
    
        booksTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = booksTable.getSelectedRow(); 
                if (selectedRow != -1) {
                    Long selectedId = (Long) model.getValueAt(selectedRow, 0); 
                    openBookDetails(selectedId); 
                }
            }
        });
    }
    

    private void openBookDetails(long id) {
        new BookDetailsFrame(id); 
        dispose(); // Close the SearchBooksFrame
    }

    public static void main(String[] args) {
        new SearchBooksFrame();
    }
}
