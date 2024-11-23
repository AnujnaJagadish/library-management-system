package com.librarysystem.repository;

import com.librarysystem.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Fetch all reviews for a specific book
    List<Review> findByBookId(Long bookId);
}

