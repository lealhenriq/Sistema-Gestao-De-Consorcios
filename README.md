Projeto desenvolvido por Paulo Henrique Leal dos Santos e Tupi Guedes Ribas.

# Sistema de Gestão de Consórcio de Veículos



## Visão Geral

O Sistema de Gestão de Consórcio de Veículos é uma aplicação web completa desenvolvida com Spring Boot que permite gerenciar todos os aspectos de um consórcio de veículos. O sistema oferece funcionalidades para administração de grupos de consórcio, clientes, cotas, pagamentos, sorteios e geração de relatórios gerenciais.

Desenvolvido com foco na usabilidade e eficiência, o sistema proporciona uma interface intuitiva para o gerenciamento de consórcios, automatizando processos críticos como cálculo de parcelas, gestão de contemplações e controle financeiro.

## Tecnologias Utilizadas

- **Backend**:
  - Java 11
  - Spring Boot 2.7.18
  - Spring Data JPA
  - Spring Security
  - Spring Validation
  - Lombok
  - H2 Database (banco de dados em memória)

- **Frontend**:
  - Thymeleaf (template engine)
  - Bootstrap 5
  - Chart.js (gráficos e visualizações)
  - Font Awesome/Bootstrap Icons

- **Documentação**:
  - Springdoc OpenAPI (Swagger)

## Funcionalidades Principais

### 1. Gestão de Grupos de Consórcio
- Cadastro, edição e visualização de grupos
- Configuração de prazos, valor do bem e número de cotas
- Controle do status do grupo (Formação, Ativo, Encerrado)
- Monitoramento de taxa de contemplação

### 2. Gestão de Clientes
- Cadastro completo de clientes com dados pessoais e financeiros
- Visualização das cotas associadas a cada cliente
- Histórico de participação em grupos

### 3. Gestão de Cotas
- Atribuição de cotas a clientes
- Controle de status (Ativa, Inativa, Contemplada)
- Visualização detalhada de cada cota

### 4. Gestão Financeira
- Registro e acompanhamento de pagamentos
- Controle de inadimplência
- Cálculo automático de valores e taxas
- Relatórios financeiros detalhados

### 5. Sorteios
- Realização de sorteios automáticos ou manuais
- Registro de contemplações
- Histórico de sorteios realizados

### 6. Sistema de Relatórios
- Relatórios gerenciais completos
- Gráficos e estatísticas em tempo real
- Exportação e impressão de relatórios
- Dashboards interativos com métricas-chave

### 7. Segurança
- Autenticação e autorização de usuários
- Controle de acesso baseado em perfis
- Proteção contra acessos não autorizados

## Estrutura do Projeto

```
src/main/java/br/edu/uniruy/consorcio/
│
├── api/                  # Recursos da API REST
├── config/               # Configurações do Spring e segurança
├── controller/           # Controladores MVC
├── model/                # Entidades JPA (Cliente, Cota, GrupoConsorcio, etc.)
├── repository/           # Interfaces de acesso a dados
├── service/              # Camada de serviços e regras de negócio
└── ConsorcioVeiculosApplication.java  # Ponto de entrada da aplicação

src/main/resources/
│
├── static/               # Recursos estáticos (CSS, JS, imagens)
├── templates/            # Templates Thymeleaf
│   ├── clientes/         # Views de clientes
│   ├── cotas/            # Views de cotas
│   ├── grupos/           # Views de grupos de consórcio
│   ├── pagamentos/       # Views de pagamentos
│   ├── relatorios/       # Views de relatórios
│   ├── sorteios/         # Views de sorteios
│   └── usuarios/         # Views de usuários
├── application.properties  # Configurações da aplicação
```

## Configuração e Instalação

### Pré-requisitos

- Java Development Kit (JDK) 11 ou superior
- Maven 3.6.0 ou superior
- Navegador web moderno

### Passos para Execução

1. Clone o repositório:
   ```bash
   git clone https://github.com/sua-organizacao/consorcio-veiculos.git
   cd consorcio-veiculos
   ```

2. Compile o projeto usando Maven:
   ```bash
   ./mvnw clean package
   ```

3. Execute a aplicação:
   ```bash
   ./mvnw spring-boot:run
   ```

