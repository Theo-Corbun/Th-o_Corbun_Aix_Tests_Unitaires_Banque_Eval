# Suivi - TP Tests Unitaires et TDD (Banque)

- **Nom** : CORBUN
- **Prénom** : Théo
- **Campus** : Aix-en-Provence (ESGI)

## Partie A : Défauts trouvés dans l'existant

| Règle | Description du défaut | Nom du test associé |
|-------|------------------------|---------------------|
| C3 | `crediter` autorisait un montant nul (0) au lieu de l'interdire | `shouldRejectZeroOrNegativeAmountWhenCrediting` |
| C4 | Aucun défaut (`debiter` rejetait déjà les montants nuls ou négatifs) | `shouldRejectZeroOrNegativeAmountWhenDebiting` |
| C5 | `debiter` ne vérifiait pas correctement la limite du découvert autorisé | `shouldRejectDebitExceedingOverdraft` |
| C6 | `estVide()` utilisait `equals` au lieu de `compareTo` pour tester le solde à zéro | `shouldReturnTrueWhenAccountIsEmpty` |
| C7 | Vérification de la méthode `estADecouvert()` pour un solde négatif | `shouldReturnTrueWhenAccountIsAtOverdraft` |
| F1 | Aucun défaut (âge >= 18 et solde moyen non null déjà vérifiés) | `shouldRejectUnderageOrNullSoldeMoyen` |
| F2 | Aucun défaut (frais de base à 2,00 € déjà définis, ligne remontée avant F3/F4) | `shouldCalculateCorrectFees` |
| F3 | L'âge limite utilisait `<= 26` au lieu de `< 26` | `shouldCalculateCorrectFees` |
| F4 | Le seuil de gratuité utilisait `> 1000` au lieu de `>= 1000` | `shouldCalculateCorrectFees` |
| F5 | Les `return BigDecimal.ZERO` anticipés de F3 et F4 ignoraient le supplément de 5,00 € de la carte premium | `shouldCalculateCorrectFees` |

## Partie B : Suivi du TDD (`VirementServiceImpl`)

- [x] **V1** : Rejet d'un montant null, nul ou négatif (`IllegalArgumentException`)
- [x] **V2** : Rejet d'un virement vers le même compte (`IllegalArgumentException`)
- [x] **V3** : Rejet si l'un des deux comptes est introuvable (`CompteIntrouvableException`)
- [x] **V4** : Cas nominal (débit source, crédit destination, double sauvegarde et retour du `Virement` avec `LocalDate.now(horloge)`)
- [x] **V5** : Propagation de `SoldeInsuffisantException` et vérification `verify(repository, never()).sauvegarder(any())`
- [x] **V6** : Alerte déclenchée (`notifications.alerter`) uniquement au-delà de 10 000 € (tests aux limites `10000.00` et `10000.01`)
- [x] **V7** : Rejet des virements le dimanche (`VirementRefuseException`) et aucune sauvegarde effectuée

### Journal d'exécution et résolution des erreurs (Partie B)

| Heure | Étape | État / Erreur rencontrée | Action corrective appliquée |
|-------|-------|--------------------------|-----------------------------|
| **18h58** | Mise en place & V1 | Erreur de package : `VirementServiceImplTest` placé initialement dans `fr.esgi.banque.business.impl` au lieu de `fr.esgi.banque.service.impl` | Déplacement de la classe de test dans le package `fr.esgi.banque.service.impl` et création du record `Virement`. |
| **19h06** | Cycle V1 (Red / Green) | **RED** : `shouldRejectNullAmount` et `shouldRejectZeroOrNegativeAmount` échouent (`virer` renvoie `null`). | **GREEN** : Ajout de la validation `if (montant == null \|\| montant.signum() <= 0)` levant `IllegalArgumentException`. |
| **19h15** | Cycle V2 (Red) | `IllegalStateException: Could not initialize plugin MockMaker` / `Unknown Java version` au lancement de Mockito avec Temurin JDK 25. | Passage du SDK du projet sur **Eclipse Temurin 17.0.18** dans Project Structure pour assurer la compatibilité ByteBuddy/Mockito sans modifier le `pom.xml`. |
| **19h19** | Cycle V2 (Green) | **RED** : `shouldRejectSameAccountTransfer` échoue sur `virer("FR001", "FR001", 50.00)`. | **GREEN** : Ajout de la vérification `numeroSource.equals(numeroDestination)` levant `IllegalArgumentException`. |
| **19h26** | Cycle V3 (Red / Green) | **RED** : Absence d'exception lorsque `repository.trouver(...)` renvoie `Optional.empty()`. | **GREEN** : Création de `CompteIntrouvableException` et utilisation de `.orElseThrow(...)` sur la recherche des comptes source et destination. |
| **19h35** | Cycle V4 (Red / Green) | **RED** : Échec de `shouldTransferMoneyAndSaveAccountsInNominalCase` (soldes inchangés et `Virement` null). | **GREEN** : Appel de `source.debiter`, `destination.crediter`, `repository.sauvegarder` sur les deux comptes et retour de `new Virement(..., LocalDate.now(horloge))`. |
| **19h40** | Cycle V5 (Red / Green) | Vérification du comportement en cas de solde insuffisant (`shouldPropagateInsufficientBalanceAndNeverSaveAccounts`). | **GREEN** : `source.debiter(montant)` lève `SoldeInsuffisantException` avant les appels à `repository.sauvegarder(...)` ; validé avec `verify(repository, never()).sauvegarder(any())`. |
| **19h46** | Cycle V6 (Red / Green) | **RED** : `shouldAlertWhenAmountExceeds10000` échoue (`Wanted but not invoked: notifications.alerter(...)`). | **GREEN** : Ajout de la constante `SEUIL_ALERTE` (`10000.00`) et appel de `notifications.alerter(source.getTitulaire(), montant)` si `montant.compareTo(SEUIL_ALERTE) > 0`. |
| **19h51** | Cycle V7 (Red / Green) | **RED** : `shouldRejectTransferOnSundayAndNeverSaveAccounts` échoue avec une horloge fixée au dimanche 15/11/2026. | **GREEN** : Création de `VirementRefuseException` et vérification `LocalDate.now(horloge).getDayOfWeek() == DayOfWeek.SUNDAY` avant tout traitement. |