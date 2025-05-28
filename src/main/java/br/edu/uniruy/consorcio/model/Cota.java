package br.edu.uniruy.consorcio.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cotas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    @NotNull(message = "O cliente é obrigatório")
    @ToString.Exclude // Prevent circular reference in toString
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "grupo_id", nullable = false)
    @NotNull(message = "O grupo de consórcio é obrigatório")
    @ToString.Exclude // Prevent circular reference in toString
    private GrupoConsorcio grupo;

    @Column(nullable = false)
    @NotNull(message = "O número da cota é obrigatório")
    private Integer numero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusCota status = StatusCota.ATIVA;

    @Column(nullable = false)
    private Boolean contemplada = false;

    @Column(nullable = false)
    private BigDecimal valorParcela;

    @OneToMany(mappedBy = "cota", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude // Prevent circular reference in toString
    private List<Pagamento> pagamentos = new ArrayList<>();

    @OneToMany(mappedBy = "cotaContemplada")
    @ToString.Exclude // Prevent circular reference in toString
    private List<Sorteio> sorteios = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime dataAtualizacao;

    public enum StatusCota {
        ATIVA, CONTEMPLADA, INATIVA
    }

    @PrePersist
    public void prePersist() {
        if (valorParcela == null && grupo != null && grupo.getValorBem() != null && grupo.getNumeroCotas() != null && grupo.getPrazoMeses() != null) {
            // Valor da parcela = Valor total do bem / (número de cotas * prazo em meses)
            valorParcela = grupo.getValorBem().divide(
                BigDecimal.valueOf(grupo.getNumeroCotas()),
                2, 
                java.math.RoundingMode.HALF_UP
            );
        }
    }
    
    // Custom toString method to prevent circular reference overflow
    @Override
    public String toString() {
        return "Cota{" +
               "id=" + id +
               ", numero=" + numero +
               ", status=" + status +
               ", contemplada=" + contemplada +
               ", valorParcela=" + valorParcela +
               '}';
    }
} 