package br.edu.uniruy.consorcio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@EnableScheduling
@OpenAPIDefinition(
    info = @Info(
        title = "API de Gerenciamento de Consórcio de Veículos",
        version = "1.0",
        description = "API para gerenciamento de grupos de consórcio, clientes, cotas, pagamentos e sorteios"
    )
)
public class ConsorcioVeiculosApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsorcioVeiculosApplication.class, args);
    }
} 