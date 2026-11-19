# GaroAvisa — Serviço de Clima

Microsserviço auxiliar do **GaroAvisa**, a CitizenTech do grupo para a Global Solution
2026/2. Ele é o único ponto do sistema que fala com o provedor de previsão do tempo.

> **A API principal, com a documentação completa da solução, fica em
> [JavaAdvanced](https://github.com/GLOBAL2-2026-GaroAvisa/JavaAdvanced).**
> Comece por lá se você quer entender o projeto.

---

## Integrantes

| Nome | RM | Turma |
|---|---|---|
| Enzo Vieira Bernardini | RM563000 | 2TDSPI |
| Eduardo Batista Locaspi | RM561713 | 2TDSPI |
| Liana Lyumi Morisita Fujisima | RM565698 | 2TDSPI |
| Victor Alves Lopes | RM561833 | 2TDSPI |

---

## O que este serviço faz

Ele recebe uma coordenada, pergunta a previsão ao **Open-Meteo** e devolve o resultado no
formato que o GaroAvisa usa. Só isso.

**Não tem banco de dados. Não tem autenticação. Não conhece usuário nenhum.** É um
adaptador puro, e essa simplicidade é intencional: isolar o provedor de clima atrás de um
serviço próprio significa que trocá-lo no futuro não encosta na API principal.

---

## Deploy

| Serviço | URL |
|---|---|
| Serviço de Clima | https://gl-clima-api-fwbvhnbhb4a4bcer.brazilsouth-01.azurewebsites.net |
| API Principal | https://garoavisa-api.onrender.com |

---

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.1.2 |
| Mensageria | Spring AMQP · RabbitMQ (CloudAMQP) |
| Chamada externa | RestClient |
| Build | Maven |

---

## Como ele conversa com a API principal

De duas formas, cada uma para um propósito.

```
   ┌──────────────────────┐                    ┌──────────────────────┐
   │   API PRINCIPAL      │                    │  SERVIÇO DE CLIMA    │
   │      (Render)        │                    │      (Azure)         │
   │                      │                    │                      │
   │  agendador publica ──┼──── AMQP ─────────▶│  consome, busca      │
   │                      │  previsao.          │                      │
   │                      │  solicitada.*       │                      │
   │                      │                     │                     │
   │  consome ◀───────────┼──── AMQP ──────────┼── publica resultado  │
   │                      │  previsao.          │                      │
   │                      │  atualizada         │                      │
   │                      │                     │                     │
   │  Pesquisa ───────────┼──── HTTP ──────────▶│  GET /previsao      │
   │                      │  (RestClient)       │  responde na hora    │
   └──────────────────────┘                    └──────────┬───────────┘
                                                          │ HTTPS
                                                          ▼
                                                   ┌─────────────┐
                                                   │ Open-Meteo  │
                                                   └─────────────┘
```

**Assíncrona**, pelo RabbitMQ, para o trabalho de fundo: a API publica pedidos de previsão
para os endereços cadastrados, este serviço consome, busca e devolve o resultado por outro
evento. Filas duráveis, DLQ e consumidor idempotente.

**Síncrona**, por HTTP, para a Pesquisa: quando o cidadão busca um lugar que ainda não é um
Local salvo, não há histórico e ninguém pode esperar a fila. A API chama este serviço
direto e responde com o resultado.

---

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/` | Responde `clima ok`. Usado para checagem de saúde |
| `GET` | `/previsao` | Previsão de uma coordenada, buscada na hora |

### `GET /previsao`

| Parâmetro | Obrigatório | Exemplo |
|---|---|---|
| `latitude` | Sim | `-22.9842` |
| `longitude` | Sim | `-43.1986` |
| `fuso` | Sim | `America/Sao_Paulo` |

```
GET /previsao?latitude=-22.9842&longitude=-43.1986&fuso=America/Sao_Paulo
```

A resposta traz o clima atual, as próximas horas e até **16 dias** de previsão, já
convertidos para o formato do GaroAvisa — incluindo índice UV, rajadas de vento e
probabilidade de chuva, que alimentam o motor de alertas da API principal.

> O serviço não tem autenticação porque não expõe dado de ninguém: ele só repassa previsão
> pública de uma coordenada. Quem protege o cidadão é a API principal, com JWT.

---

## Como rodar

### Pré-requisitos

- Java 21
- Maven (ou o wrapper `mvnw` incluído)
- Uma instância RabbitMQ (o plano gratuito do CloudAMQP serve)

### 1. Clonar

```bash
git clone https://github.com/GLOBAL2-2026-GaroAvisa/Clima_JavaAdvanced.git
cd Clima_JavaAdvanced
```

### 2. Configurar a variável de ambiente

**Nenhuma credencial fica no código ou no repositório.** Este serviço precisa de uma
variável só:

| Variável | Para que serve |
|---|---|
| `RABBITMQ_URL` | URL AMQP do CloudAMQP — a **mesma** usada pela API principal |

No Linux ou macOS:

```bash
export RABBITMQ_URL=amqps://usuario:senha@host/vhost
```

No Windows (PowerShell):

```powershell
$env:RABBITMQ_URL="amqps://usuario:senha@host/vhost"
```

No Eclipse ou IntelliJ, em **Run Configurations → Environment**.

> Os dois serviços precisam apontar para a **mesma** instância do RabbitMQ, senão as
> mensagens publicadas por um nunca chegam ao outro.

### 3. Rodar

```bash
./mvnw spring-boot:run
```

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

O serviço sobe em `http://localhost:8081`. Para conferir:

```
http://localhost:8081/
```

Deve responder `clima ok`.

### 4. Apontar a API principal para ele

Na API principal, a variável `CLIMA_URL` precisa apontar para este serviço, **sem barra no
final**:

```bash
export CLIMA_URL=http://localhost:8081
```

---

## Uma nota sobre a hospedagem

Este serviço está no **Azure**, enquanto a API principal está no Render. A separação não é
estética.

A API gratuita do Open-Meteo limita as chamadas **por endereço IP**, e o plano gratuito do
Render compartilha o IP de saída entre muitos aplicativos. A cota diária daquele IP era
consumida por outros projetos, e o Open-Meteo passou a recusar as nossas chamadas com
`429 Too Many Requests`.

Confirmamos que o consumo não era nosso: a fila do RabbitMQ mostrava taxa zero e um volume
de mensagens muito abaixo do limite. Testamos também outra região do Render, e o IP europeu
estava igualmente esgotado.

Como este é o **único** serviço que fala com o Open-Meteo, movê-lo sozinho para outra nuvem
resolveu o problema sem tocar em mais nada do sistema — exatamente o tipo de troca que
justifica tê-lo isolado desde o começo.

---

## Créditos de dados

- Dados de previsão: **[Open-Meteo.com](https://open-meteo.com/)**, sob licença
  [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/).
- Dados de geocodificação: **Open-Meteo Geocoding API**, baseada em dados do
  **[GeoNames](https://www.geonames.org/)**.

---

## Declaração de uso de IA generativa

Durante o desenvolvimento deste trabalho utilizamos ferramentas de IA generativa,
conforme as regras da Global Solution.

| Ferramenta | Para quê |
|---|---|
| Claude (Anthropic) | Apoio na definição da arquitetura e discussão de alternativas; explicação de mensagens de erro e apoio na investigação de falhas; revisão de código e sugestões de melhoria; apoio na redação deste README |

Todo o código entregue foi **lido, compreendido, revisado e testado** pelo grupo.

A declaração completa, incluindo o uso de IA generativa **dentro do produto** (a
funcionalidade Explicação do Dia, com Spring AI), está no README da
[API principal](https://github.com/GLOBAL2-2026-GaroAvisa/JavaAdvanced).
