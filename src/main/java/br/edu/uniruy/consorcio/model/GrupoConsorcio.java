package br.edu.uniruy.consorcio.model;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "grupos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrupoConsorcio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do grupo é obrigatório")
    @Column(nullable = false)
    private String nome;

    @NotNull(message = "O valor do bem é obrigatório")
    @Min(value = 1, message = "O valor do bem deve ser maior que zero")
    @Column(nullable = false, name = "valor_bem")
    private BigDecimal valorBem;

    @NotNull(message = "O número de cotas é obrigatório")
    @Min(value = 2, message = "O grupo deve ter pelo menos 2 cotas")
    @Column(nullable = false, name = "num_cotas")
    private Integer numeroCotas;

    @NotNull(message = "O prazo em meses é obrigatório")
    @Min(value = 1, message = "O prazo deve ser de pelo menos 1 mês")
    @Column(nullable = false, name = "prazo_meses")
    private Integer prazoMeses;

    @Column(nullable = false)
    private LocalDate dataInicio;

    @Column(nullable = false)
    private LocalDate dataTermino;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusGrupo status = StatusGrupo.FORMACAO;

    @OneToMany(mappedBy = "grupo", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude // Prevent circular reference in toString
    private List<Cota> cotas = new ArrayList<>();

    @OneToMany(mappedBy = "grupo", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude // Prevent circular reference in toString
    private List<Sorteio> sorteios = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime dataAtualizacao;

    public enum StatusGrupo {
        FORMACAO, ATIVO, ENCERRADO
    }

    @PrePersist
    public void prePersist() {
        if (dataInicio == null) {
            dataInicio = LocalDate.now();
        }
        if (dataTermino == null && prazoMeses != null) {
            dataTermino = dataInicio.plusMonths(prazoMeses);
        }
    }
    
    // Custom toString method to prevent circular reference overflow
    @Override
    public String toString() {
        return "GrupoConsorcio{" +
               "id=" + id +
               ", nome='" + nome + '\'' +
               ", valorBem=" + valorBem +
               ", numeroCotas=" + numeroCotas +
               ", prazoMeses=" + prazoMeses +
               ", status=" + status +
               '}';
    }
} 