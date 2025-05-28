package br.edu.uniruy.consorcio.repository;

import br.edu.uniruy.consorcio.model.Cota;
import br.edu.uniruy.consorcio.model.GrupoConsorcio;
import br.edu.uniruy.consorcio.model.Sorteio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SorteioRepository extends JpaRepository<Sorteio, Long> {
    
    List<Sorteio> findByGrupo(GrupoConsorcio grupo);
    
    Page<Sorteio> findByGrupo(GrupoConsorcio grupo, Pageable pageable);
    
    List<Sorteio> findByDataSorteioBetween(LocalDate inicio, LocalDate fim);
    
    Page<Sorteio> findByDataSorteioBetween(LocalDate inicio, LocalDate fim, Pageable pageable);
    
    List<Sorteio> findByCotaContemplada(Cota cota);
    
    Optional<Sorteio> findByGrupoAndMesAno(GrupoConsorcio grupo, String mesAno);
    
    List<Sorteio> findByRealizado(Boolean realizado);

    List<Sorteio> findByGrupoId(Long grupoId);
    
    // Método removido - não existe propriedade Status
    // List<Sorteio> findByStatus(String status);
    
    // Métodos para relatórios
    List<Sorteio> findByDataSorteioBetweenAndGrupoId(LocalDate dataInicio, LocalDate dataFim, Long grupoId);
    
    // Método atualizado para usar o campo realizado
    long countByRealizado(Boolean realizado);
    
    long countByGrupoId(Long grupoId);
    
    long countByDataSorteioBetween(LocalDate dataInicio, LocalDate dataFim);
} 