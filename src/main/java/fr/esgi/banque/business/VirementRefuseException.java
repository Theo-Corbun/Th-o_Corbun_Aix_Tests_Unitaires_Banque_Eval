package fr.esgi.banque.business;

// <--- CRÉÉ POUR V7 : Exception levée quand un virement est tenté le dimanche
public class VirementRefuseException extends RuntimeException {
    public VirementRefuseException(String message) {
        super(message);
    }
}