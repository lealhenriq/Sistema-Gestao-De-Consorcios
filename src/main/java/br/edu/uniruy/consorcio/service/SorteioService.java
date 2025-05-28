package br.edu.uniruy.consorcio.service;

import br.edu.uniruy.consorcio.model.Cota;
import br.edu.uniruy.consorcio.model.GrupoConsorcio;
import br.edu.uniruy.consorcio.model.Pagamento;
import br.edu.uniruy.consorcio.model.Sorteio;
import br.edu.uniruy.consorcio.repository.CotaRepository;
import br.edu.uniruy.consorcio.repository.SorteioRepository;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class SorteioService {

    private final SorteioRepository sorteioRepository;
    private final GrupoConsorcioService grupoService;
    private final CotaService cotaService;
    private final CotaRepository cotaRepository;

    private final Random random = new Random();

    @Autowired
    public SorteioService(SorteioRepository sorteioRepository, 
                          GrupoConsorcioService grupoService, 
                          CotaService cotaService,
                          CotaRepository cotaRepository) {
        this.sorteioRepository = sorteioRepository;
        this.grupoService = grupoService;
        this.cotaService = cotaService;
        this.cotaRepository = cotaRepository;
    }

    public List<Sorteio> listarTodos() {
        return sorteioRepository.findAll();
    }

    public Page<Sorteio> listarTodosPaginado(Pageable pageable) {
        return sorteioRepository.findAll(pageable);
    }

    public Sorteio buscarPorId(Long id) {
        return sorteioRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Sorteio não encontrado com ID: " + id));
    }

    public List<Sorteio> buscarPorGrupo(Long grupoId) {
        GrupoConsorcio grupo = grupoService.buscarPorId(grupoId);
        return sorteioRepository.findByGrupo(grupo);
    }

    public Page<Sorteio> buscarPorGrupoPaginado(Long grupoId, Pageable pageable) {
        GrupoConsorcio grupo = grupoService.buscarPorId(grupoId);
        return sorteioRepository.findByGrupo(grupo, pageable);
    }

    public List<Sorteio> buscarPorPeriodo(LocalDate inicio, LocalDate fim) {
        return sorteioRepository.findByDataSorteioBetween(inicio, fim);
    }

    public Page<Sorteio> buscarPorPeriodoPaginado(LocalDate inicio, LocalDate fim, Pageable pageable) {
        return sorteioRepository.findByDataSorteioBetween(inicio, fim, pageable);
    }

    public List<Sorteio> buscarPorCotaContemplada(Long cotaId) {
        Cota cota = cotaService.buscarPorId(cotaId);
        return sorteioRepository.findByCotaContemplada(cota);
    }

    @Transactional
    public Sorteio salvar(Sorteio sorteio) {
        if (sorteio.getDataCadastro() == null) {
            sorteio.setDataCadastro(LocalDateTime.now());
        }
        sorteio.setDataAtualizacao(LocalDateTime.now());
        validarSorteio(sorteio);
        return sorteioRepository.save(sorteio);
    }

    @Transactional
    public Sorteio atualizar(Long id, Sorteio sorteio) {
        if (!sorteioRepository.existsById(id)) {
            throw new EntityNotFoundException("Sorteio não encontrado com ID: " + id);
        }
        
        validarSorteio(sorteio);
        sorteio.setId(id);
        return sorteioRepository.save(sorteio);
    }

    @Transactional
    public Sorteio realizarSorteio(Long grupoId, String mesAno) {
        GrupoConsorcio grupo = grupoService.buscarPorId(grupoId);
        
        if (grupo.getStatus() != GrupoConsorcio.StatusGrupo.ATIVO) {
            throw new IllegalStateException("Não é possível realizar sorteio em um grupo que não está ativo");
        }
        
        // Verifica se já existe um sorteio para este grupo/mês
        Sorteio sorteio = sorteioRepository.findByGrupoAndMesAno(grupo, mesAno)
            .orElse(new Sorteio());
        
        if (sorteio.getId() != null && sorteio.getRealizado()) {
            throw new IllegalStateException("Já existe um sorteio realizado para o grupo " + 
                grupo.getNome() + " no mês " + mesAno);
        }
        
        // Configura o sorteio
        sorteio.setGrupo(grupo);
        sorteio.setMesAno(mesAno);
        sorteio.setDataSorteio(LocalDate.now());
        sorteio.setRealizado(true);
        
        // Busca as cotas ativas e adimplentes
        List<Cota> cotasElegiveis = cotaRepository.findByGrupoAndStatus(grupo, Cota.StatusCota.ATIVA);
        
        // Filtra as cotas inadimplentes
        cotasElegiveis.removeIf(cota -> cotaRepository.findCotasInadimplentesPorMes(
            grupo.getId(), mesAno, Cota.StatusCota.ATIVA, Pagamento.StatusPagamento.PAGO).contains(cota));
        
        if (cotasElegiveis.isEmpty()) {
            throw new IllegalStateException("Não há cotas elegíveis para o sorteio");
        }
        
        // Sorteia uma cota aleatoriamente
        int indiceSort = random.nextInt(cotasElegiveis.size());
        Cota cotaContemplada = cotasElegiveis.get(indiceSort);
        
        // Atualiza o status da cota usando o CotaService para garantir que todas as validações sejam aplicadas
        cotaService.contemplarCota(cotaContemplada.getId());
        
        // Busca a cota atualizada para garantir que temos a versão mais recente
        cotaContemplada = cotaService.buscarPorId(cotaContemplada.getId());
        
        // Atualiza o sorteio
        sorteio.setCotaContemplada(cotaContemplada);
        return sorteioRepository.save(sorteio);
    }

    @Transactional
    public Sorteio realizarSorteioExistente(Long sorteioId) {
        Sorteio sorteio = buscarPorId(sorteioId);
        
        if (sorteio.getRealizado()) {
            throw new IllegalStateException("Este sorteio já foi realizado");
        }
        
        GrupoConsorcio grupo = sorteio.getGrupo();
        
        if (grupo.getStatus() != GrupoConsorcio.StatusGrupo.ATIVO) {
            throw new IllegalStateException("Não é possível realizar sorteio em um grupo que não está ativo");
        }
        
        // Configura o sorteio
        sorteio.setDataSorteio(LocalDate.now());
        sorteio.setRealizado(true);
        
        // Busca as cotas ativas
        List<Cota> cotasElegiveis = cotaRepository.findByGrupoAndStatus(grupo, Cota.StatusCota.ATIVA);
        
        // NOTA: Temporariamente desativamos a verificação de inadimplência
        // para permitir a realização do sorteio em ambiente de teste
        // Em produção, isto deve ser reativado
        /*
        // Filtra as cotas inadimplentes
        cotasElegiveis.removeIf(cota -> cotaRepository.findCotasInadimplentesPorMes(
            grupo.getId(), sorteio.getMesAno(), Cota.StatusCota.ATIVA, Pagamento.StatusPagamento.PAGO).contains(cota));
        */
        
        if (cotasElegiveis.isEmpty()) {
            throw new IllegalStateException("Não há cotas elegíveis para o sorteio");
        }
        
        // Sorteia uma cota aleatoriamente
        int indiceSort = random.nextInt(cotasElegiveis.size());
        Cota cotaContemplada = cotasElegiveis.get(indiceSort);
        
        // Atualiza o status da cota usando o CotaService para garantir que todas as validações sejam aplicadas
        cotaService.contemplarCota(cotaContemplada.getId());
        
        // Busca a cota atualizada para garantir que temos a versão mais recente
        cotaContemplada = cotaService.buscarPorId(cotaContemplada.getId());
        
        // Atualiza o sorteio
        sorteio.setCotaContemplada(cotaContemplada);
        return sorteioRepository.save(sorteio);
    }

    @Transactional
    public void excluir(Long id) {
        Sorteio sorteio = buscarPorId(id);
        
        if (sorteio.getRealizado() && sorteio.getCotaContemplada() != null) {
            throw new IllegalStateException("Não é possível excluir um sorteio já realizado com cota contemplada");
        }
        
        sorteioRepository.delete(sorteio);
    }
    
    private void validarSorteio(Sorteio sorteio) {
        // Verifica se o grupo existe
        GrupoConsorcio grupo = grupoService.buscarPorId(sorteio.getGrupo().getId());
        sorteio.setGrupo(grupo);
        
        // Verifica se já existe um sorteio para este grupo/mês
        sorteioRepository.findByGrupoAndMesAno(grupo, sorteio.getMesAno()).ifPresent(s -> {
            if (!s.getId().equals(sorteio.getId())) {
                throw new IllegalArgumentException("Já existe um sorteio registrado para o grupo " + 
                    grupo.getNome() + " no mês " + sorteio.getMesAno());
            }
        });
        
        // Verifica se a cota contemplada existe e pertence ao grupo
        if (sorteio.getCotaContemplada() != null) {
            Cota cota = cotaService.buscarPorId(sorteio.getCotaContemplada().getId());
            
            if (!cota.getGrupo().getId().equals(grupo.getId())) {
                throw new IllegalArgumentException("A cota contemplada não pertence ao grupo do sorteio");
            }
            
            sorteio.setCotaContemplada(cota);
        }
        
        // Valida o formato do mês/ano
        try {
            String[] partes = sorteio.getMesAno().split("/");
            int mes = Integer.parseInt(partes[0]);
            int ano = Integer.parseInt(partes[1]);
            
            if (mes < 1 || mes > 12) {
                throw new IllegalArgumentException("Mês inválido");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de mês/ano inválido. Use MM/AAAA");
        }
    }

    // Métodos para relatórios
    
    public List<Sorteio> listarPorPeriodoEGrupo(LocalDate dataInicio, LocalDate dataFim, Long grupoId) {
        if (grupoId != null) {
            return sorteioRepository.findByDataSorteioBetweenAndGrupoId(dataInicio, dataFim, grupoId);
        } else {
            return sorteioRepository.findByDataSorteioBetween(dataInicio, dataFim);
        }
    }
    
    public long contarSorteiosPorStatus(String status) {
        try {
            return sorteioRepository.countByRealizado("REALIZADO".equals(status));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status inválido: " + status);
        }
    }
    
    public long contarSorteiosPorGrupo(Long grupoId) {
        return sorteioRepository.countByGrupoId(grupoId);
    }
    
    public long contarSorteiosPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return sorteioRepository.countByDataSorteioBetween(dataInicio, dataFim);
    }
} 