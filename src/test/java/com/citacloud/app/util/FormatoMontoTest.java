package com.citacloud.app.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FormatoMontoTest {
    @Test
    void formatsAmountsWithThousandsAndTwoDecimals() {
        assertThat(FormatoMonto.format(new BigDecimal("1000"))).isEqualTo("1,000.00");
        assertThat(FormatoMonto.format(new BigDecimal("1234567.8"))).isEqualTo("1,234,567.80");
    }

    @Test
    void parsesFormattedAmountsBeforeSaving() {
        assertThat(FormatoMonto.parse("1,000.00")).isEqualByComparingTo("1000.00");
        assertThat(FormatoMonto.parse(" 2500 ")).isEqualByComparingTo("2500");
    }

    @Test
    void rejectsEmptyAmounts() {
        assertThatThrownBy(() -> FormatoMonto.parse(" ")).isInstanceOf(NumberFormatException.class);
    }
}
