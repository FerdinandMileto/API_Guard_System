# 🛡️ APIGuard - Sistema Integral de Soporte para Servicios Web

![Java](https://img.shields.io/badge/Java-17+-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen?style=flat-square&logo=spring)
![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)

## 📋 Resumen Ejecutivo

**APIGuard** es una plataforma integral de soporte técnico para servicios web que demuestra capacidades profesionales en:

- ✅ Monitoreo y diagnóstico de APIs REST/SOAP
- ✅ Base de conocimiento técnico (self-service)
- ✅ Herramientas automatizadas de troubleshooting
- ✅ Documentación técnica completa
- ✅ Integración con servicios externos

**Propósito:** Demostrar habilidades técnicas para roles de Soporte de Servicios Web, combinando conocimientos de desarrollo con atención al cliente.

---

## 🎯 Problema que Resuelve

Los equipos de soporte técnico enfrentan desafíos constantes:
- Diagnóstico manual de problemas de integración API
- Documentación dispersa y desactualizada
- Falta de herramientas de autoservicio para clientes
- Tiempo de respuesta elevado en incidentes comunes

**APIGuard centraliza estas necesidades en una sola plataforma.**

---

## 🚀 Características Principales

### 1. **API Health Monitor**
Monitoreo en tiempo real de endpoints REST y SOAP con:
- Validación de disponibilidad
- Medición de latencia
- Análisis de respuestas
- Alertas configurables

### 2. **Diagnostic Tools**
Herramientas automatizadas para:
- Validar configuraciones de APIs
- Analizar errores comunes (401, 403, 500)
- Verificar formato de payloads
- Simular requests con diferentes parámetros

### 3. **Knowledge Base**
Base de conocimiento técnico con:
- Guías de integración paso a paso
- Ejemplos de código en múltiples lenguajes
- Soluciones a problemas frecuentes
- Best practices de la industria

### 4. **SOAP Client Demo**
Cliente SOAP funcional demostrando:
- Consumo de servicios WSDL
- Manejo de autenticación
- Transformación de respuestas XML
- Logging estructurado

### 5. **Integration Examples**
Colección completa de ejemplos:
- Postman Collection exportable
- Código de ejemplo en Java, Python, PHP
- Casos de uso reales
- Troubleshooting guide interactivo

---

## 🏗️ Arquitectura Técnica

```
┌─────────────────────────────────────────────────────────┐
│                     Frontend (React)                     │
│              Dashboard │ KB │ Diagnostics                │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│              API Gateway (Spring Boot)                   │
│  /health │ /diagnose │ /knowledge │ /soap-client        │
└──────────────────────┬──────────────────────────────────┘
                       │
        ┌──────────────┼──────────────┐
        │              │              │
┌───────▼──────┐ ┌────▼─────┐ ┌─────▼────────┐
│   Monitor    │ │ Diagnostic│ │     SOAP     │
│   Service    │ │  Service  │ │   Client     │
└──────────────┘ └───────────┘ └──────────────┘
        │              │              │
        └──────────────┼──────────────┘
                       │
            ┌──────────▼─────────┐
            │   PostgreSQL DB    │
            │  Logs │ KB │ Stats │
            └────────────────────┘
```

---

## 🛠️ Stack Tecnológico

**Backend:**
- Java 17+
- Spring Boot 3.2
- Spring Web (REST)
- Spring WS (SOAP)
- PostgreSQL
- Lombok
- Jackson (JSON)

**Frontend:**
- React 18
- Axios
- TailwindCSS
- Recharts (gráficas)

**DevOps:**
- Docker & Docker Compose
- GitHub Actions (CI/CD)
- Maven

---

## ⚡ Quick Start

### Prerequisitos
```bash
- Java JDK 17 o superior
- Maven 3.8+
- Docker (opcional)
- PostgreSQL 14+ (o usar Docker)
```

### Instalación Rápida

**Opción 1: Con Docker (Recomendado)**
```bash
# Clonar repositorio
git clone https://github.com/tu-usuario/apiguard.git
cd apiguard

# Levantar todos los servicios
docker-compose up -d

# Acceder a la aplicación
http://localhost:8080
```

**Opción 2: Manual**
```bash
# Backend
cd backend
mvn clean install
mvn spring-boot:run

# Frontend (en otra terminal)
cd frontend
npm install
npm start
```

### Configuración Base de Datos
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/apiguard
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: update
```

---

## 📚 Documentación Completa

- [📖 Guía de Arquitectura](docs/ARCHITECTURE.md)
- [🔌 API Integration Guide](docs/API_GUIDE.md)
- [🛠️ Troubleshooting](docs/TROUBLESHOOTING.md)
- [🚀 Deployment Guide](docs/DEPLOYMENT.md)
- [📝 Contributing](CONTRIBUTING.md)

---

## 🎓 Casos de Uso

### Para Equipos de Soporte
```java
// Diagnosticar un endpoint problemático
POST /api/diagnose
{
  "endpoint": "https://api.cliente.com/v1/products",
  "method": "GET",
  "headers": {"Authorization": "Bearer token"}
}

// Respuesta automática con análisis
{
  "status": "ERROR",
  "issue": "Invalid token format",
  "solution": "Token debe incluir prefijo 'Bearer'",
  "kbArticle": "/kb/auth-best-practices"
}
```

### Para Clientes (Self-Service)
1. Consultar KB para errores comunes
2. Usar herramientas de diagnóstico
3. Descargar ejemplos de código
4. Validar su integración antes de producción

---

## 🔍 Endpoints Principales

```http
# Health Check de APIs
GET /api/health/check?url={endpoint}

# Diagnóstico Completo
POST /api/diagnose

# Knowledge Base
GET /api/knowledge/articles
GET /api/knowledge/search?q={query}

# SOAP Client Demo
POST /api/soap/currency-conversion
POST /api/soap/validate-rfc

# Métricas
GET /api/metrics/summary
```

---

## 📊 Roadmap

- [x] Fase 1: Core API REST
- [x] Fase 2: SOAP Client
- [x] Fase 3: Knowledge Base
- [x] Fase 4: Dashboard Web
- [ ] Fase 5: Notificaciones Email
- [ ] Fase 6: Integración con Slack
- [ ] Fase 7: ML para predicción de errores

---

## 🤝 Contribuciones

Este proyecto está diseñado como portafolio profesional, pero acepta contribuciones:

1. Fork el proyecto
2. Crea tu feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la branch (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

---

## 👨‍💻 Autor

**Tu Nombre**
- LinkedIn: [tu-perfil](https://linkedin.com/in/fernando berumen
- Email: ferdinand.daemontech@proton.me
- Portfolio: [tu-sitio.com](https://github.com/FerdinandMileto/API_Guard_System)

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver [LICENSE.md](LICENSE.md) para detalles.

---

## 🙏 Agradecimientos

- Spring Framework Team
- Comunidad de desarrolladores Java
- SW Sapien por inspirar este proyecto

---

## 📞 Soporte

¿Preguntas o sugerencias?
- Abre un [Issue](https://github.com/FerdinandMileto/API_Guard_System/issues)
- Revisa la [Documentación](docs/)
- Consulta el [FAQ](docs/FAQ.md)

---

**Construido con ❤️ para demostrar excelencia en Soporte Técnico de Servicios Web**