package models.dinheiro;

import java.util.Arrays;

// ALTEREI MAIS A CLASSE DO 'TROCOPARA'
public class Dispenser {

    // Inventário inicial – uma instância de cada denominação
    private static final Dinheiro[] estoque = new Dinheiro[] {
            new MoedaDeCincoCentavos(),
            new MoedaDeDezCentavos(),
            new MoedaDeVinteECincoCentavos(),
            new MoedaDeCinquentaCentavos(),
            new MoedaDeUmReal(),
            new NotaDeUmReal(),
            new NotaDeDoisReais(),
            new NotaDeCincoReais(),
            new NotaDeDezReais(),
            new NotaDeVinteReais(),
            new NotaDeCinquentaReais(),
            new NotaDeCemReais(),
    };

    /** Define a quantidade fixa para uma denominação (inicialização). */
    public void definirEstoque(Class<? extends Dinheiro> clazz, int quantidade){
        getDinheiro(clazz).setQuantidade(quantidade);
    }

    /** Retorna a quantidade atual de uma denominação. */
    public static int getQuantidade(Class<? extends Dinheiro> clazz){
        return getDinheiro(clazz).getQuantidade();
    }

    /** Soma total do dinheiro no dispenser. */
    public double getSaldoTotal() {
        double total = 0;
        for (Dinheiro dinheiro : estoque) {
            total += dinheiro.getQuantidade() * dinheiro.valor();
        }
        return total;
    }

    private static Dinheiro getDinheiro(Class<? extends Dinheiro> clazz) {
        for (Dinheiro dinheiro : estoque) {
            if (dinheiro.getClass() == clazz) {
                return dinheiro;
            }
        }
        throw new IllegalArgumentException("Denominação não registrada: " + clazz.getSimpleName());
    }

    public static void incrementarEstoque(Class<? extends Dinheiro> clazz, int quantidade){
        Dinheiro dinheiro = getDinheiro(clazz);
        dinheiro.setQuantidade(dinheiro.getQuantidade() + quantidade);
    }

    public static void decrementarEstoque(Class<? extends Dinheiro> clazz, int quantidade){
        Dinheiro dinheiro = getDinheiro(clazz);
        dinheiro.setQuantidade(dinheiro.getQuantidade() - quantidade);
    }

    /**
     * Calcula o troco usando um algoritmo ganancioso (maiores valores primeiro).
     * Retorna um array vazio se não houver troco e {@code null} caso o estoque
     * não permita montar o valor necessário.
     */
    // NOTA: ALTEREI O ALGORITMO PARA QUE ELE NÃO SEJA MAIS GREEDY, PORQUE NÃO ENCONTREI SOLUÇÃO COM ELE GREEDY

    public Dinheiro[] trocoPara(double valorPago, double valorProduto) {
        double troco = valorPago - valorProduto;

        // PASSAR NO TESTE 5 AQUI:
        if (troco > getSaldoTotal()){
            return null;
        }
        if (troco == 0.0) {
            return new Dinheiro[0];
        }

        // Cópia apenas das referências para ordenação
        Dinheiro[] ordenado = Arrays.copyOf(estoque, estoque.length);

        // Ordena por valor decrescente (bubble‑sort simples)
        for (int i = 0; i < ordenado.length - 1; i++) {
            for (int j = i + 1; j < ordenado.length; j++) {
                if (ordenado[j].valor() > ordenado[i].valor()) {
                    Dinheiro tmp = ordenado[i];
                    ordenado[i] = ordenado[j];
                    ordenado[j] = tmp;
                }
            }
        }

        // MUDEI UM POUCO PARA FICAR MAIS CLARO
        Dinheiro[] usados = new Dinheiro[100];
        int[] quantidadesUsadas = new int[ordenado.length];

        // ABAIXO FOI ONDE MAIS MUDEI + TENTATIVA DO DESAFIO

        // NOVA FUNÇÃO (DIVIDI EM DOIS PASSOS)
        if (encontrarTroco(ordenado, troco, 0, usados, 0, quantidadesUsadas)) {
            for (int i = 0; i < ordenado.length; i++) {
                if (quantidadesUsadas[i] > 0) {
                    decrementarEstoque(ordenado[i].getClass(), quantidadesUsadas[i]);
                }
            }
            int totalUsado = 0;

            // "LOOP INTELIGENTE", BASICAMENTE SUBSTITUI 'I = 0; I < X; I++' ETC
            for (int q : quantidadesUsadas) {
                totalUsado += q;
            }

            return Arrays.copyOf(usados, totalUsado);
        }

        return null;
    }

