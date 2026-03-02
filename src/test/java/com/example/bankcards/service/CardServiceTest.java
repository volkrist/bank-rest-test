package com.example.bankcards.service;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AppException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    private CardService cardService;

    private static final byte[] TEST_ENCRYPTION_KEY = new byte[32];

    @BeforeEach
    void setUp() {
        cardService = new CardService(cardRepository, userRepository, TEST_ENCRYPTION_KEY);
    }

    @Test
    void getById_whenCardExistsAndUserOwnsIt_returnsCardResponse() {
        Long cardId = 1L;
        Long userId = 10L;
        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");

        Card card = new Card();
        card.setId(cardId);
        card.setUser(user);
        card.setLastFour("1234");
        card.setHolderName("Test Holder");
        card.setExpiryDate(LocalDate.now().plusYears(1));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalanceCents(1000L);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        CardResponse response = cardService.getById(cardId, userId, false);

        assertNotNull(response);
        assertEquals(cardId, response.getId());
        assertEquals("**** **** **** 1234", response.getMaskedNumber());
        assertEquals("Test Holder", response.getHolderName());
        assertEquals(CardStatus.ACTIVE, response.getStatus());
        verify(cardRepository).findById(cardId);
    }

    @Test
    void getById_whenCardNotFound_throwsAppException() {
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () ->
                cardService.getById(999L, 10L, false)
        );

        assertTrue(ex.getMessage().contains("not found"));
        verify(cardRepository).findById(999L);
    }
}
