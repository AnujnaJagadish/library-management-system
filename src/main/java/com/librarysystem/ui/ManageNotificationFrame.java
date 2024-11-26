package com.librarysystem.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.librarysystem.dto.NotificationRequestDTO;
import com.librarysystem.model.Reservation;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/* Class is related to admin functionality to send notifications for over-due books */

public class ManageNotificationFrame extends JFrame {

    private JTable reservationTable;
    private DefaultTableModel tableModel;

    public ManageNotificationFrame() {
        setTitle("Manage Notifications - Library Management System");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        // Header Label
        JLabel headerLabel = new JLabel("Manage Notifications", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 40));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(headerLabel, BorderLayout.NORTH);

        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        reservationTable = new JTable() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };

        tableModel = new DefaultTableModel(new String[]{"Reservation ID", "Member Name", "Book Title", "Reserved Date", "Due Date", "Action"}, 0);
        reservationTable.setModel(tableModel);
        reservationTable.setFont(new Font("Arial", Font.PLAIN, 16));
        reservationTable.setRowHeight(30);
        reservationTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 18)); 
        reservationTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        reservationTable.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox()));


        JScrollPane scrollPane = new JScrollPane(reservationTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);

        // Back Button
        JButton backButton = new JButton("Back to Dashboard");
        backButton.setFont(new Font("Arial", Font.BOLD, 24)); 
        backButton.setPreferredSize(new Dimension(250, 50));
        backButton.addActionListener(e -> {
            dispose();
            new AdminHomeFrame();
        });
        add(backButton, BorderLayout.SOUTH);


        // Fetch overdue reservations
        fetchOverdueReservations();

        setVisible(true);
    }


    private void fetchOverdueReservations() {
        try {
            URL url = new URI("http://localhost:8080/api/admin/reservations/past-due").toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // Read the response
                Scanner scanner = new Scanner(conn.getInputStream());
                StringBuilder result = new StringBuilder(); 
                while (scanner.hasNextLine()) {
                    result.append(scanner.nextLine());
                }
                scanner.close();

                // Parse the response using ObjectMapper
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                // Deserialize into a list of reservations
                List<Reservation> overdueReservations = objectMapper.readValue(
                        result.toString(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Reservation.class)
                );

                // Filter out reservations with a returnedDate
                overdueReservations.removeIf(reservation -> reservation.getReturnedDate() != null);

                // Populate the table with overdue reservations
                updateTable(overdueReservations);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to fetch overdue reservations. Response code: " + conn.getResponseCode(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error occurred while fetching overdue reservations: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable(List<Reservation> reservations) {
        DefaultTableModel model = (DefaultTableModel) reservationTable.getModel();
        model.setRowCount(0); // Clear existing rows

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");

        for (Reservation reservation : reservations) {
            String dueDate = reservation.getDueDate() != null ? reservation.getDueDate().toLocalDate().format(formatter) : "N/A";
            String reservedDate = reservation.getReservedDate() != null ? reservation.getReservedDate().toLocalDate().format(formatter) : "N/A";

            model.addRow(new Object[]{
                    reservation.getId(),
                    reservation.getMember().getName(),
                    reservation.getBook().getTitle(),
                    reservedDate,
                    dueDate,
                    "Send Notification"
            });
        }
    }

    private void sendNotification(Long reservationId) {
        try {
            Reservation reservation = getReservationById(reservationId);
            if (reservation == null) {
                JOptionPane.showMessageDialog(this, "Reservation not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            NotificationRequestDTO request = new NotificationRequestDTO();
            request.setReservationId(reservation.getId());
            request.setMemberId(reservation.getMember().getId());
            request.setMessage("The book \"" + reservation.getBook().getTitle() + "\" is past its due date!");

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            String json = objectMapper.writeValueAsString(request);

            URL url = new URI("http://localhost:8080/api/admin/notifications/send").toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.getOutputStream().write(json.getBytes());

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                JOptionPane.showMessageDialog(this, "Notification sent successfully!");
                // Refresh the overdue reservations
                fetchOverdueReservations();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to send notification.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error occurred while sending notification.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Reservation getReservationById(Long reservationId) {
        try {
            URL url = new URI("http://localhost:8080/api/reservations/" + reservationId).toURL();
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
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    
                return objectMapper.readValue(result.toString(), Reservation.class);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to fetch reservation details.", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error occurred while fetching reservation details.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    

    // Button Renderer for Action Column
    private static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setText("Send Notification");
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    // Button Editor for Action Column
    private class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private Long reservationId;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("Send Notification");
            button.setOpaque(true);
            button.addActionListener(e -> sendNotification(reservationId));
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            reservationId = (Long) tableModel.getValueAt(row, 0);
            return button;
        }
    }

    public static void main(String[] args) {
        new ManageNotificationFrame();
    }
}
