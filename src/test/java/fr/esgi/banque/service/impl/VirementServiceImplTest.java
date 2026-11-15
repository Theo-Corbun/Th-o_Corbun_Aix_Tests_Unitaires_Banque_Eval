package fr.esgi.banque.service.impl;

import fr.esgi.banque.business.Compte;
import fr.esgi.banque.business.CompteIntrouvableException;
import fr.esgi.banque.business.SoldeInsuffisantException;
import fr.esgi.banque.business.Virement;
import fr.esgi.banque.business.VirementRefuseException;
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
        // Horloge fixée au lundi 16 novembre 2026
        horloge = Clock.fixed(Instant.parse("2026-11-16T10:00:00Z"), ZoneId.of("Europe/Paris"));
        service = new VirementServiceImpl(repository, notifications, horloge);
    }

    // <--- V1 : Montant null refusé
    @Test
    void shouldRejectNullAmount() {
        assertThatThrownBy(() -> service.virer("FR001", "FR002", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // <--- V1 : Montant nul ou négatif refusé
    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-0.01", "-100.00"})
    void shouldRejectZeroOrNegativeAmount(String montantInvalide) {
        assertThatThrownBy(() -> service.virer("FR001", "FR002", new BigDecimal(montantInvalide)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // <--- V2 : Virement vers le même compte refusé
    @Test
    void shouldRejectSameAccountTransfer() {
        assertThatThrownBy(() -> service.virer("FR001", "FR001", new BigDecimal("50.00")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // <--- V3 : Compte source introuvable
    @Test
    void shouldThrowExceptionWhenSourceAccountNotFound() {
        when(repository.trouver("FR999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.virer("FR999", "FR002", new BigDecimal("50.00")))
                .isInstanceOf(CompteIntrouvableException.class);
    }

    // <--- V3 : Compte destination introuvable
    @Test
    void shouldThrowExceptionWhenDestinationAccountNotFound() {
        Compte source = new Compte("FR001", "Alice", new BigDecimal("100.00"), BigDecimal.ZERO);
        when(repository.trouver("FR001")).thenReturn(Optional.of(source));
        when(repository.trouver("FR999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.virer("FR001", "FR999", new BigDecimal("50.00")))
                .isInstanceOf(CompteIntrouvableException.class);
    }

    // <--- V4 : Cas nominal (débit, crédit, sauvegardes et retour Virement avec date du jour)
    @Test
    void shouldTransferMoneyAndSaveAccountsInNominalCase() {
        Compte source = new Compte("FR001", "Alice", new BigDecimal("100.00"), BigDecimal.ZERO);
        Compte destination = new Compte("FR002", "Bob", new BigDecimal("50.00"), BigDecimal.ZERO);
        when(repository.trouver("FR001")).thenReturn(Optional.of(source));
        when(repository.trouver("FR002")).thenReturn(Optional.of(destination));

        Virement resultat = service.virer("FR001", "FR002", new BigDecimal("30.00"));

        assertThat(source.getSolde()).isEqualByComparingTo("70.00");
        assertThat(destination.getSolde()).isEqualByComparingTo("80.00");
        verify(repository).sauvegarder(source);
        verify(repository).sauvegarder(destination);
        assertThat(resultat).isNotNull();
        assertThat(resultat.numeroSource()).isEqualTo("FR001");
        assertThat(resultat.numeroDestination()).isEqualTo("FR002");
        assertThat(resultat.montant()).isEqualByComparingTo("30.00");
        assertThat(resultat.date()).isEqualTo(LocalDate.of(2026, 11, 16));
    }

    // <--- V5 : Solde insuffisant remonte SoldeInsuffisantException et ne sauvegarde rien
    @Test
    void shouldPropagateInsufficientBalanceAndNeverSaveAccounts() {
        Compte source = new Compte("FR001", "Alice", new BigDecimal("50.00"), BigDecimal.ZERO);
        Compte destination = new Compte("FR002", "Bob", new BigDecimal("50.00"), BigDecimal.ZERO);
        when(repository.trouver("FR001")).thenReturn(Optional.of(source));
        when(repository.trouver("FR002")).thenReturn(Optional.of(destination));

        assertThatThrownBy(() -> service.virer("FR001", "FR002", new BigDecimal("100.00")))
                .isInstanceOf(SoldeInsuffisantException.class);

        verify(repository, never()).sauvegarder(any());
    }

    // <--- V6 : Valeur limite 10 000,00 € -> pas d'alerte
    @Test
    void shouldNotAlertWhenAmountIsExactly10000() {
        Compte source = new Compte("FR001", "Alice", new BigDecimal("20000.00"), BigDecimal.ZERO);
        Compte destination = new Compte("FR002", "Bob", new BigDecimal("50.00"), BigDecimal.ZERO);
        when(repository.trouver("FR001")).thenReturn(Optional.of(source));
        when(repository.trouver("FR002")).thenReturn(Optional.of(destination));

        service.virer("FR001", "FR002", new BigDecimal("10000.00"));

        verifyNoInteractions(notifications);
    }

    // <--- V6 : Valeur limite 10 000,01 € -> alerte déclenchée au titulaire source
    @Test
    void shouldAlertWhenAmountExceeds10000() {
        Compte source = new Compte("FR001", "Alice", new BigDecimal("20000.00"), BigDecimal.ZERO);
        Compte destination = new Compte("FR002", "Bob", new BigDecimal("50.00"), BigDecimal.ZERO);
        when(repository.trouver("FR001")).thenReturn(Optional.of(source));
        when(repository.trouver("FR002")).thenReturn(Optional.of(destination));

        service.virer("FR001", "FR002", new BigDecimal("10000.01"));

        verify(notifications).alerter(eq("Alice"), anyString()); // <--- CORRIGÉ : Vérifie les 2 paramètres String
    }

    // <--- V7 : Virement refusé le dimanche et aucun compte sauvegardé
    @Test
    void shouldRejectTransferOnSundayAndNeverSaveAccounts() {
        // Dimanche 15 novembre 2026
        Clock horlogeDimanche = Clock.fixed(Instant.parse("2026-11-15T10:00:00Z"), ZoneId.of("Europe/Paris"));
        VirementServiceImpl serviceDimanche = new VirementServiceImpl(repository, notifications, horlogeDimanche);

        assertThatThrownBy(() -> serviceDimanche.virer("FR001", "FR002", new BigDecimal("50.00")))
                .isInstanceOf(VirementRefuseException.class);

        verify(repository, never()).sauvegarder(any());
    }

}