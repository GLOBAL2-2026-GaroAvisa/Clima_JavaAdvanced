package br.com.garoavisa.clima.mensageria;

public final class Mensageria {

    public static final String EXCHANGE = "garoavisa.eventos";

    // Pedidos de previsão (API → Clima)
    public static final String RK_PREVISAO_SOLICITADA = "previsao.solicitada.*";
    public static final String FILA_PREVISAO_SOLICITADA = "clima.previsao-solicitada";
    public static final String FILA_PREVISAO_SOLICITADA_DLQ = "clima.previsao-solicitada.dlq";

    // Previsões prontas (Clima → API)
    public static final String RK_PREVISAO_ATUALIZADA = "previsao.atualizada";

    private Mensageria() {
    }
}
