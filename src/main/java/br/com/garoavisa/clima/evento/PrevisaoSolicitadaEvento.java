package br.com.garoavisa.clima.evento;

import java.util.List;

public record PrevisaoSolicitadaEvento(List<EnderecoParaPrevisao> enderecos) {
}