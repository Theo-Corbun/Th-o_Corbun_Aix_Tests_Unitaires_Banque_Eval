package fr.esgi.banque.business;

import java.math.BigDecimal;
import java.time.LocalDate;

// <--- CRÉÉ POUR LA PARTIE B : Record demandé par le contrat
public record Virement(String numeroSource, String numeroDestination, BigDecimal montant, LocalDate date) {
}