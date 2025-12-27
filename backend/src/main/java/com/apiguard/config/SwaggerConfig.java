package com.apiguard.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de Swagger/OpenAPI
 * 
 * Genera documentación interactiva de la API
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI apiDocConfig() {
        return new OpenAPI()
                .info(new Info()
                        .title("APIGuard - Sistema de Soporte para Servicios Web")
                        .description(
                                "API completa para monitoreo, diagnóstico y soporte de integraciones API REST/SOAP. " +
                                        "Incluye herramientas de health checking, análisis automatizado y base de conocimiento técnico.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Tu Nombre")
                                .email("tu-email@ejemplo.com")
                                .url("https://github.com/tu-usuario/apiguard"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de Desarrollo"),
                        new Server()
                                .url("https://apiguard-production.up.railway.app")
                                .description("Servidor de Producción")));
    }
}