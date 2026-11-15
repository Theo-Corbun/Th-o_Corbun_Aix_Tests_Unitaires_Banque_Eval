package fr.esgi.banque.service.impl;

import fr.esgi.banque.business.Compte;
import fr.esgi.banque.business.CompteIntrouvableException;
import fr.esgi.banque.business.Virement;
import fr.esgi.banque.business.VirementRefuseException;
import fr.esgi.banque.service.CompteRepository;
import fr.esgi.banque.service.NotificationService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;

public class VirementServiceImpl {

    private static final BigDecimal SEUIL_ALERTE = new BigDecimal("10000.00"); // <--- AJOUTÉ POUR V6

    private final CompteRepository repository;
    private final NotificationService notifications;
    private final Clock horloge;

    public VirementServiceImpl(CompteRepository repository, NotificationService notifications, Clock horloge) {
        this.repository = repository;
        this.notifications = notifications;
        this.horloge = horloge;
    }

    public Virement virer(String numeroSource, String numeroDestination, BigDecimal montant) {
        // <--- V1 : Un montant null, nul ou négatif est refusé
        if (montant == null || montant.signum() <= 0) {
            throw new IllegalArgumentException("Le montant doit être strictement positif");
        }

        // <--- V2 : Un virement vers le même compte est refusé
        if (numeroSource != null && numeroSource.equals(numeroDestination)) {
            throw new IllegalArgumentException("Le compte source et le compte destination doivent être différents");
        }

        // <--- V7 : Aucun virement n'est accepté le dimanche
        LocalDate dateDuJour = LocalDate.now(horloge);
        if (dateDuJour.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new VirementRefuseException("Aucun virement n'est accepté le dimanche");
        }

        // <--- V3 : Si l'un des deux comptes n'existe pas -> CompteIntrouvableException
        Compte source = repository.trouver(numeroSource)
                .orElseThrow(() -> new CompteIntrouvableException("Compte source introuvable : " + numeroSource));
        Compte destination = repository.trouver(numeroDestination)
                .orElseThrow(() -> new CompteIntrouvableException("Compte destination introuvable : " + numeroDestination));

        // <--- V4 & V5 : Débit (lève SoldeInsuffisantException si insuffisant avant toute sauvegarde), crédit et sauvegardes
        source.debiter(montant);
        destination.crediter(montant);
        repository.sauvegarder(source);
        repository.sauvegarder(destination);

        // <--- V6 : Alerte au titulaire du compte source si montant strictement supérieur à 10 000 €
        if (montant.compareTo(SEUIL_ALERTE) > 0) {
            notifications.alerter(source.getTitulaire(), "Virement important de " + montant.toPlainString() + " €"); // <--- CORRIGÉ : String en 2e paramètre
        }

        // <--- V4 : Renvoie le Virement avec la date du jour fournie par l'horloge
        return new Virement(numeroSource, numeroDestination, montant, dateDuJour);
    }

}