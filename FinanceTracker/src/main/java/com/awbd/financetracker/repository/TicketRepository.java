package com.awbd.financetracker.repository;

import com.awbd.financetracker.entity.Ticket;
import com.awbd.financetracker.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("SELECT DISTINCT t FROM Ticket t JOIN FETCH t.submittedBy WHERE t.submittedBy.id = :userId ORDER BY t.createdAt DESC")
    List<Ticket> findBySubmittedByIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT DISTINCT t FROM Ticket t JOIN FETCH t.submittedBy ORDER BY t.createdAt DESC")
    List<Ticket> findAllByOrderByCreatedAtDesc();

    @Query("SELECT DISTINCT t FROM Ticket t JOIN FETCH t.submittedBy WHERE t.status = :status ORDER BY t.createdAt DESC")
    List<Ticket> findByStatusOrderByCreatedAtDesc(@Param("status") TicketStatus status);

    @Query("SELECT DISTINCT t FROM Ticket t " +
           "LEFT JOIN FETCH t.replies r " +
           "LEFT JOIN FETCH r.author " +
           "LEFT JOIN FETCH t.submittedBy " +
           "LEFT JOIN FETCH t.resolvedBy " +
           "WHERE t.id = :id")
    Optional<Ticket> findByIdWithReplies(@Param("id") Long id);
}
