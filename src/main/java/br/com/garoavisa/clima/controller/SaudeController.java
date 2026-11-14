package br.com.garoavisa.clima.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SaudeController {

    // Usado pelo Render para ver se o servico esta de pe
    // e pela API para acordar o Clima antes de publicar um pedido
    @GetMapping("/")
    public String saude() {
        return "clima ok";
    }
}