package br.edu.uniruy.consorcio.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagamentos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cota_id", nullable = false)
    @NotNull(message = "A cota é obrigatória")
    @ToString.Exclude
    private Cota cota;

    @Column(nullable = false, name = "mes_ref")
    @Pattern(regexp = "\\d{2}/\\d{4}", message = "Mês de referência inválido. Use o formato MM/AAAA")
    @NotNull(message = "O mês de referência é obrigatório")
    private String mesReferencia;

    @Column(nullable = false)
    @NotNull(message = "O valor é obrigatório")
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDate dataVencimento;

    private LocalDate dataPagamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPagamento status = StatusPagamento.PENDENTE;

    @Column(length = 500)
    private String observacao;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime dataAtualizacao;

    public enum StatusPagamento {
        PAGO, PENDENTE, ATRASADO, CANCELADO
    }

    @PreUpdate
    @PrePersist
    public void verificaStatus() {
        if (this.status == StatusPagamento.CANCELADO) {
            // Don't change status if it's already CANCELADO
            return;
        }
        
        if (this.dataPagamento != null) {
            this.status = StatusPagamento.PAGO;
        } else if (this.dataVencimento.isBefore(LocalDate.now())) {
            this.status = StatusPagamento.ATRASADO;
        }
    }
    
    @Override
    public String toString() {
        return "Pagamento{" +
               "id=" + id +
               ", mesReferencia='" + mesReferencia + '\'' +
               ", valor=" + valor +
               ", dataVencimento=" + dataVencimento +
               ", dataPagamento=" + dataPagamento +
               ", status=" + status +
               '}';
    }
} 