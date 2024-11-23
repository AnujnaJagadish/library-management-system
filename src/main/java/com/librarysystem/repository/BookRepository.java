package com.librarysystem.repository;

import com.librarysystem.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    // Custom query to find books by title
    List<Book> findByTitleContainingIgnoreCase(String title);

    // Custom query to find books by author
    List<Book> findByAuthorContainingIgnoreCase(String author);

    // Custom query to find books by genre
    List<Book> findByGenre(String genre);

    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author);

    List<Book> findByGenreIgnoreCase(String genre);

        @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "CAST(b.id AS string) LIKE CONCAT('%', :query, '%')")
    List<Book> searchBooksByQuery(@Param("query") String query);


    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrGenreContainingIgnoreCase(
        String title, String author, String genre
);

}
