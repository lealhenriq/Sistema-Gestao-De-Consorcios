package br.edu.uniruy.consorcio.service;

import br.edu.uniruy.consorcio.model.Cliente;
import br.edu.uniruy.consorcio.model.Cota;
import br.edu.uniruy.consorcio.model.GrupoConsorcio;
import br.edu.uniruy.consorcio.model.Pagamento;
import br.edu.uniruy.consorcio.repository.CotaRepository;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CotaService {

    private final CotaRepository cotaRepository;
    private final ClienteService clienteService;
    private final GrupoConsorcioService grupoService;

    @Autowired
    public CotaService(CotaRepository cotaRepository, ClienteService clienteService, GrupoConsorcioService grupoService) {
        this.cotaRepository = cotaRepository;
        this.clienteService = clienteService;
        this.grupoService = grupoService;
    }

    public List<Cota> listarTodas() {
        return cotaRepository.findAll();
    }

    public Cota buscarPorId(Long id) {
        return cotaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Cota não encontrada com ID: " + id));
    }

    public List<Cota> buscarPorCliente(Long clienteId) {
        Cliente cliente = clienteService.buscarPorId(clienteId);
        return cotaRepository.findByCliente(cliente);
    }

    public List<Cota> buscarPorGrupo(Long grupoId) {
        GrupoConsorcio grupo = grupoService.buscarPorId(grupoId);
        return cotaRepository.findByGrupo(grupo);
    }

    public Page<Cota> buscarPorGrupoPaginado(Long grupoId, Pageable pageable) {
        GrupoConsorcio grupo = grupoService.buscarPorId(grupoId);
        return cotaRepository.findByGrupo(grupo, pageable);
    }

    public List<Cota> buscarCotasAtivasPorGrupo(Long grupoId) {
        GrupoConsorcio grupo = grupoService.buscarPorId(grupoId);
        return cotaRepository.findByGrupoAndStatus(grupo, Cota.StatusCota.ATIVA);
    }

    public List<Cota> buscarCotasInadimplentesPorMes(Long grupoId, String mesReferencia) {
        return cotaRepository.findCotasInadimplentesPorMes(grupoId, mesReferencia, 
            Cota.StatusCota.ATIVA, Pagamento.StatusPagamento.PAGO);
    }

    public List<GrupoConsorcio> listarGruposAtivos() {
        return grupoService.buscarPorStatus(GrupoConsorcio.StatusGrupo.ATIVO);
    }

    public GrupoConsorcio buscarGrupoPorId(Long id) {
        return grupoService.buscarPorId(id);
    }

    @Transactional
    public Cota salvar(Cota cota) {
        if (cota.getDataCadastro() == null) {
            cota.setDataCadastro(LocalDateTime.now());
        }
        cota.setDataAtualizacao(LocalDateTime.now());
        validarCota(cota);
        
        // Calcula o valor da parcela baseado no grupo
        if (cota.getValorParcela() == null) {
            GrupoConsorcio grupo = cota.getGrupo();
            BigDecimal valorParcela = grupo.getValorBem()
                .divide(BigDecimal.valueOf(grupo.getNumeroCotas()), 2, java.math.RoundingMode.HALF_UP);
            cota.setValorParcela(valorParcela);
        }
        
        return cotaRepository.save(cota);
    }

    @Transactional
    public Cota atualizar(Long id, Cota cota) {
        if (!cotaRepository.existsById(id)) {
            throw new EntityNotFoundException("Cota não encontrada com ID: " + id);
        }
        
        validarCota(cota);
        cota.setId(id);
        return cotaRepository.save(cota);
    }

    @Transactional
    public void excluir(Long id) {
        Cota cota = buscarPorId(id);
        
        if (cota.getStatus() != Cota.StatusCota.ATIVA) {
            throw new IllegalStateException("Não é possível excluir uma cota que já foi contemplada");
        }
        
        if (!cota.getPagamentos().isEmpty()) {
            throw new IllegalStateException("Não é possível excluir a cota pois ela possui pagamentos associados");
        }
        
        // Check if the cota is referenced as a cotaContemplada in any sorteio
        if (!cota.getSorteios().isEmpty()) {
            throw new IllegalStateException("Não é possível excluir a cota pois ela está contemplada em sorteios");
        }
        
        cotaRepository.delete(cota);
    }

    @Transactional
    public Cota inativarCota(Long id) {
        Cota cota = buscarPorId(id);
        
        if (cota.getStatus() != Cota.StatusCota.ATIVA) {
            throw new IllegalStateException("A cota já está inativa ou contemplada");
        }
        
        cota.setStatus(Cota.StatusCota.INATIVA);
        return cotaRepository.save(cota);
    }

    @Transactional
    public Cota contemplarCota(Long id) {
        Cota cota = buscarPorId(id);
        
        if (cota.getStatus() != Cota.StatusCota.ATIVA) {
            throw new IllegalStateException("A cota já está inativa ou contemplada");
        }
        
        cota.setStatus(Cota.StatusCota.CONTEMPLADA);
        cota.setContemplada(true);
        return cotaRepository.save(cota);
    }

    private void validarCota(Cota cota) {
        if (cota.getCliente() == null) {
            throw new IllegalArgumentException("O cliente é obrigatório");
        }
        
        if (cota.getGrupo() == null) {
            throw new IllegalArgumentException("O grupo de consórcio é obrigatório");
        }
        
        // Verifica se o cliente existe
        Cliente cliente = clienteService.buscarPorId(cota.getCliente().getId());
        cota.setCliente(cliente);
        
        // Verifica se o grupo existe
        GrupoConsorcio grupo = grupoService.buscarPorId(cota.getGrupo().getId());
        cota.setGrupo(grupo);
        
        // Verifica status do grupo
        if (grupo.getStatus() != GrupoConsorcio.StatusGrupo.ATIVO) {
            throw new IllegalArgumentException("Não é possível adicionar cotas a um grupo que não está ativo");
        }
        
        // Verifica se já existe uma cota com o mesmo número no grupo
        cotaRepository.findByGrupoAndNumero(grupo, cota.getNumero())
            .ifPresent(c -> {
                if (cota.getId() == null || !c.getId().equals(cota.getId())) {
                    throw new IllegalArgumentException("Já existe uma cota com o número " + 
                        cota.getNumero() + " no grupo " + grupo.getNome());
                }
            });
    }
    
    // Métodos para relatórios
    
    public long contarTodasCotas() {
        return cotaRepository.count();
    }
    
    public long contarCotasPorStatus(String status) {
        try {
            Cota.StatusCota statusEnum = Cota.StatusCota.valueOf(status);
            return cotaRepository.countByStatus(statusEnum);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status inválido: " + status);
        }
    }
    
    public long contarCotasPorGrupo(Long grupoId) {
        return cotaRepository.countByGrupoId(grupoId);
    }
    
    public long contarCotasPorGrupoEStatus(Long grupoId, String status) {
        try {
            Cota.StatusCota statusEnum = Cota.StatusCota.valueOf(status);
            return cotaRepository.countByGrupoIdAndStatus(grupoId, statusEnum);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status inválido: " + status);
        }
    }
    
    public long contarCotasPorCliente(Long clienteId) {
        return cotaRepository.countByClienteId(clienteId);
    }
    
    public long contarTodasCotasContempladas() {
        return cotaRepository.countByContempladaTrue();
    }
    
    public long contarCotasContempladas(Long grupoId) {
        return cotaRepository.countByGrupoIdAndContempladaTrue(grupoId);
    }
    
    public long contarCotasContempladasPorCliente(Long clienteId) {
        return cotaRepository.countByClienteIdAndContempladaTrue(clienteId);
    }
    
    public double calcularPercentualContemplacao(Long grupoId) {
        long totalCotas = contarCotasPorGrupo(grupoId);
        long cotasContempladas = contarCotasContempladas(grupoId);
        
        if (totalCotas == 0) {
            return 0.0;
        }
        
        return (double) cotasContempladas / totalCotas * 100;
    }
    
    public double calcularValorTotalParcelasPorCliente(Long clienteId) {
        List<Cota> cotas = buscarPorCliente(clienteId);
        return cotas.stream()
                .mapToDouble(cota -> cota.getValorParcela().doubleValue())
                .sum();
    }

    // Métodos adicionais para compatibilidade com RelatorioController
    public List<Cota> buscarPorGrupoEStatus(Long grupoId, String status) {
        try {
            Cota.StatusCota statusEnum = Cota.StatusCota.valueOf(status);
            return cotaRepository.findByGrupoIdAndStatus(grupoId, statusEnum);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status inválido: " + status);
        }
    }
    
    public List<Cota> buscarPorStatus(String status) {
        try {
            Cota.StatusCota statusEnum = Cota.StatusCota.valueOf(status);
            return cotaRepository.findByStatus(statusEnum);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status inválido: " + status);
        }
    }
} 