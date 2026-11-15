package fr.esgi.banque.service.impl;

import fr.esgi.banque.service.CompteRepository;
import fr.esgi.banque.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class VirementServiceImplTest {

    @Mock
    private CompteRepository repository;

    @Mock
    private NotificationService notifications;

    private Clock horloge;
    private VirementServiceImpl service;

    @BeforeEach
    void setUp() {
        // Horloge fixée à un lundi (16/11/2026)
        horloge = Clock.fixed(Instant.parse("2026-11-16T10:00:00Z"), ZoneId.of("Europe/Paris"));
        service = new VirementServiceImpl(repository, notifications, horloge);
    }

    @Test
    void shouldRejectNullAmount() { // <--- TEST V1 : montant null
        assertThatThrownBy(() -> service.virer("FR001", "FR002", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-0.01", "-100.00"})
    void shouldRejectZeroOrNegativeAmount(String montantInvalide) { // <--- TEST V1 : montant nul ou négatif
        assertThatThrownBy(() -> service.virer("FR001", "FR002", new BigDecimal(montantInvalide)))
                .isInstanceOf(IllegalArgumentException.class);
    }

}