package com.librarysystem.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.librarysystem.model.Reservation;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/* Class is related to member functionality to fetch books reservation history */

public class MyBooksFrame extends JFrame {

    private JTable returnedTable;
    private JTable reservedTable;
    private JTable borrowedTable;

    public MyBooksFrame(Long memberId, String memberName) {
        setTitle("My Books - Library Management System");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        // Header
        JLabel headerLabel = new JLabel("My Books", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 36));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(headerLabel, BorderLayout.NORTH);

        // Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 18));

        // Returned Books Table
        returnedTable = createTable(new String[]{"Reservation ID", "Title", "Author", "Return Date"});
        JScrollPane returnedScrollPane = new JScrollPane(returnedTable);
        returnedScrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Set black border
        tabbedPane.addTab("Returned Books", returnedScrollPane);

        // Reserved Books Table
        reservedTable = createTable(new String[]{"Reservation ID", "Title", "Author", "Reserved Date"});
        JScrollPane reservedScrollPane = new JScrollPane(reservedTable);
        reservedScrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Set black border
        tabbedPane.addTab("Reserved Books", reservedScrollPane);

        // Borrowed Books Table
        borrowedTable = createTable(new String[]{"Reservation ID", "Title", "Author", "Borrowed Date", "Due Date"});
        JScrollPane borrowedScrollPane = new JScrollPane(borrowedTable);
        borrowedScrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Set black border
        tabbedPane.addTab("Borrowed Books", borrowedScrollPane);

        add(tabbedPane, BorderLayout.CENTER);

        // Footer Panel with Back to Dashboard Button
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton backButton = new JButton("Back to Dashboard");
        backButton.setFont(new Font("Arial", Font.BOLD, 24));
        backButton.setPreferredSize(new Dimension(1420, 50));
        backButton.addActionListener(e -> {
            dispose();
            new MemberDashboardFrame(memberName, memberId);
        });
        footerPanel.add(backButton);
        add(footerPanel, BorderLayout.SOUTH);

        // Fetch data
        fetchBooks(memberId);

        setVisible(true);
    }

    private JTable createTable(String[] columnNames) {
        JTable table = new JTable(new DefaultTableModel(columnNames, 0));
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 16));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 18)); 
        return table;
    }


    private void fetchBooks(Long memberId) {
        fetchTableData("/api/reservations/mybooks/returned?memberId=" + memberId, returnedTable);
        fetchTableData("/api/reservations/mybooks/reserved?memberId=" + memberId, reservedTable);
        fetchTableData("/api/reservations/mybooks/borrowed?memberId=" + memberId, borrowedTable);
    }

    private void fetchTableData(String endpoint, JTable table) {
        try {
            URL url = new URI("http://localhost:8080" + endpoint).toURL();
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
                List<Reservation> reservations = objectMapper.readValue(
                        result.toString(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Reservation.class)
                );

                populateTable(reservations, table);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to fetch data.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error occurred while fetching data.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateTable(List<Reservation> reservations, JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0); // Clear existing rows

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");

        for (Reservation reservation : reservations) {
            Object[] row;
            switch (reservation.getActionType()) {
                case RETURN:
                    row = new Object[]{
                            reservation.getId(),
                            reservation.getBook().getTitle(),
                            reservation.getBook().getAuthor(),
                            reservation.getReturnedDate() != null ? reservation.getReturnedDate().toLocalDate().format(formatter) : ""
                    };
                    break;
                case RESERVE:
                    row = new Object[]{
                            reservation.getId(),
                            reservation.getBook().getTitle(),
                            reservation.getBook().getAuthor(),
                            reservation.getReservedDate() != null ? reservation.getReservedDate().toLocalDate().format(formatter) : ""
                    };
                    break;
                case BORROW:
                    row = new Object[]{
                            reservation.getId(),
                            reservation.getBook().getTitle(),
                            reservation.getBook().getAuthor(),
                            reservation.getBorrowedDate() != null ? reservation.getBorrowedDate().toLocalDate().format(formatter) : "",
                            reservation.getDueDate() != null ? reservation.getDueDate().toLocalDate().format(formatter) : ""
                    };
                    break;
                default:
                    continue;
            }
            model.addRow(row);
        }
    }

    public static void main(String[] args) {
        new MyBooksFrame(1L, "John Doe");
    }
}
