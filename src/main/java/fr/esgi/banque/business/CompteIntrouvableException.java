package fr.esgi.banque.business;

// <--- CRÉÉ POUR V3 : Exception levée quand le compte source ou destination n'existe pas
public class CompteIntrouvableException extends RuntimeException {
    public CompteIntrouvableException(String message) {
        super(message);
    }
}