package com.librarysystem.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.librarysystem.model.Reservation;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/* Class is related to admin functionality for approving books returned by members*/

public class ApproveReturnFrame extends JFrame {

    private JTable reservationsTable;
    private DefaultTableModel tableModel;

    public ApproveReturnFrame() {
        setTitle("Approve Return Requests - Library Management System");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        // Header
        JLabel headerLabel = new JLabel("Pending Return Approvals", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 40));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(headerLabel, BorderLayout.NORTH);

        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tableModel = new DefaultTableModel(new String[]{"Reservation ID", "Member Name", "Book Title", "Due Date", "Action"}, 0);
        reservationsTable = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };

        reservationsTable.setRowHeight(40); 
        reservationsTable.setFont(new Font("Arial", Font.PLAIN, 18)); 
        reservationsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 20));
        
        // Set custom renderer and editor for the "Action" column
        reservationsTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonPanelRenderer());
        reservationsTable.getColumnModel().getColumn(4).setCellEditor(new ButtonPanelEditor());

        JScrollPane scrollPane = new JScrollPane(reservationsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        tablePanel.add(scrollPane, BorderLayout.CENTER);        

        add(tablePanel, BorderLayout.CENTER);

        // Back Button
        JButton backButton = new JButton("Back to Dashboard");
        backButton.setFont(new Font("Arial", Font.BOLD, 24));
        backButton.addActionListener(e -> {
            dispose();
            new AdminHomeFrame(); // Navigate back to Admin Dashboard
        });
        add(backButton, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(this::fetchPendingReturns);

        setVisible(true);
    }

    private void fetchPendingReturns() {
        try {
            URL url = new URI("http://localhost:8080/api/reservations/borrowed").toURL();
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

                updateTable(reservations);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to fetch reservations.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error occurred while fetching reservations.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
    }

    private void updateTable(List<Reservation> reservations) {
        tableModel.setRowCount(0); // Clear existing rows
        for (Reservation reservation : reservations) {
            tableModel.addRow(new Object[]{
                    reservation.getId(),
                    reservation.getMember().getName(),
                    reservation.getBook().getTitle(),
                    formatDate(reservation.getDueDate().toLocalDate()),
                    "Actions" // Placeholder for action buttons
            });
        }
    }

    private void approveReturn(Long reservationId) {
        try {
            URL url = new URI("http://localhost:8080/api/reservations/approveReturn?reservationId=" + reservationId).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                JOptionPane.showMessageDialog(this, "Return approved successfully.");
                SwingUtilities.invokeLater(this::fetchPendingReturns);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to approve return.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error occurred while approving return.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void extendDueDate(Long reservationId) {
        try {
            URL url = new URI("http://localhost:8080/api/reservations/extendDue?reservationId=" + reservationId).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                JOptionPane.showMessageDialog(this, "Due date extended successfully.");
                SwingUtilities.invokeLater(this::fetchPendingReturns);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to extend due date.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error occurred while extending due date.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class ButtonPanelRenderer extends JPanel implements TableCellRenderer {
        public ButtonPanelRenderer() {
            setLayout(new FlowLayout(FlowLayout.LEFT));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton approveButton = new JButton("Approve Return");
            JButton extendButton = new JButton("Extend Due");

            Long reservationId = (Long) table.getValueAt(row, 0);

            approveButton.addActionListener(e -> approveReturn(reservationId));
            extendButton.addActionListener(e -> extendDueDate(reservationId));

            panel.add(approveButton);
            panel.add(extendButton);

            return panel;
        }
    }

    private class ButtonPanelEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            panel.removeAll(); // Clear previous buttons
            JButton approveButton = new JButton("Approve Return");
            JButton extendButton = new JButton("Extend Due");

            Long reservationId = (Long) table.getValueAt(row, 0);

            approveButton.addActionListener(e -> {
                approveReturn(reservationId);
                stopCellEditing();
            });

            extendButton.addActionListener(e -> {
                extendDueDate(reservationId);
                stopCellEditing();
            });

            panel.add(approveButton);
            panel.add(extendButton);

            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "Actions";
        }
    }

    public static void main(String[] args) {
        new ApproveReturnFrame();
    }
}
