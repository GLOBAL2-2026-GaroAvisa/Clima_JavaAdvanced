package br.com.garoavisa.clima.openmeteo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// Resposta da Open-Meteo para UMA localização (com várias, vem uma lista destes)
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenMeteoResposta(
        Double latitude,
        Double longitude,
        String timezone,
        @JsonProperty("utc_offset_seconds") Integer utcOffsetSegundos,
        Atual current,
        PorHora hourly,
        PorDia daily) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Atual(
            LocalDateTime time,
            @JsonProperty("temperature_2m") Double temperatura,
            @JsonProperty("apparent_temperature") Double sensacaoTermica,
            @JsonProperty("relative_humidity_2m") Integer umidade,
            @JsonProperty("weather_code") Integer codigoWmo,
            @JsonProperty("wind_speed_10m") Double ventoVelocidade,
            @JsonProperty("wind_direction_10m") Integer ventoDirecao,
            @JsonProperty("wind_gusts_10m") Double rajada,
            @JsonProperty("is_day") Integer ehDia) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PorHora(
            List<LocalDateTime> time,
            @JsonProperty("temperature_2m") List<Double> temperatura,
            @JsonProperty("precipitation_probability") List<Integer> chanceChuva,
            @JsonProperty("weather_code") List<Integer> codigoWmo,
            @JsonProperty("uv_index") List<Double> uv,
            @JsonProperty("wind_gusts_10m") List<Double> rajada,
            @JsonProperty("is_day") List<Integer> ehDia) {
    }

    // "daily": um array por variável, todos do mesmo tamanho que "time"
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PorDia(
            List<LocalDate> time,
            @JsonProperty("temperature_2m_max") List<Double> maxima,
            @JsonProperty("temperature_2m_min") List<Double> minima,
            @JsonProperty("weather_code") List<Integer> codigoWmo,
            @JsonProperty("precipitation_probability_max") List<Integer> chanceChuvaMaxima,
            @JsonProperty("precipitation_sum") List<Double> chuvaMm,
            @JsonProperty("uv_index_max") List<Double> uvMaximo,
            @JsonProperty("wind_gusts_10m_max") List<Double> rajadaMaxima,
            List<LocalDateTime> sunrise,
            List<LocalDateTime> sunset) {
    }
}