package br.com.garoavisa.clima.previsao;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// Agrupa os códigos WMO da Open-Meteo nas categorias que o app mostra
public enum CategoriaClima {

    CEU_LIMPO(Set.of(0)),
    PARCIALMENTE_NUBLADO(Set.of(1, 2)),
    NUBLADO(Set.of(3)),
    NEBLINA(Set.of(45, 48)),
    GAROA(Set.of(51, 53, 55, 56, 57)),
    CHUVA(Set.of(61, 63, 65, 66, 67, 80, 81, 82)),
    NEVE(Set.of(71, 73, 75, 77, 85, 86)),
    TEMPESTADE(Set.of(95, 96, 99)),
    DESCONHECIDA(Set.of());

    private static final Map<Integer, CategoriaClima> POR_CODIGO = new HashMap<>();

    static {
        for (CategoriaClima categoria : values()) {
            for (Integer codigo : categoria.codigos) {
                POR_CODIGO.put(codigo, categoria);
            }
        }
    }

    private final Set<Integer> codigos;

    CategoriaClima(Set<Integer> codigos) {
        this.codigos = codigos;
    }

    public static CategoriaClima deCodigo(Integer codigoWmo) {
        if (codigoWmo == null) {
            return DESCONHECIDA;
        }
        return POR_CODIGO.getOrDefault(codigoWmo, DESCONHECIDA);
    }
}