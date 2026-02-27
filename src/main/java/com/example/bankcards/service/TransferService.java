package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.exception.AppException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public Transaction transfer(Long fromCardId, Long toCardId, long amountCents, Long currentUserId) {
        if (amountCents <= 0) {
            throw new AppException("Amount must be positive", HttpStatus.BAD_REQUEST);
        }
        if (fromCardId.equals(toCardId)) {
            throw new AppException("Source and target card must be different", HttpStatus.BAD_REQUEST);
        }
        Card fromCard = cardRepository.findByIdForUpdate(fromCardId).orElseThrow(() -> new AppException("Card not found", HttpStatus.NOT_FOUND));
        Card toCard = cardRepository.findByIdForUpdate(toCardId).orElseThrow(() -> new AppException("Card not found", HttpStatus.NOT_FOUND));

        if (!fromCard.getUser().getId().equals(currentUserId) || !toCard.getUser().getId().equals(currentUserId)) {
            throw new AppException("Both cards must belong to you", HttpStatus.FORBIDDEN);
        }

        ensureCardOperable(fromCard);
        ensureCardOperable(toCard);

        if (fromCard.getBalanceCents() < amountCents) {
            throw new AppException("Insufficient balance", HttpStatus.BAD_REQUEST);
        }

        fromCard.setBalanceCents(fromCard.getBalanceCents() - amountCents);
        toCard.setBalanceCents(toCard.getBalanceCents() + amountCents);
        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        Transaction tx = Transaction.builder()
                .fromCard(fromCard)
                .toCard(toCard)
                .amountCents(amountCents)
                .build();
        return transactionRepository.save(tx);
    }

    private void ensureCardOperable(Card card) {
        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new AppException("Card is blocked", HttpStatus.BAD_REQUEST);
        }
        if (card.getStatus() == CardStatus.EXPIRED || card.getExpiryDate().isBefore(LocalDate.now())) {
            throw new AppException("Card is expired", HttpStatus.BAD_REQUEST);
        }
    }
}
