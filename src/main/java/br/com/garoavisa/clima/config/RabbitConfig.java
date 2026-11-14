package br.com.garoavisa.clima.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.garoavisa.clima.mensageria.Mensageria;

@Configuration
public class RabbitConfig {

    // Exchange de tópicos onde os dois serviços publicam
    @Bean
    TopicExchange garoavisaEventos() {
        return new TopicExchange(Mensageria.EXCHANGE, true, false);
    }

    // Fila de pedidos: durável; mensagens com erro vão para a DLQ
    @Bean
    Queue filaPrevisaoSolicitada() {
        return QueueBuilder.durable(Mensageria.FILA_PREVISAO_SOLICITADA)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Mensageria.FILA_PREVISAO_SOLICITADA_DLQ)
                .build();
    }

    // Fila de erros (DLQ)
    @Bean
    Queue filaPrevisaoSolicitadaDlq() {
        return QueueBuilder.durable(Mensageria.FILA_PREVISAO_SOLICITADA_DLQ).build();
    }

    // Binding: "previsao.solicitada.*" da exchange vai para a fila de pedidos
    @Bean
    Binding bindingPrevisaoSolicitada(Queue filaPrevisaoSolicitada, TopicExchange garoavisaEventos) {
        return BindingBuilder.bind(filaPrevisaoSolicitada)
                .to(garoavisaEventos)
                .with(Mensageria.RK_PREVISAO_SOLICITADA);
    }

    // JSON ↔ objeto. Usa o tipo do parâmetro do @RabbitListener,
    // e não o nome da classe enviado pela API (que tem outro pacote)
    @Bean
    MessageConverter conversorJson() {
        JacksonJsonMessageConverter conversor = new JacksonJsonMessageConverter();
        conversor.setAlwaysConvertToInferredType(true);
        return conversor;
    }
}