package br.com.garoavisa.clima.mensageria;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import br.com.garoavisa.clima.evento.PrevisaoAtualizadaEvento;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrevisaoAtualizadaPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publicar(PrevisaoAtualizadaEvento evento) {
        rabbitTemplate.convertAndSend(Mensageria.EXCHANGE, Mensageria.RK_PREVISAO_ATUALIZADA, evento);
        log.info("Previsão publicada (previsao.atualizada): endereço {}, {} horas, {} dias",
                evento.enderecoId(), evento.horas().size(), evento.dias().size());
    }
}