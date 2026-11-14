package br.com.garoavisa.clima.evento;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import br.com.garoavisa.clima.previsao.CategoriaClima;

// E2: a previsão de UM endereço (Clima -> API)
public record PrevisaoAtualizadaEvento(
        Long enderecoId,
        String fuso,
        Instant atualizadoEm,
        Agora agora,
        List<Hora> horas,
        List<Dia> dias) {

    public record Agora(
            LocalDateTime horario,
            Double temperatura,
            Double sensacaoTermica,
            CategoriaClima categoria,
            Integer codigoWmo,
            Integer umidade,
            Double ventoVelocidade,
            Integer ventoDirecao,
            Double rajada,
            Double uv,
            Integer chanceChuva,
            Boolean ehDia) {
    }

    public record Hora(
            LocalDateTime horario,
            Double temperatura,
            Integer chanceChuva,
            CategoriaClima categoria,
            Integer codigoWmo,
            Double uv,
            Double rajada,
            Boolean ehDia) {
    }

    // Um dia (16 dias). Usado pelos Detalhes e pelo status do compromisso
    public record Dia(
            LocalDate data,
            Double maxima,
            Double minima,
            CategoriaClima categoria,
            Integer codigoWmo,
            Integer chanceChuvaMaxima,
            Double chuvaMm,
            Double uvMaximo,
            Double rajadaMaxima,
            LocalDateTime nascerDoSol,
            LocalDateTime porDoSol) {
    }
}