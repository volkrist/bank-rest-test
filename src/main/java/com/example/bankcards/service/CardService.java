package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AppException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardNumberUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final byte[] cardEncryptionKey;

    @Transactional
    public CardResponse createForUser(Long userId, CardCreateRequest request, boolean isAdmin) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));
        return createCard(user, request);
    }

    @Transactional
    public CardResponse createCard(User user, CardCreateRequest request) {
        String digits = request.getNumber().replaceAll("\\D", "");
        if (digits.length() < 16 || digits.length() > 19) {
            throw new AppException("Card number must be 16-19 digits", HttpStatus.BAD_REQUEST);
        }
        String lastFour = CardNumberUtil.lastFour(digits);
        String encrypted = CardNumberUtil.encrypt(digits, cardEncryptionKey);
        LocalDate expiry = request.getExpiryDate();
        CardStatus status = expiry.isBefore(LocalDate.now()) ? CardStatus.EXPIRED : CardStatus.ACTIVE;
        Card card = Card.builder()
                .user(user)
                .encryptedNumber(encrypted)
                .lastFour(lastFour)
                .holderName(request.getHolderName())
                .expiryDate(expiry)
                .status(status)
                .balanceCents(0L)
                .build();
        card = cardRepository.save(card);
        return toResponse(card);
    }

    public CardResponse getById(Long cardId, Long currentUserId, boolean isAdmin) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new AppException("Card not found", HttpStatus.NOT_FOUND));
        if (!isAdmin && !card.getUser().getId().equals(currentUserId)) {
            throw new AppException("Access denied", HttpStatus.FORBIDDEN);
        }
        return toResponse(card);
    }

    public Page<CardResponse> getMyCards(Long userId, CardStatus statusFilter, Pageable pageable) {
        Page<Card> page = statusFilter == null
                ? cardRepository.findByUserId(userId, pageable)
                : cardRepository.findByUserIdAndStatus(userId, statusFilter, pageable);
        return page.map(this::toResponse);
    }

    public Page<CardResponse> getAllCardsForAdmin(Pageable pageable) {
        return cardRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public CardResponse setStatus(Long cardId, CardStatus status, Long currentUserId, boolean isAdmin) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new AppException("Card not found", HttpStatus.NOT_FOUND));
        if (!isAdmin && !card.getUser().getId().equals(currentUserId)) {
            throw new AppException("Access denied", HttpStatus.FORBIDDEN);
        }
        if (!isAdmin && status != CardStatus.BLOCKED) {
            throw new AppException("User can only request BLOCKED", HttpStatus.FORBIDDEN);
        }
        card.setStatus(status);
        card = cardRepository.save(card);
        return toResponse(card);
    }

    @Transactional
    public void deleteCard(Long cardId, Long currentUserId, boolean isAdmin) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new AppException("Card not found", HttpStatus.NOT_FOUND));
        if (!isAdmin) {
            throw new AppException("Only admin can delete cards", HttpStatus.FORBIDDEN);
        }
        cardRepository.delete(card);
    }

    /** Удаление карты по id (только ADMIN, вызывается из контроллера с @PreAuthorize). */
    @Transactional
    public void deleteCard(Long id) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new AppException("Card not found", HttpStatus.NOT_FOUND));
        cardRepository.delete(card);
    }

    public CardResponse toResponse(Card card) {
        return CardResponse.builder()
                .id(card.getId())
                .maskedNumber(CardNumberUtil.mask(card.getLastFour()))
                .holderName(card.getHolderName())
                .expiryDate(card.getExpiryDate())
                .status(card.getStatus())
                .balance(BigDecimal.valueOf(card.getBalanceCents(), 2))
                .createdAt(card.getCreatedAt())
                .build();
    }

    /** Обновить статус EXPIRED по дате (вызывать при необходимости). */
    @Transactional
    public void updateExpiredStatus(Card card) {
        if (card.getStatus() == CardStatus.ACTIVE && card.getExpiryDate().isBefore(LocalDate.now())) {
            card.setStatus(CardStatus.EXPIRED);
            cardRepository.save(card);
        }
    }
}
