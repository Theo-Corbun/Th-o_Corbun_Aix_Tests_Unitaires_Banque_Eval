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

## Partie B : Suivi du TDD
- [x] V1 : Rejet d'un montant null, nul ou négatif (`IllegalArgumentException`)
- [ ] V2
- [ ] V3
- [ ] V4
- [ ] V5
- [ ] V6
- [ ] V7