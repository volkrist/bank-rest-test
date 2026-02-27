package com.example.bankcards;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AppException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(classes = BankRestApplication.class)
class TransferServiceIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("card.encryption-key", () -> "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=");
    }

    @Autowired
    private TransferService transferService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    private User u;
    private Card from;
    private Card to;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        cardRepository.deleteAll();
        userRepository.deleteAll();

        u = new User();
        u.setUsername("u1");
        u.setPasswordHash("x");
        u.setRole(Role.USER);
        u = userRepository.save(u);

        from = new Card();
        from.setUser(u);
        from.setStatus(CardStatus.ACTIVE);
        from.setBalanceCents(500);
        from.setEncryptedNumber("dummy");
        from.setLastFour("1111");
        from.setHolderName("Holder From");
        from.setExpiryDate(LocalDate.now().plusYears(1));
        from = cardRepository.save(from);

        to = new Card();
        to.setUser(u);
        to.setStatus(CardStatus.ACTIVE);
        to.setBalanceCents(0);
        to.setEncryptedNumber("dummy2");
        to.setLastFour("2222");
        to.setHolderName("Holder To");
        to.setExpiryDate(LocalDate.now().plusYears(1));
        to = cardRepository.save(to);
    }

    @Test
    void transfer_success_updates_balances() {
        Transaction tx = transferService.transfer(from.getId(), to.getId(), 100, u.getId());

        assertNotNull(tx.getId());
        assertEquals(100, tx.getAmountCents());

        Card fromAfter = cardRepository.findById(from.getId()).orElseThrow();
        Card toAfter = cardRepository.findById(to.getId()).orElseThrow();

        assertEquals(400, fromAfter.getBalanceCents());
        assertEquals(100, toAfter.getBalanceCents());
    }

    @Test
    void transfer_insufficientFunds_returnsError_andBalancesUnchanged() {
        AppException ex = assertThrows(AppException.class, () ->
                transferService.transfer(from.getId(), to.getId(), 10_000, u.getId())
        );
        assertTrue(ex.getMessage().contains("Insufficient") || ex.getMessage().contains("balance"));

        Card fromAfter = cardRepository.findById(from.getId()).orElseThrow();
        Card toAfter = cardRepository.findById(to.getId()).orElseThrow();

        assertEquals(500, fromAfter.getBalanceCents());
        assertEquals(0, toAfter.getBalanceCents());
    }

    @Test
    void transfer_atomicRollback_whenErrorOccurs_midway() {
        to.setStatus(CardStatus.BLOCKED);
        cardRepository.save(to);

        assertThrows(AppException.class, () ->
                transferService.transfer(from.getId(), to.getId(), 100, u.getId())
        );

        Card fromAfter = cardRepository.findById(from.getId()).orElseThrow();
        Card toAfter = cardRepository.findById(to.getId()).orElseThrow();

        assertEquals(500, fromAfter.getBalanceCents());
        assertEquals(0, toAfter.getBalanceCents());
    }
}
