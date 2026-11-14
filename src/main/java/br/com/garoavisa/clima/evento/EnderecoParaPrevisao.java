package br.com.garoavisa.clima.evento;

import java.math.BigDecimal;

public record EnderecoParaPrevisao(
        Long enderecoId,
        BigDecimal latitude,
        BigDecimal longitude,
        String fuso) {
}