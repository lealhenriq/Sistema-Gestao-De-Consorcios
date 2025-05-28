package br.edu.uniruy.consorcio.repository;

import br.edu.uniruy.consorcio.model.Cliente;
import br.edu.uniruy.consorcio.model.Cota;
import br.edu.uniruy.consorcio.model.GrupoConsorcio;
import br.edu.uniruy.consorcio.model.Pagamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CotaRepository extends JpaRepository<Cota, Long> {
    
    List<Cota> findByCliente(Cliente cliente);
    
    List<Cota> findByGrupo(GrupoConsorcio grupo);
    
    Page<Cota> findByGrupo(GrupoConsorcio grupo, Pageable pageable);
    
    List<Cota> findByGrupoAndStatus(GrupoConsorcio grupo, Cota.StatusCota status);
    
    Optional<Cota> findByGrupoAndNumero(GrupoConsorcio grupo, Integer numero);
    
    @Query("SELECT c FROM Cota c WHERE c.grupo.id = :grupoId AND c.status = :status AND c.id NOT IN (SELECT p.cota.id FROM Pagamento p WHERE p.mesReferencia = :mesReferencia AND p.status = :pagamentoStatus)")
    List<Cota> findCotasInadimplentesPorMes(Long grupoId, String mesReferencia, 
        @Param("status") Cota.StatusCota status, 
        @Param("pagamentoStatus") Pagamento.StatusPagamento pagamentoStatus);
    
    @Query("SELECT COUNT(c) FROM Cota c WHERE c.grupo.id = :grupoId AND c.status = :status")
    Long countCotasAtivasPorGrupo(Long grupoId, @Param("status") Cota.StatusCota status);

    List<Cota> findByClienteId(Long clienteId);
    
    List<Cota> findByGrupoId(Long grupoId);
    
    List<Cota> findByStatus(Cota.StatusCota status);
    
    List<Cota> findByGrupoIdAndStatus(Long grupoId, Cota.StatusCota status);
    
    Page<Cota> findByGrupoId(Long grupoId, Pageable pageable);
    
    @Query("SELECT c FROM Cota c WHERE c.grupo.id = :grupoId AND c.contemplada = false AND c.status = :status")
    List<Cota> findCotasElegiveisParaSorteio(Long grupoId, @Param("status") Cota.StatusCota status);
    
    // Métodos para relatórios
    long countByStatus(Cota.StatusCota status);
    
    long countByGrupoId(Long grupoId);
    
    long countByGrupoIdAndStatus(Long grupoId, Cota.StatusCota status);
    
    long countByClienteId(Long clienteId);
    
    long countByContempladaTrue();
    
    long countByGrupoIdAndContempladaTrue(Long grupoId);
    
    long countByClienteIdAndContempladaTrue(Long clienteId);
} 