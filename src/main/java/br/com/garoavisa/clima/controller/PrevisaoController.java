package br.com.garoavisa.clima.controller;

import java.math.BigDecimal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.garoavisa.clima.evento.EnderecoParaPrevisao;
import br.com.garoavisa.clima.evento.PrevisaoAtualizadaEvento;
import br.com.garoavisa.clima.previsao.PrevisaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Chamada SINCRONA, usada pela tela de Pesquisa da API principal.
// Nada e guardado nem publicado: a resposta volta na hora.
@Slf4j
@RestController
@RequiredArgsConstructor
public class PrevisaoController {

    private final PrevisaoService previsaoService;

    @GetMapping("/previsao")
    public PrevisaoAtualizadaEvento buscar(@RequestParam BigDecimal latitude,
                                           @RequestParam BigDecimal longitude,
                                           @RequestParam String fuso) {

        log.info("Previsao pedida na hora: {}, {} ({})", latitude, longitude, fuso);

        // Sem enderecoId: este lugar nao esta salvo no banco da API
        return previsaoService.buscarUm(new EnderecoParaPrevisao(null, latitude, longitude, fuso));
    }
}