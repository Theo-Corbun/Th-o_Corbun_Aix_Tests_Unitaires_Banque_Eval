package fr.esgi.banque.business;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test d'exemple fourni pour vérifier que le projet est bien configuré.
 * À compléter (partie A).
 */
class CompteTest {

	@Test
	void shouldRejectZeroOrNegativeAmountWhenCrediting() {
		Compte compte = new Compte("FR001", "Alice", new BigDecimal("100.00"), new BigDecimal("200"));

		assertThatThrownBy(() -> compte.crediter(BigDecimal.ZERO))
				.isInstanceOf(IllegalArgumentException.class);

		assertThatThrownBy(() -> compte.crediter(new BigDecimal("-10.00")))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectZeroOrNegativeAmountWhenDebiting() {
		Compte compte = new Compte("FR001", "Alice", new BigDecimal("100.00"), new BigDecimal("200"));

		assertThatThrownBy(() -> compte.debiter(BigDecimal.ZERO))
				.isInstanceOf(IllegalArgumentException.class);

		assertThatThrownBy(() -> compte.debiter(new BigDecimal("-10.00")))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectDebitExceedingOverdraft() {
		Compte compte = new Compte("FR001", "Alice", new BigDecimal("100.00"), new BigDecimal("200.00"));

		assertThatThrownBy(() -> compte.debiter(new BigDecimal("350.00")))
				.isInstanceOf(SoldeInsuffisantException.class);
	}

	@Test
	void shouldTransferMoneySuccessfully() {
		Compte source = new Compte("FR001", "Alice", new BigDecimal("100.00"), new BigDecimal("200.00"));
		Compte destination = new Compte("FR002", "Bob", new BigDecimal("50.00"), new BigDecimal("200.00"));

		source.virer(new BigDecimal("30.00"), destination);

		assertThat(source.getSolde()).isEqualByComparingTo("70.00");
		assertThat(destination.getSolde()).isEqualByComparingTo("80.00");
	}

	@Test
	void shouldReturnTrueWhenAccountIsEmpty() {
		Compte compteZero = new Compte("FR001", "Alice", BigDecimal.ZERO, new BigDecimal("200.00"));
		Compte compteZeroDecimal = new Compte("FR002", "Bob", new BigDecimal("0.00"), new BigDecimal("200.00"));

		assertThat(compteZero.estVide()).isTrue();
		assertThat(compteZeroDecimal.estVide()).isTrue();
	}

	@Test
	void shouldReturnTrueWhenAccountIsAtOverdraft() {
		Compte compteNegatif = new Compte("FR001", "Alice", new BigDecimal("-10.00"), new BigDecimal("200.00"));

		assertThat(compteNegatif.estADecouvert()).isTrue();
	}
}