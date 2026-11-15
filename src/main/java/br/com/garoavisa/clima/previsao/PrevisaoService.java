package br.com.garoavisa.clima.previsao;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.garoavisa.clima.evento.EnderecoParaPrevisao;
import br.com.garoavisa.clima.evento.PrevisaoAtualizadaEvento;
import br.com.garoavisa.clima.evento.PrevisaoAtualizadaEvento.Agora;
import br.com.garoavisa.clima.evento.PrevisaoAtualizadaEvento.Dia;
import br.com.garoavisa.clima.evento.PrevisaoAtualizadaEvento.Hora;
import br.com.garoavisa.clima.mensageria.PrevisaoAtualizadaPublisher;
import br.com.garoavisa.clima.openmeteo.OpenMeteoClient;
import br.com.garoavisa.clima.openmeteo.OpenMeteoResposta;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PrevisaoService {

    private final OpenMeteoClient openMeteoClient;
    private final PrevisaoAtualizadaPublisher publisher;

    // Um pedido (E1) com N endereços -> 1 chamada à Open-Meteo -> N mensagens E2
    public void atualizar(List<EnderecoParaPrevisao> enderecos) {
        List<OpenMeteoResposta> respostas = openMeteoClient.buscar(enderecos);
        Instant atualizadoEm = Instant.now();

        // A resposta vem na mesma ordem do pedido: a posição liga cada uma ao seu endereço
        for (int i = 0; i < enderecos.size(); i++) {
            publisher.publicar(montar(enderecos.get(i), respostas.get(i), atualizadoEm));
        }
    }
    
    // Pesquisa (feature 74): busca e DEVOLVE, sem publicar nada
    public PrevisaoAtualizadaEvento buscarUm(EnderecoParaPrevisao endereco) {
        List<OpenMeteoResposta> respostas = openMeteoClient.buscar(List.of(endereco));
        return montar(endereco, respostas.get(0), Instant.now());
    }

    // ---------- Montagem do E2 ----------

    private PrevisaoAtualizadaEvento montar(EnderecoParaPrevisao endereco, OpenMeteoResposta resposta,
                                            Instant atualizadoEm) {
        List<Hora> horas = montarHoras(resposta.hourly());
        List<Dia> dias = montarDias(resposta.daily());
        Agora agora = montarAgora(resposta.current(), horas);

        return new PrevisaoAtualizadaEvento(endereco.enderecoId(), endereco.fuso(), atualizadoEm,
                agora, horas, dias);
    }

    // Colunas -> linhas: a hora i pega a posição i de cada array
    private List<Hora> montarHoras(OpenMeteoResposta.PorHora h) {
        List<Hora> horas = new ArrayList<>();
        if (h == null || h.time() == null) {
            return horas;
        }
        for (int i = 0; i < h.time().size(); i++) {
            Integer codigo = valor(h.codigoWmo(), i);
            horas.add(new Hora(
                    h.time().get(i),
                    valor(h.temperatura(), i),
                    valor(h.chanceChuva(), i),
                    CategoriaClima.deCodigo(codigo),
                    codigo,
                    valor(h.uv(), i),
                    valor(h.rajada(), i),
                    ehDia(valor(h.ehDia(), i))));
        }
        return horas;
    }

    private List<Dia> montarDias(OpenMeteoResposta.PorDia d) {
        List<Dia> dias = new ArrayList<>();
        if (d == null || d.time() == null) {
            return dias;
        }
        for (int i = 0; i < d.time().size(); i++) {
            Integer codigo = valor(d.codigoWmo(), i);
            dias.add(new Dia(
                    d.time().get(i),
                    valor(d.maxima(), i),
                    valor(d.minima(), i),
                    CategoriaClima.deCodigo(codigo),
                    codigo,
                    valor(d.chanceChuvaMaxima(), i),
                    valor(d.chuvaMm(), i),
                    valor(d.uvMaximo(), i),
                    valor(d.rajadaMaxima(), i),
                    valor(d.sunrise(), i),
                    valor(d.sunset(), i)));
        }
        return dias;
    }

    // UV e chance de chuva "agora" vêm da hora cheia atual (ex.: 14:30 -> 14:00)
    private Agora montarAgora(OpenMeteoResposta.Atual a, List<Hora> horas) {
        if (a == null) {
            return null;
        }
        LocalDateTime horaCheia = a.time() == null ? null : a.time().truncatedTo(ChronoUnit.HOURS);
        Hora horaAtual = horas.stream()
                .filter(h -> h.horario().equals(horaCheia))
                .findFirst()
                .orElse(null);

        return new Agora(
                a.time(),
                a.temperatura(),
                a.sensacaoTermica(),
                CategoriaClima.deCodigo(a.codigoWmo()),
                a.codigoWmo(),
                a.umidade(),
                a.ventoVelocidade(),
                a.ventoDirecao(),
                a.rajada(),
                horaAtual == null ? null : horaAtual.uv(),
                horaAtual == null ? null : horaAtual.chanceChuva(),
                ehDia(a.ehDia()));
    }

    // Posição i da lista, ou null se a lista não veio ou é mais curta
    private <T> T valor(List<T> lista, int i) {
        return (lista == null || i >= lista.size()) ? null : lista.get(i);
    }

    // A Open-Meteo manda 1 (dia) ou 0 (noite)
    private Boolean ehDia(Integer valor) {
        return valor == null ? null : valor == 1;
    }
}