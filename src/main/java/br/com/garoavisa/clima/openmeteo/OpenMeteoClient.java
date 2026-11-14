package br.com.garoavisa.clima.openmeteo;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import br.com.garoavisa.clima.evento.EnderecoParaPrevisao;

@Component
public class OpenMeteoClient {

    private static final String URL_BASE = "https://api.open-meteo.com";
    private static final int DIAS = 16;

    private static final String CAMPOS_ATUAL =
            "temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,is_day,"
          + "wind_speed_10m,wind_direction_10m,wind_gusts_10m";

    private static final String CAMPOS_POR_HORA =
            "temperature_2m,precipitation_probability,weather_code,is_day,uv_index,wind_gusts_10m";

    private static final String CAMPOS_POR_DIA =
            "temperature_2m_max,temperature_2m_min,weather_code,precipitation_probability_max,"
          + "precipitation_sum,uv_index_max,wind_gusts_10m_max,sunrise,sunset";

    private final RestClient restClient;

    public OpenMeteoClient() {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(10));
        fabrica.setReadTimeout(Duration.ofSeconds(30));

        this.restClient = RestClient.builder()
                .baseUrl(URL_BASE)
                .requestFactory(fabrica)
                .build();
    }

    // Uma chamada para todos os endereços. A resposta vem NA MESMA ORDEM do pedido.
    public List<OpenMeteoResposta> buscar(List<EnderecoParaPrevisao> enderecos) {
        RestClient.ResponseSpec resposta = restClient.get()
                .uri(uri -> uri.path("/v1/forecast")
                        .queryParam("latitude", juntar(enderecos, e -> e.latitude().toPlainString()))
                        .queryParam("longitude", juntar(enderecos, e -> e.longitude().toPlainString()))
                        .queryParam("timezone", juntar(enderecos, EnderecoParaPrevisao::fuso))
                        .queryParam("forecast_days", DIAS)
                        .queryParam("current", CAMPOS_ATUAL)
                        .queryParam("hourly", CAMPOS_POR_HORA)
                        .queryParam("daily", CAMPOS_POR_DIA)
                        .build())
                .retrieve();

        // Com 1 coordenada a Open-Meteo devolve um OBJETO; com várias, uma LISTA
        List<OpenMeteoResposta> respostas = enderecos.size() == 1
                ? List.of(resposta.body(OpenMeteoResposta.class))
                : resposta.body(new ParameterizedTypeReference<List<OpenMeteoResposta>>() {});

        if (respostas == null || respostas.size() != enderecos.size()) {
            throw new IllegalStateException("Open-Meteo devolveu " + (respostas == null ? 0 : respostas.size())
                    + " previsões para " + enderecos.size() + " endereços");
        }
        return respostas;
    }

    private String juntar(List<EnderecoParaPrevisao> enderecos, Function<EnderecoParaPrevisao, String> campo) {
        return enderecos.stream().map(campo).collect(Collectors.joining(","));
    }
}