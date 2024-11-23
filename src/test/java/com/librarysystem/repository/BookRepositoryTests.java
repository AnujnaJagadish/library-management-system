package com.librarysystem.repository;

import com.librarysystem.model.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookRepositoryTests {

    @Autowired
    private BookRepository bookRepository;

    @Test
    void testDatabaseConnectionAndInsert() {
        // Create a new book
        Book book = new Book();
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setGenre("Fiction");
        book.setCount(10);
        book.setReservedCount(0);
        book.setBorrowedCount(0);

        // Save the book to the database
        bookRepository.save(book);

        // Retrieve all books and verify
        List<Book> books = bookRepository.findAll();
        assertThat(books).isNotEmpty(); // Check that books are not empty
        assertThat(books.get(0).getTitle()).isEqualTo("Test Book"); // Check inserted book
    }
}
