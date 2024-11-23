package com.librarysystem.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.librarysystem.dto.BookDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

/* Class is related to access books available in library catalog without logging in*/

public class GuestPageFrame extends JFrame {
    private JTextField searchField;
    private JPanel resultsPanel;

    public GuestPageFrame() {
        setTitle("Library Management System - Guest Page");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        BackgroundPanel backgroundPanel = new BackgroundPanel("images/add_member.jpg");
        backgroundPanel.setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel headerLabel = new JLabel("Library Search", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 36));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.NORTH);

        JLabel subHeaderLabel = new JLabel("Search books by title, author, or browse by genre", SwingConstants.CENTER);
        subHeaderLabel.setFont(new Font("Arial", Font.BOLD, 18));
        subHeaderLabel.setForeground(Color.WHITE);
        headerPanel.add(subHeaderLabel, BorderLayout.SOUTH);

        backgroundPanel.add(headerPanel, BorderLayout.NORTH);

        // Main Content Panel
        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setOpaque(false);
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));
        mainContentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        backgroundPanel.add(mainContentPanel, BorderLayout.CENTER);

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        searchPanel.setOpaque(false);
        searchPanel.setBorder(new EmptyBorder(10, 0, 20, 0));

        searchField = new JTextField(30);
        searchField.setFont(new Font("Arial", Font.PLAIN, 18));
        searchField.setBorder(new LineBorder(Color.BLACK, 1));
        searchPanel.add(searchField);

        JButton searchButton = new JButton("Search");
        searchButton.setFont(new Font("Arial", Font.BOLD, 18));
        searchButton.addActionListener(new SearchActionListener());
        searchPanel.add(searchButton);

        mainContentPanel.add(searchPanel);

        JPanel genrePanel = new JPanel(new GridLayout(2, 3, 20, 20));
        genrePanel.setOpaque(false); 
        genrePanel.setBorder(new EmptyBorder(10, 0, 20, 0));

        String[] genres = {"Fiction", "Science", "History", "Biography", "Education", "Fantasy"};
        for (String genre : genres) {
            JButton genreButton = new JButton(genre);
            genreButton.setFont(new Font("Arial", Font.BOLD, 18));
            genreButton.setBorder(new LineBorder(Color.BLACK, 3)); 
            genreButton.setBackground(Color.WHITE); 
            genreButton.setOpaque(true); 
            genreButton.setContentAreaFilled(true);
            genreButton.addActionListener(new GenreActionListener(genre));
            genrePanel.add(genreButton);
        }

        mainContentPanel.add(genrePanel);

        JPanel resultsContainer = new JPanel(new BorderLayout());
        resultsContainer.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
        resultsContainer.setPreferredSize(new Dimension(600, 350));

        JLabel resultsLabel = new JLabel("Search Results", SwingConstants.CENTER);
        resultsLabel.setFont(new Font("Arial", Font.BOLD, 24));
        resultsContainer.add(resultsLabel, BorderLayout.NORTH);

        resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(resultsPanel);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        resultsContainer.add(scrollPane, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        centerPanel.setOpaque(false);
        centerPanel.add(resultsContainer);

        mainContentPanel.add(centerPanel);

        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        navigationPanel.setOpaque(false); 
        navigationPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        JButton loginButton = new JButton("Back to Login Page");
        loginButton.setFont(new Font("Arial", Font.PLAIN, 16));
        loginButton.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });
        navigationPanel.add(loginButton);

        JButton registerButton = new JButton("Register as a Member");
        registerButton.setFont(new Font("Arial", Font.PLAIN, 16));
        registerButton.addActionListener(e -> {
            dispose();
            new RegistrationFrame();
        });
        navigationPanel.add(registerButton);

        mainContentPanel.add(navigationPanel);

        setContentPane(backgroundPanel);
        setVisible(true);
    }

    private void displayResults(List<BookDTO> books) {
        resultsPanel.removeAll(); 
    
        if (books.isEmpty()) {
            JLabel noResultsLabel = new JLabel("No books found.", SwingConstants.CENTER);
            noResultsLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            resultsPanel.add(noResultsLabel);
        } else {
            for (BookDTO book : books) {
                JPanel bookPanel = new JPanel(new GridLayout(4, 1)); 
                bookPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
                bookPanel.setBackground(Color.WHITE);
    
                JLabel titleLabel = new JLabel("Title: " + book.getTitle());
                titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
                bookPanel.add(titleLabel);
    
                JLabel authorLabel = new JLabel("Author: " + book.getAuthor());
                authorLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                bookPanel.add(authorLabel);
    
                JLabel genreLabel = new JLabel("Genre: " + book.getGenre());
                genreLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                bookPanel.add(genreLabel);
        
                bookPanel.setMaximumSize(new Dimension(600, 100));
                resultsPanel.add(bookPanel);
                resultsPanel.add(Box.createRigidArea(new Dimension(0, 10))); 
            }
        }
    
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }
    

    private class SearchActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String query = searchField.getText().trim();
            if (query.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please enter a search term.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                URL url = new URI("http://localhost:8080/api/books?query=" + 
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
                    List<BookDTO> books = objectMapper.readValue(result.toString(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, BookDTO.class));
                    displayResults(books);
                } else {
                    JOptionPane.showMessageDialog(null, "Error fetching results. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error connecting to server.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class GenreActionListener implements ActionListener {
        private final String genre;

        public GenreActionListener(String genre) {
            this.genre = genre;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                URL url = new URI("http://localhost:8080/api/books?genre=" + genre).toURL();
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
                    List<BookDTO> books = objectMapper.readValue(result.toString(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, BookDTO.class));
                    displayResults(books);
                } else {
                    JOptionPane.showMessageDialog(null, "Error fetching results. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error connecting to server.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        new GuestPageFrame();
    }

    private static class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel(String imagePath) {
            try {
                backgroundImage = new ImageIcon(getClass().getClassLoader().getResource(imagePath)).getImage();
            } catch (Exception e) {
                e.printStackTrace();
                backgroundImage = null;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }
}
