package com.librarysystem.controller;

import com.librarysystem.dto.AdminBooksDTO;
import com.librarysystem.dto.BookDTO;
import com.librarysystem.model.Book;
import com.librarysystem.model.Member;
import com.librarysystem.model.Reservation.ActionType;
import com.librarysystem.model.Review;
import com.librarysystem.dto.BookDetailsDTO;
import com.librarysystem.dto.BookDetailsDTO.ReviewDTO;
import com.librarysystem.dto.ReviewRequest;
import com.librarysystem.repository.BookRepository;
import com.librarysystem.repository.ReviewRepository;
import com.librarysystem.repository.ReservationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles book-related operations such as searching by title, author, or genre.
 */

@RestController
@RequestMapping("/api")
public class BookController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @GetMapping("/books")
    public ResponseEntity<List<BookDTO>> searchBooks(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "genre", required = false) String genre
    ) {      

        List<Book> books;

        // Search by query or filter by genre
        if (query != null && !query.trim().isEmpty()) {
            books = bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(query, query);
        } else if (genre != null && !genre.trim().isEmpty()) {
            books = bookRepository.findByGenreIgnoreCase(genre);
        } else {
            books = bookRepository.findAll();
        }

        // Map Book entities to BookDTO
        List<BookDTO> bookDTOs = books.stream()
                .map(book -> new BookDTO(book.getTitle(), book.getAuthor(), book.getGenre(), book.getCount()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(bookDTOs);
    }

    @PostMapping("/books/add")
    public ResponseEntity<String> addBook(@RequestBody Book book) {
        // Validate book fields
        if (book.getTitle() == null || book.getTitle().trim().isEmpty() ||
            book.getAuthor() == null || book.getAuthor().trim().isEmpty() ||
            book.getGenre() == null || book.getGenre().trim().isEmpty() ||
            book.getCount() <= 0) {
            return ResponseEntity.badRequest().body("All fields are required and count must be greater than zero.");
        }

        // Check for duplicate title (case insensitive)
        boolean exists = bookRepository.findByTitleContainingIgnoreCase(book.getTitle())
                .stream()
                .anyMatch(existingBook -> existingBook.getTitle().equalsIgnoreCase(book.getTitle()));

        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("A book with the same title already exists.");
        }

    // Save the book
    bookRepository.save(book);
    return ResponseEntity.ok("Book added successfully!");
    }

    @GetMapping("/admin/books/list")
    public ResponseEntity<List<AdminBooksDTO>> listAllBooksForAdmin() {
        List<Book> books = bookRepository.findAll();

        // Map Book entities to AdminBooksDTO
        List<AdminBooksDTO> adminBooksDTOs = books.stream()
                .map(book -> new AdminBooksDTO(
                        book.getId(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getGenre(),
                        book.getCount(),
                        book.getReservedCount(),
                        book.getBorrowedCount()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(adminBooksDTOs);
    }

        /**
     * Update a book's details.
     * 
     * @param updatedBook The book details to update
     * @return Success or error message
     */
    @PutMapping("/admin/books/update")
    public ResponseEntity<String> updateBook(@RequestBody Book updatedBook) {
        Book existingBook = bookRepository.findById(updatedBook.getId())
                .orElse(null);

        if (existingBook == null) {
            return ResponseEntity.badRequest().body("Book not found.");
        }

        // Update the book details
        existingBook.setTitle(updatedBook.getTitle());
        existingBook.setAuthor(updatedBook.getAuthor());
        existingBook.setGenre(updatedBook.getGenre());
        existingBook.setCount(updatedBook.getCount());

        bookRepository.save(existingBook);

        return ResponseEntity.ok("Book updated successfully.");
    }

        /**
     * Delete a book by ID.
     * 
     * @param id The book ID
     * @return Success or error message
     */
    @DeleteMapping("/admin/books/delete/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable Long id) {
        if (!bookRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("Book not found.");
        }

        bookRepository.deleteById(id);

        return ResponseEntity.ok("Book deleted successfully.");
    }

    @GetMapping("/admin/books/search")
    public ResponseEntity<List<AdminBooksDTO>> searchBooks(
            @RequestParam(value = "query", required = false) String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        List<Book> books = bookRepository.searchBooksByQuery(query);

        List<AdminBooksDTO> adminBooksDTOs = books.stream()
                .map(book -> new AdminBooksDTO(
                        book.getId(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getGenre(),
                        book.getCount(),
                        book.getReservedCount(),
                        book.getBorrowedCount()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(adminBooksDTOs);
    }

        /**
     * Fetch detailed information about a book.
     *
     * @param bookId The ID of the book
     * @return Book details including borrowed members and reviews
     */
    
     @GetMapping("/books/details/{id}")
     public ResponseEntity<BookDetailsDTO> getBookDetails(@PathVariable("id") Long bookId) {
         // Fetch the book
         Book book = bookRepository.findById(bookId)
                 .orElseThrow(() -> new IllegalArgumentException("Book not found"));
     
         // Fetch borrowed members and dates from Reservation
         List<BookDetailsDTO.BorrowedMemberDTO> borrowedMembers = reservationRepository.findByBooksId(bookId)
                 .stream()
                 .filter(reservation -> reservation.getActionType().equals(ActionType.BORROW))
                 .map(reservation -> new BookDetailsDTO.BorrowedMemberDTO(
                         reservation.getMember().getName(),
                         reservation.getBorrowedDate(),
                         reservation.getDueDate()
                 ))
                 .collect(Collectors.toList());
     
         // Fetch reviews
         List<BookDetailsDTO.ReviewDTO> reviews = reviewRepository.findByBookId(bookId)
                 .stream()
                 .map(review -> new BookDetailsDTO.ReviewDTO(
                         review.getMember().getName(),
                         review.getRating(),
                         review.getReviewText(),
                         review.getReviewDate()
                 ))
                 .collect(Collectors.toList());
     
         // Map to BookDetailsDTO
         BookDetailsDTO bookDetails = new BookDetailsDTO(
                 book.getId(),
                 book.getTitle(),
                 book.getAuthor(),
                 book.getGenre(),
                 book.getCount(),
                 book.getReservedCount(),
                 book.getBorrowedCount(),
                 borrowedMembers,
                 reviews
         );
     
         return ResponseEntity.ok(bookDetails);
    }
    @GetMapping("/books/search")
    public ResponseEntity<List<BookDetailsDTO>> searchBooksByMember(@RequestParam(value = "query", required = false) String query) {
        List<Book> books;
    
        if (query != null && !query.trim().isEmpty()) {
            books = bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrGenreContainingIgnoreCase(query, query, query);
        } else {
            books = bookRepository.findAll();
        }
    
        // Filter and map to BookDetailsDTO
        List<BookDetailsDTO> bookDetails = books.stream()
                .filter(book -> book.getCount() > 0) // Only include books available for reservation
                .map(book -> new BookDetailsDTO(
                        book.getId(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getGenre(),
                        book.getCount(),
                        book.getReservedCount(),
                        book.getBorrowedCount(),
                        null, // BorrowedMembers not required here
                        null  // Reviews not required here
                ))
                .collect(Collectors.toList());
    
        return ResponseEntity.ok(bookDetails);
    }
        /**
     * Submit a review for a book.
     */
    @PostMapping("/books/review")
    public ResponseEntity<String> addReview(@RequestBody ReviewRequest reviewRequest) {
    try {
        // Create references to Book and Member using their IDs
        Book book = new Book();
        book.setId(reviewRequest.getBookId());

        Member member = new Member();
        member.setId(reviewRequest.getMemberId());

        // Create and populate the Review entity
        Review review = new Review();
        review.setBook(book); // Use setBook
        review.setMember(member); // Use setMember
        review.setRating(reviewRequest.getRating());
        review.setReviewText(reviewRequest.getReviewText());
        review.setReviewDate(LocalDateTime.now());

        // Save the review
        reviewRepository.save(review);
        return ResponseEntity.ok("Review added successfully!");
    } catch (Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.badRequest().body("Error adding review: " + ex.getMessage());
    }
    }

    /**
     * Fetch all reviews for a specific book.
     */
    @GetMapping("/books/{id}/reviews")
    public ResponseEntity<List<ReviewDTO>> getBookReviews(@PathVariable("id") Long bookId) {
        List<ReviewDTO> reviews = reviewRepository.findByBookId(bookId)
                .stream()
                .map(review -> new ReviewDTO(
                        review.getMember().getName(),
                        review.getRating(),
                        review.getReviewText(),
                        review.getReviewDate()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(reviews);
    }
}
