package models.dinheiro;
import models.ValorMonetario;
public abstract class Dinheiro implements ValorMonetario {
    private int quantidade = 0;
    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

}