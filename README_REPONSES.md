# Suivi - TP Tests Unitaires et TDD (Banque)

- **Nom** : CORBUN
- **Prénom** : Théo
- **Campus** : Aix-en-Provence (ESGI)

## Partie A : Défauts trouvés dans l'existant

| Règle | Description du défaut | Nom du test associé |
|-------|------------------------|---------------------|
| C3 | `crediter` autorisait un montant nul (0) au lieu de l'interdire[cite: 1] | `shouldRejectZeroOrNegativeAmountWhenCrediting` |
| C4 | `debiter` gère déjà correctement le rejet des montants nuls ou négatifs[cite: 1] | `shouldRejectZeroOrNegativeAmountWhenDebiting` |
| C5 | `debiter` ne vérifiait pas correctement la limite du découvert autorisé | `shouldRejectDebitExceedingOverdraft` |
| C6-C7 | Ajout de la méthode `virer` manquante dans la classe `Compte` | `shouldTransferMoneySuccessfully` |

## Partie B : Suivi du TDD
- [ ] V1
- [ ] V2
- [ ] V3
- [ ] V4
- [ ] V5
- [ ] V6
- [ ] V7