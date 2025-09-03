package br.com.etl.painel_macroeconomico.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "painelExchange";
    public static final String QUEUE = "painelQueue";
    public static final String ROUTING_KEY = "painelKey";


    @Description("O Exchange é como um correio que entrega mensagens para as filas certas")
    @Bean
    public DirectExchange painelExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Description("(fila) é onde as mensagens esperam, em ordem, para serem processadas")
    @Bean
    public Queue painelQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Description("é a regra que conecta o Exchange à Queue. É o que diz ao sistema como o roteamento deve acontecer")
    @Bean
    public Binding binding(Queue painelQueue, DirectExchange painelExchange) {
        return BindingBuilder.bind(painelQueue).to(painelExchange).with(ROUTING_KEY);
    }
}
