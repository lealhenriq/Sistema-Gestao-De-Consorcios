package br.edu.uniruy.consorcio.repository;

import br.edu.uniruy.consorcio.model.GrupoConsorcio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrupoConsorcioRepository extends JpaRepository<GrupoConsorcio, Long> {
    
    List<GrupoConsorcio> findByNomeContainingIgnoreCase(String nome);
    
    List<GrupoConsorcio> findByStatus(GrupoConsorcio.StatusGrupo status);
} 