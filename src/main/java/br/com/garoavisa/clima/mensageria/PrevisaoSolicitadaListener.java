package br.com.garoavisa.clima.mensageria;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import br.com.garoavisa.clima.evento.PrevisaoSolicitadaEvento;
import br.com.garoavisa.clima.previsao.PrevisaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrevisaoSolicitadaListener {

    private final PrevisaoService previsaoService;

    @RabbitListener(queues = Mensageria.FILA_PREVISAO_SOLICITADA)
    public void receber(PrevisaoSolicitadaEvento evento,
                        @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {

        log.info("Pedido de previsão recebido ({}): {} endereço(s)", routingKey, evento.enderecos().size());

        if (evento.enderecos().isEmpty()) {
            return;
        }
        previsaoService.atualizar(evento.enderecos());
    }
}