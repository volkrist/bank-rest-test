package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByUserId(Long userId);

    Page<Card> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT c FROM Card c WHERE c.user.id = :userId AND (:status IS NULL OR c.status = :status)")
    Page<Card> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") CardStatus status, Pageable pageable);

    @Query("SELECT c FROM Card c WHERE c.id = :id AND c.user.id = :userId")
    Optional<Card> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Card c WHERE c.id = :id")
    Optional<Card> findByIdForUpdate(@Param("id") Long id);

    boolean existsByIdAndUserId(Long cardId, Long userId);
}