    // PRIVATE BOOLEAN PORQUE SÓ QUERO QUE CHAME NA PRÓPRIA CLASSE ATÉ FALHAR OU DAR CERTO
    private boolean encontrarTroco(Dinheiro[] ordenado, double trocoRestante,
                                   int indice, Dinheiro[] usados, int usadosIndex,
                                   int[] quantidadesUsadas) {

        // NÃO TEM NECESSIDADE DE CONTINUAR PORQUE O TROCO JÁ DEU CERTO
        if (Math.abs(trocoRestante) < 1e-6) {
            return true;
        }

        // POR PRECAUÇÃO, CASO O TROCO DÊ NEGATIVO
        if (indice >= ordenado.length || trocoRestante < -1e-6) {
            return false;
        }

        // AQUI TÁ PEGANDO A MOEDA DE ANÁLISE E COMPARANDO COM A MOEDA QUE TEM NO ÍNDICE QUE FOI ORDENADO JÁ POR BUBBLE-SORT
        Dinheiro moedaAtual = ordenado[indice];
        // REDUZ AS MOEDAS QUE ESTÃO SENDO USADAS AGORA DO ÍNDICE
        int disponivel = moedaAtual.getQuantidade() - quantidadesUsadas[indice];
        // TENTA USAR O MÁXIMO POSSÍVEL DE MOEDAS
        int maxUso = Math.min(disponivel, (int)(trocoRestante / moedaAtual.valor() + 1e-6));

        // Coloca as moedas usadas como se fosse num "saco" pra dar o troco, usa até zerar as moedas que tem
        for (int usar = maxUso; usar >= 0; usar--) {
            if (usar > 0) {
                for (int i = 0; i < usar; i++) {
                    usados[usadosIndex + i] = novaInstancia(moedaAtual);
                }
                quantidadesUsadas[indice] += usar;
            }

            // CALCULA O QUE AINDA FALTA PARA USAR
            double novoTroco = trocoRestante - (usar * moedaAtual.valor());
            novoTroco = (double) Math.round(novoTroco * 100.0) / 100.0;

            // AQUI É ONDE TÁ A RECURSION
            // FICA SE CHAMANDO DE NOVO PARA TENTAR O IDEAL, SE FALHAR DÁ BACKTRACKING
            if (encontrarTroco(ordenado, novoTroco, indice + 1, usados,
                    usadosIndex + usar, quantidadesUsadas)) {
                return true;
            }

            if (usar > 0) {
                quantidadesUsadas[indice] -= usar;
            }
        }

        return false;
    }

    /**
     * Cria uma nova instância do mesmo tipo de {@code Dinheiro} sem usar
     * reflection (recursos ainda não vistos).  Para manter o código simples,
     * utilizamos uma cadeia de `instanceof`.
     */
    private Dinheiro novaInstancia(Dinheiro dinheiro) {
        if (dinheiro instanceof MoedaDeCincoCentavos)       return new MoedaDeCincoCentavos();
        if (dinheiro instanceof MoedaDeDezCentavos)         return new MoedaDeDezCentavos();
        if (dinheiro instanceof MoedaDeVinteECincoCentavos) return new MoedaDeVinteECincoCentavos();
        if (dinheiro instanceof MoedaDeCinquentaCentavos)   return new MoedaDeCinquentaCentavos();
        if (dinheiro instanceof MoedaDeUmReal)              return new MoedaDeUmReal();
        if (dinheiro instanceof NotaDeUmReal)               return new NotaDeUmReal();
        if (dinheiro instanceof NotaDeDoisReais)            return new NotaDeDoisReais();
        if (dinheiro instanceof NotaDeCincoReais)           return new NotaDeCincoReais();
        if (dinheiro instanceof NotaDeDezReais)             return new NotaDeDezReais();
        if (dinheiro instanceof NotaDeVinteReais)           return new NotaDeVinteReais();
        if (dinheiro instanceof NotaDeCinquentaReais)       return new NotaDeCinquentaReais();
        if (dinheiro instanceof NotaDeCemReais)             return new NotaDeCemReais();

        throw new IllegalArgumentException("Tipo de dinheiro desconhecido");
    }

    /**
     * Coloca todas as quantidades do estoque em zero (útil para testes).
     */
    public void zerarEstoque() {
        for (int i = 0; i < estoque.length; i++) {
            estoque[i].setQuantidade(0);
        }
    }

}

