package br.edu.uniruy.consorcio.repository;

import br.edu.uniruy.consorcio.model.Cota;
import br.edu.uniruy.consorcio.model.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    
    List<Pagamento> findByCota(Cota cota);
    
    List<Pagamento> findByCotaAndStatus(Cota cota, Pagamento.StatusPagamento status);
    
    Optional<Pagamento> findByCotaAndMesReferencia(Cota cota, String mesReferencia);
    
    @Query("SELECT p FROM Pagamento p WHERE p.cota.grupo.id = :grupoId AND p.status = :status")
    List<Pagamento> findByGrupoAndStatus(Long grupoId, Pagamento.StatusPagamento status);
    
    @Query("SELECT p FROM Pagamento p WHERE (:grupoId IS NULL OR p.cota.grupo.id = :grupoId) AND p.dataVencimento < :dataLimite AND p.status != :statusPago AND p.status != :statusCancelado")
    List<Pagamento> findPagamentosAtrasadosPorGrupo(Long grupoId, LocalDate dataLimite, 
        @Param("statusPago") Pagamento.StatusPagamento statusPago, 
        @Param("statusCancelado") Pagamento.StatusPagamento statusCancelado);
    
    @Query("SELECT COUNT(p) FROM Pagamento p WHERE p.cota.id = :cotaId AND p.status = :status")
    Long countPagamentosPagosPorCota(Long cotaId, Pagamento.StatusPagamento status);

    List<Pagamento> findByCotaId(Long cotaId);
    
    @Query("SELECT p FROM Pagamento p JOIN p.cota c WHERE c.grupo.id = :grupoId")
    List<Pagamento> findByGrupoId(Long grupoId);
    
    List<Pagamento> findByStatus(Pagamento.StatusPagamento status);
    
    List<Pagamento> findByDataVencimentoBeforeAndStatusNot(LocalDate data, Pagamento.StatusPagamento status);
    
    // Métodos para relatórios
    @Query("SELECT COUNT(p) FROM Pagamento p WHERE p.status = :status AND p.dataVencimento BETWEEN :inicio AND :fim")
    long countByStatusAndDataVencimentoBetween(Pagamento.StatusPagamento status, LocalDate inicio, LocalDate fim);
    
    @Query("SELECT COUNT(p) FROM Pagamento p WHERE p.status = :status AND p.cota.grupo.id = :grupoId AND p.dataVencimento BETWEEN :inicio AND :fim")
    long countByStatusAndCotaGrupoIdAndDataVencimentoBetween(Pagamento.StatusPagamento status, Long grupoId, LocalDate inicio, LocalDate fim);
    
    @Query("SELECT COUNT(p) FROM Pagamento p WHERE p.status != :status AND p.dataVencimento < :data")
    long countByStatusNotAndDataVencimentoBefore(Pagamento.StatusPagamento status, LocalDate data);
    
    @Query("SELECT COUNT(p) FROM Pagamento p WHERE p.status != :status AND p.dataVencimento < :data AND p.cota.grupo.id = :grupoId")
    long countByStatusNotAndDataVencimentoBeforeAndCotaGrupoId(Pagamento.StatusPagamento status, LocalDate data, Long grupoId);
    
    @Query("SELECT p FROM Pagamento p WHERE p.status = :status AND p.dataVencimento BETWEEN :inicio AND :fim")
    List<Pagamento> findByStatusAndDataVencimentoBetween(Pagamento.StatusPagamento status, LocalDate inicio, LocalDate fim);
    
    @Query("SELECT p FROM Pagamento p WHERE p.status = :status AND p.cota.grupo.id = :grupoId AND p.dataVencimento BETWEEN :inicio AND :fim")
    List<Pagamento> findByStatusAndCotaGrupoIdAndDataVencimentoBetween(Pagamento.StatusPagamento status, Long grupoId, LocalDate inicio, LocalDate fim);
    
    long countByDataVencimentoBetween(LocalDate inicio, LocalDate fim);
    
    long countByCotaGrupoIdAndDataVencimentoBetween(Long grupoId, LocalDate inicio, LocalDate fim);
} 