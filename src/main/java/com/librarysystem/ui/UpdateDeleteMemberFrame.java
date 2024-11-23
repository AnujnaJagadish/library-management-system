package com.librarysystem.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.librarysystem.model.Member;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

/* Class is related to admin functionality to update/delete members from the catalog */

public class UpdateDeleteMemberFrame extends JFrame {

    private JTextField searchField;
    private JTable memberTable;
    private DefaultTableModel tableModel;

    public UpdateDeleteMemberFrame() {
        setTitle("Update/Delete Members - Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); 
        setLayout(new BorderLayout());

        // Header
        JLabel headerLabel = new JLabel("Update/Delete Members", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 36));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setOpaque(true);
        headerLabel.setBackground(new Color(70, 130, 180)); 
        headerLabel.setBorder(new EmptyBorder(20, 0, 20, 0));
        add(headerLabel, BorderLayout.NORTH);

        // Search Panel
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        searchField = new JTextField();
        searchField.setFont(new Font("Arial", Font.PLAIN, 18));
        searchField.setPreferredSize(new Dimension(300, 40));
        searchPanel.add(searchField, BorderLayout.CENTER);

        JButton searchButton = new JButton("Search");
        searchButton.setFont(new Font("Arial", Font.BOLD, 18));
        searchButton.setBackground(new Color(34, 139, 34)); 
        searchButton.setForeground(Color.BLACK);
        searchButton.addActionListener(e -> searchMembers(searchField.getText()));
        searchPanel.add(searchButton, BorderLayout.EAST);

        add(searchPanel, BorderLayout.NORTH);

        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        memberTable = new JTable();
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Email", "Role"}, 0);
        memberTable.setModel(tableModel);

        memberTable.setFont(new Font("Arial", Font.PLAIN, 20));
        memberTable.setRowHeight(30);
        memberTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 22));
        memberTable.getTableHeader().setForeground(Color.BLACK);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        for (int i = 0; i < memberTable.getColumnCount(); i++) {
            memberTable.getColumnModel().getColumn(i).setCellRenderer(leftRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(memberTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(tablePanel, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton updateButton = new JButton("Update");
        updateButton.setFont(new Font("Arial", Font.BOLD, 18));
        updateButton.setBackground(new Color(255, 140, 0)); 
        updateButton.setForeground(Color.BLACK);
        updateButton.addActionListener(e -> updateSelectedMember());
        buttonsPanel.add(updateButton);

        JButton deleteButton = new JButton("Delete");
        deleteButton.setFont(new Font("Arial", Font.BOLD, 18));
        deleteButton.setBackground(new Color(178, 34, 34));
        deleteButton.setForeground(Color.BLACK);
        deleteButton.addActionListener(e -> deleteSelectedMember());
        buttonsPanel.add(deleteButton);

        JButton backButton = new JButton("Back to Dashboard");
        backButton.setFont(new Font("Arial", Font.BOLD, 18));
        backButton.setBackground(new Color(30, 144, 255)); 
        backButton.setForeground(Color.BLACK);
        backButton.addActionListener(e -> {
            dispose();
            new AdminHomeFrame();
        });
        buttonsPanel.add(backButton);

        add(buttonsPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void searchMembers(String query) {
        try {
            URL url = new URI("http://localhost:8080/api/admin/members/search?query=" + query).toURL();
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
                List<Member> members = objectMapper.readValue(
                        result.toString(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Member.class)
                );

                updateTable(members);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to fetch members.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error occurred while searching for members.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable(List<Member> members) {
        tableModel.setRowCount(0); // Clear existing rows
        for (Member member : members) {
            tableModel.addRow(new Object[]{member.getId(), member.getName(), member.getEmail(), member.getRole()});
        }
    }

    private void updateSelectedMember() {
        int selectedRow = memberTable.getSelectedRow();
        if (selectedRow != -1) {
            Long memberId = (Long) tableModel.getValueAt(selectedRow, 0);
            String name = (String) tableModel.getValueAt(selectedRow, 1);
            String email = (String) tableModel.getValueAt(selectedRow, 2);
            String role = (String) tableModel.getValueAt(selectedRow, 3);
    
            try {
                Member updatedMember = new Member();
                updatedMember.setId(memberId);
                updatedMember.setName(name);
                updatedMember.setEmail(email);
                updatedMember.setRole(role);
    
                ObjectMapper objectMapper = new ObjectMapper();
                String json = objectMapper.writeValueAsString(updatedMember);
    
                URL url = new URI("http://localhost:8080/api/admin/members/update").toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
    
                conn.getOutputStream().write(json.getBytes());
    
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    JOptionPane.showMessageDialog(this, "Member updated successfully.");
                    // Re-fetch members using the existing query
                    String currentQuery = searchField.getText().trim();
                    searchMembers(currentQuery);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update member.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error occurred while updating member.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a member to update.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void deleteSelectedMember() {
        int selectedRow = memberTable.getSelectedRow();
        if (selectedRow != -1) {
            Long memberId = (Long) tableModel.getValueAt(selectedRow, 0);
            try {
                URL url = new URI("http://localhost:8080/api/admin/members/delete/" + memberId).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
    
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    JOptionPane.showMessageDialog(this, "Member deleted successfully.");
                    // Re-fetch members using the existing query
                    String currentQuery = searchField.getText().trim();
                    searchMembers(currentQuery);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete member.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error occurred while deleting member.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a member to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new UpdateDeleteMemberFrame();
    }
}