4. Acesse a aplicação no navegador:
   ```
   http://localhost:8080
   ```

5. Credenciais padrão:
   ```
   Usuário: admin
   Senha: admin
   ```

## Módulos do Sistema

### Módulo de Dashboard

O dashboard principal apresenta uma visão geral do sistema com os principais indicadores:
- Total de grupos ativos
- Total de clientes
- Valor total de parcelas
- Taxa média de contemplação
- Gráficos de desempenho financeiro
- Alertas e notificações importantes

### Módulo de Grupos

Permite o gerenciamento completo dos grupos de consórcio:
- Criação de novos grupos com definição de parâmetros
- Acompanhamento do ciclo de vida de cada grupo
- Visualização de cotas disponíveis e vendidas
- Monitoramento de contemplações

### Módulo de Clientes

Gerencia o cadastro e relacionamento com clientes:
- Registro completo de dados pessoais e contato
- Histórico de participação em grupos
- Situação financeira do cliente no consórcio
- Acompanhamento de contemplações

### Módulo de Cotas

Controla as cotas disponíveis em cada grupo:
- Venda de cotas para clientes
- Transferência de cotas entre clientes
- Acompanhamento de status e pagamentos
- Registro de contemplações

### Módulo de Pagamentos

Gerencia o fluxo financeiro do consórcio:
- Registro de pagamentos de parcelas
- Controle de inadimplência
- Geração de extratos
- Visualização de histórico financeiro

### Módulo de Sorteios

Administra os sorteios para contemplação:
- Agendamento de sorteios
- Execução manual ou automática
- Registro detalhado dos resultados
- Notificação de contemplados

### Módulo de Relatórios

Fornece informações gerenciais detalhadas:
- Relatório financeiro
- Relatório de grupos
- Relatório de clientes
- Relatório de cotas
- Relatório de sorteios
- Exportação e impressão

## API REST

A aplicação disponibiliza uma API REST documentada com Swagger para integração com outros sistemas:

- **Documentação da API**: http://localhost:8080/swagger-ui.html
- **Especificação OpenAPI**: http://localhost:8080/api-docs

## Segurança

O sistema implementa medidas robustas de segurança:

- Autenticação de usuários com Spring Security
- Armazenamento seguro de senhas com BCrypt
- Controle de acesso baseado em perfis (ADMIN, GESTOR, OPERADOR)
- Proteção contra ataques CSRF
- Sessões seguras com timeout

## Banco de Dados

Por padrão, a aplicação utiliza o banco de dados H2 em memória para facilitar testes e desenvolvimento. Para ambientes de produção, é recomendado configurar um banco de dados persistente como MySQL, PostgreSQL ou Oracle.

A configuração do banco de dados pode ser alterada no arquivo `application.properties`:

```properties
# Database configuration
spring.datasource.url=jdbc:h2:mem:consorcio
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
```

## Melhores Práticas Implementadas

Este projeto segue diversas práticas recomendadas de desenvolvimento:

- **Arquitetura em Camadas**: Separação clara entre apresentação, lógica de negócios e acesso a dados
- **Validação de Dados**: Validação rigorosa de entradas do usuário
- **Tratamento de Exceções**: Manipulação adequada de erros em todos os níveis
- **Segurança**: Implementação de controles de acesso e proteção de dados
- **Código Limpo**: Seguindo princípios SOLID e boas práticas de codificação
- **Testes**: Estrutura preparada para testes unitários e de integração
- **Documentação**: Código documentado e API documentada com OpenAPI

## Problemas Conhecidos e Soluções

O sistema implementa tratamentos específicos para evitar problemas comuns:

1. **Serialização de Entidades com Referências Circulares**:
   - Configuração do Jackson para lidar com referências circulares
   - Uso de DTOs para transferência de dados quando necessário

2. **Formatação de Valores Monetários**:
   - Implementação de formatação consistente para valores zero/nulos
   - Uso do padrão brasileiro para exibição de moeda (R$ 0,00)

3. **Expressões Complexas no Thymeleaf**:
   - Simplificação de expressões lambda que causavam erros
   - Pré-processamento de dados no controlador para simplificar o template
