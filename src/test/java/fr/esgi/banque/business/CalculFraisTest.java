package fr.esgi.banque.business;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalculFraisTest {

    @Test
    void shouldRejectUnderageOrNullSoldeMoyen() {
        CalculFrais calculFrais = new CalculFrais();

        assertThatThrownBy(() -> calculFrais.fraisMensuels(17, new BigDecimal("500.00"), false))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> calculFrais.fraisMensuels(20, null, false))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource({
            "30, 500.00, false, 2.00",   // F2 : Standard
            "25, 500.00, false, 0.00",   // F3 : Moins de 26 ans -> offert
            "26, 500.00, false, 2.00",   // F3 : Limite exacte 26 ans -> payant
            "30, 1000.00, false, 0.00",  // F4 : Limite exacte 1000 € -> offert
            "20, 500.00, true, 5.00",    // F5 : Jeune + carte premium -> 5.00 €
            "30, 1000.00, true, 5.00",   // F5 : Solde >= 1000 + carte premium -> 5.00 €
            "30, 500.00, true, 7.00"     // F2 + F5 : Standard + carte premium -> 7.00 €
    })
    void shouldCalculateCorrectFees(int age, String soldeMoyenStr, boolean cartePremium, String expectedFrais) {
        CalculFrais calculFrais = new CalculFrais();

        BigDecimal frais = calculFrais.fraisMensuels(age, new BigDecimal(soldeMoyenStr), cartePremium);

        assertThat(frais).isEqualByComparingTo(expectedFrais);
    }

}