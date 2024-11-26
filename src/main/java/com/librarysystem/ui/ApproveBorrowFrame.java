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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/* Class is related to admin functionality for approving books borrowed by members*/

public class ApproveBorrowFrame extends JFrame {

    private JTable reservationsTable;
    private DefaultTableModel tableModel;

    public ApproveBorrowFrame() {
        setTitle("Approve Borrow Requests - Library Management System");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        // Header
        JLabel headerLabel = new JLabel("Pending Borrow Approvals", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 40)); 
        headerLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(headerLabel, BorderLayout.NORTH);

        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tableModel = new DefaultTableModel(new String[]{"Reservation ID", "Book Title", "Member Name", "Reserved Date", "Action"}, 0);
        reservationsTable = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };

        reservationsTable.setFont(new Font("Arial", Font.PLAIN, 16)); 
        reservationsTable.setRowHeight(40); 
        reservationsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 18));

        // Set the custom renderer and editor for the "Action" column
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
            new AdminHomeFrame();
        });
        add(backButton, BorderLayout.SOUTH);

        fetchPendingReservations();

        setVisible(true);
    }

    private void fetchPendingReservations() {
        try {
            URL url = new URI("http://localhost:8080/api/reservations/pending").toURL();
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

                SwingUtilities.invokeLater(() -> updateTable(reservations)); 
            } else {
                JOptionPane.showMessageDialog(this, "Failed to fetch reservations.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error occurred while fetching reservations.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
    }

    private void updateTable(List<Reservation> reservations) {
        tableModel.setRowCount(0); // Clear existing rows
        for (Reservation reservation : reservations) {
            tableModel.addRow(new Object[]{
                    reservation.getId(),
                    reservation.getBook().getTitle(),
                    reservation.getMember().getName(),
                    formatDate(reservation.getReservedDate()),
                    "Actions" 
            });
        }
    }

    private void approveBorrow(Long reservationId) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    URL url = new URI("http://localhost:8080/api/reservations/approve?reservationId=" + reservationId).toURL();
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");

                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(ApproveBorrowFrame.this, "Borrow approved successfully."));
                        SwingUtilities.invokeLater(() -> fetchPendingReservations()); // Refresh table
                    } else {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(ApproveBorrowFrame.this, "Failed to approve borrow.", "Error", JOptionPane.ERROR_MESSAGE));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(ApproveBorrowFrame.this, "Error occurred while approving borrow.", "Error", JOptionPane.ERROR_MESSAGE));
                }
                return null;
            }
        }.execute();
    }

    private void cancelBorrowRequest(Long reservationId) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    URL url = new URI("http://localhost:8080/api/reservations/cancel?reservationId=" + reservationId).toURL();
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("DELETE");

                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(ApproveBorrowFrame.this, "Borrow request canceled successfully.");
                            fetchPendingReservations(); // Refresh table
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                                ApproveBorrowFrame.this, "Failed to cancel borrow request.", "Error", JOptionPane.ERROR_MESSAGE));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                            ApproveBorrowFrame.this, "Error occurred while canceling borrow request.", "Error", JOptionPane.ERROR_MESSAGE));
                }
                return null;
            }
        }.execute();
    }

    // Custom Button Renderer for Action Panel
    private static class ButtonPanelRenderer extends JPanel implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton approveButton = new JButton("Approve");
            JButton cancelButton = new JButton("Cancel");

            approveButton.setPreferredSize(new Dimension(100, 30));
            cancelButton.setPreferredSize(new Dimension(100, 30));

            panel.add(approveButton);
            panel.add(cancelButton);

            return panel;
        }
    }

    // Custom Button Editor for Action Panel
    private class ButtonPanelEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel;
        private final JButton approveButton;
        private final JButton cancelButton;
        private Long currentReservationId;

        public ButtonPanelEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            approveButton = new JButton("Approve");
            cancelButton = new JButton("Cancel");

            approveButton.addActionListener(e -> {
                if (currentReservationId != null) {
                    approveBorrow(currentReservationId);
                }
                stopCellEditing();
            });

            cancelButton.addActionListener(e -> {
                if (currentReservationId != null) {
                    cancelBorrowRequest(currentReservationId);
                }
                stopCellEditing();
            });

            panel.add(approveButton);
            panel.add(cancelButton);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentReservationId = (Long) table.getValueAt(row, 0);
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return panel;
        }

        @Override
        public boolean stopCellEditing() {
            fireEditingStopped();
            return true;
        }
    }

    public static void main(String[] args) {
        new ApproveBorrowFrame();
    }
}
