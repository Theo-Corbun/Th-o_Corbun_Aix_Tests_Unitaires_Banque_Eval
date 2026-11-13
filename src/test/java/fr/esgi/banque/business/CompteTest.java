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

}

