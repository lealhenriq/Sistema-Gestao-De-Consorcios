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
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sorteios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sorteio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "grupo_id", nullable = false)
    @NotNull(message = "O grupo é obrigatório")
    @ToString.Exclude
    private GrupoConsorcio grupo;

    @Column(nullable = false, name = "mes_ano")
    @Pattern(regexp = "\\d{2}/\\d{4}", message = "Mês/Ano inválido. Use o formato MM/AAAA")
    @NotNull(message = "O mês/ano é obrigatório")
    private String mesAno;

    @ManyToOne
    @JoinColumn(name = "cota_contemplada_id")
    @ToString.Exclude
    private Cota cotaContemplada;

    @Column(nullable = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataSorteio;

    @Column(nullable = false)
    private Boolean realizado = false;

    @Column(length = 500)
    private String observacao;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime dataAtualizacao;

    @PrePersist
    public void prePersist() {
        if (dataSorteio == null) {
            dataSorteio = LocalDate.now();
        }
    }
    
    @Override
    public String toString() {
        return "Sorteio{" +
               "id=" + id +
               ", mesAno='" + mesAno + '\'' +
               ", dataSorteio=" + dataSorteio +
               ", realizado=" + realizado +
               '}';
    }
} 