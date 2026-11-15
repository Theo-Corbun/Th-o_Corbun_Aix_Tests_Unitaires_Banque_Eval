package fr.esgi.banque.service.impl;

import fr.esgi.banque.business.Virement;
import fr.esgi.banque.service.CompteRepository;
import fr.esgi.banque.service.NotificationService;

import java.math.BigDecimal;
import java.time.Clock;

public class VirementServiceImpl {

    private final CompteRepository repository;
    private final NotificationService notifications;
    private final Clock horloge;

    public VirementServiceImpl(CompteRepository repository, NotificationService notifications, Clock horloge) {
        this.repository = repository;
        this.notifications = notifications;
        this.horloge = horloge;
    }

    public Virement virer(String numeroSource, String numeroDestination, BigDecimal montant) {
        return null; // <--- RED V1 : Ne fait rien pour faire échouer le test V1
    }

}