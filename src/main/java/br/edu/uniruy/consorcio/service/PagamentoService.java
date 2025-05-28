package br.edu.uniruy.consorcio.service;

import br.edu.uniruy.consorcio.model.Cota;
import br.edu.uniruy.consorcio.model.GrupoConsorcio;
import br.edu.uniruy.consorcio.model.Pagamento;
import br.edu.uniruy.consorcio.repository.PagamentoRepository;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final CotaService cotaService;

    @Autowired
    public PagamentoService(PagamentoRepository pagamentoRepository, CotaService cotaService) {
        this.pagamentoRepository = pagamentoRepository;
        this.cotaService = cotaService;
    }

    public List<Pagamento> listarTodos() {
        return pagamentoRepository.findAll();
    }

    public Pagamento buscarPorId(Long id) {
        return pagamentoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Pagamento não encontrado com ID: " + id));
    }

    public List<Pagamento> buscarPorCota(Long cotaId) {
        Cota cota = cotaService.buscarPorId(cotaId);
        return pagamentoRepository.findByCota(cota);
    }

    public List<Pagamento> buscarPorGrupoEStatus(Long grupoId, Pagamento.StatusPagamento status) {
        return pagamentoRepository.findByGrupoAndStatus(grupoId, status);
    }

    public List<Pagamento> buscarPagamentosAtrasadosPorGrupo(Long grupoId) {
        return pagamentoRepository.findPagamentosAtrasadosPorGrupo(
            grupoId, 
            LocalDate.now(), 
            Pagamento.StatusPagamento.PAGO, 
            Pagamento.StatusPagamento.CANCELADO
        );
    }

    @Transactional
    public Pagamento salvar(Pagamento pagamento) {
        if (pagamento.getDataCadastro() == null) {
            pagamento.setDataCadastro(LocalDateTime.now());
        }
        pagamento.setDataAtualizacao(LocalDateTime.now());
        validarPagamento(pagamento);
        return pagamentoRepository.save(pagamento);
    }

    @Transactional
    public Pagamento atualizar(Long id, Pagamento pagamento) {
        if (!pagamentoRepository.existsById(id)) {
            throw new EntityNotFoundException("Pagamento não encontrado com ID: " + id);
        }
        
        validarPagamento(pagamento);
        pagamento.setId(id);
        return pagamentoRepository.save(pagamento);
    }

    @Transactional
    public Pagamento registrarPagamento(Long id, LocalDate dataPagamento) {
        Pagamento pagamento = buscarPorId(id);
        
        if (pagamento.getStatus() == Pagamento.StatusPagamento.PAGO) {
            throw new IllegalStateException("Este pagamento já foi registrado");
        }
        
        if (pagamento.getStatus() == Pagamento.StatusPagamento.CANCELADO) {
            throw new IllegalStateException("Este pagamento está cancelado");
        }
        
        pagamento.setDataPagamento(dataPagamento);
        pagamento.setStatus(Pagamento.StatusPagamento.PAGO);
        
        return pagamentoRepository.save(pagamento);
    }

    @Transactional
    public Pagamento cancelarPagamento(Long id) {
        Pagamento pagamento = buscarPorId(id);
        
        if (pagamento.getStatus() == Pagamento.StatusPagamento.CANCELADO) {
            throw new IllegalStateException("Este pagamento já está cancelado");
        }
        
        pagamento.setStatus(Pagamento.StatusPagamento.CANCELADO);
        pagamento.setDataPagamento(null); // Clear payment date when canceling
        return pagamentoRepository.save(pagamento);
    }

    @Transactional
    public void excluir(Long id) {
        Pagamento pagamento = buscarPorId(id);
        
        if (pagamento.getStatus() == Pagamento.StatusPagamento.PAGO) {
            throw new IllegalStateException("Não é possível excluir um pagamento já realizado");
        }
        
        pagamentoRepository.delete(pagamento);
    }
    
    private void validarPagamento(Pagamento pagamento) {
        if (pagamento.getCota() == null) {
            throw new IllegalArgumentException("A cota é obrigatória");
        }
        
        // Verifica se a cota existe
        Cota cota = cotaService.buscarPorId(pagamento.getCota().getId());
        pagamento.setCota(cota);
        
        // Verifica se já existe um pagamento para o mesmo mês
        pagamentoRepository.findByCotaAndMesReferencia(cota, pagamento.getMesReferencia())
            .ifPresent(p -> {
                if (pagamento.getId() == null || !p.getId().equals(pagamento.getId())) {
                    throw new IllegalArgumentException("Já existe um pagamento para a cota " + 
                        cota.getNumero() + " no mês " + pagamento.getMesReferencia());
                }
            });
        
        // Verifica se a data de vencimento é válida
        if (pagamento.getDataVencimento() == null) {
            // Tenta extrair mês e ano da referência (formato MM/AAAA)
            try {
                String[] partes = pagamento.getMesReferencia().split("/");
                int mes = Integer.parseInt(partes[0]);
                int ano = Integer.parseInt(partes[1]);
                
                // Define o vencimento para o dia 10 do mês de referência
                pagamento.setDataVencimento(LocalDate.of(ano, mes, 10));
            } catch (Exception e) {
                throw new IllegalArgumentException("Formato de mês de referência inválido. Use MM/AAAA");
            }
        }
        
        // Define o valor do pagamento baseado na cota, se não for informado
        if (pagamento.getValor() == null) {
            pagamento.setValor(cota.getValorParcela());
        }
        
        // Atualiza o status conforme a data de pagamento
        if (pagamento.getDataPagamento() != null) {
            pagamento.setStatus(Pagamento.StatusPagamento.PAGO);
        } else if (pagamento.getDataVencimento().isBefore(LocalDate.now())) {
            pagamento.setStatus(Pagamento.StatusPagamento.ATRASADO);
        } else {
            pagamento.setStatus(Pagamento.StatusPagamento.PENDENTE);
        }
    }

    @Transactional
    public int gerarPagamentosParaGrupo(Long grupoId, String mesReferencia, LocalDate dataVencimento, 
                                       boolean sobrescrever) {
        GrupoConsorcio grupo = cotaService.buscarGrupoPorId(grupoId);
        
        if (grupo.getStatus() != GrupoConsorcio.StatusGrupo.ATIVO) {
            throw new IllegalStateException("Não é possível gerar pagamentos para um grupo que não está ativo");
        }
        
        // Busca todas as cotas ativas do grupo
        List<Cota> cotasAtivas = cotaService.buscarCotasAtivasPorGrupo(grupoId);
        
        if (cotasAtivas.isEmpty()) {
            throw new IllegalStateException("Não existem cotas ativas neste grupo");
        }
        
        int pagamentosGerados = 0;
        
        for (Cota cota : cotasAtivas) {
            // Verifica se já existe um pagamento para este mês e cota
            boolean pagamentoExiste = pagamentoRepository.findByCotaAndMesReferencia(cota, mesReferencia).isPresent();
            
            if (pagamentoExiste && !sobrescrever) {
                continue;
            }
            
            // Se existe e deve sobrescrever, exclui o existente
            if (pagamentoExiste && sobrescrever) {
                Pagamento pagamentoExistente = pagamentoRepository.findByCotaAndMesReferencia(cota, mesReferencia).get();
                
                // Só substitui se não estiver pago
                if (pagamentoExistente.getStatus() == Pagamento.StatusPagamento.PAGO) {
                    continue;
                }
                
                pagamentoRepository.delete(pagamentoExistente);
            }
            
            // Cria novo pagamento
            Pagamento novoPagamento = new Pagamento();
            novoPagamento.setCota(cota);
            novoPagamento.setMesReferencia(mesReferencia);
            novoPagamento.setValor(cota.getValorParcela());
            novoPagamento.setDataVencimento(dataVencimento);
            novoPagamento.setStatus(Pagamento.StatusPagamento.PENDENTE);
            
            pagamentoRepository.save(novoPagamento);
            pagamentosGerados++;
        }
        
        return pagamentosGerados;
    }

    @Transactional
    public void atualizarStatusPagamentosAtrasados() {
        // Find all pending payments with past due dates and update them to ATRASADO
        List<Pagamento> pagamentosVencidos = pagamentoRepository.findAll()
            .stream()
            .filter(p -> p.getStatus() == Pagamento.StatusPagamento.PENDENTE 
                   && p.getDataVencimento().isBefore(LocalDate.now()))
            .collect(Collectors.toList());
            
        for (Pagamento pagamento : pagamentosVencidos) {
            pagamento.setStatus(Pagamento.StatusPagamento.ATRASADO);
            pagamentoRepository.save(pagamento);
        }
    }

    public Page<Pagamento> listarTodosPaginado(Pageable pageable) {
        return pagamentoRepository.findAll(pageable);
    }

    public List<Pagamento> listarPorCota(Long cotaId) {
        return pagamentoRepository.findByCotaId(cotaId);
    }

    public List<Pagamento> listarPorGrupo(Long grupoId) {
        return pagamentoRepository.findByGrupoId(grupoId);
    }

    public List<Pagamento> listarPorStatus(String status) {
        try {
            Pagamento.StatusPagamento statusEnum = Pagamento.StatusPagamento.valueOf(status);
            return pagamentoRepository.findByStatus(statusEnum);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status inválido: " + status);
        }
    }

    public List<Pagamento> listarVencidos() {
        return pagamentoRepository.findByDataVencimentoBeforeAndStatusNot(LocalDate.now(), Pagamento.StatusPagamento.PAGO);
    }

    public Pagamento processar(Long id, String status) {
        Pagamento pagamento = buscarPorId(id);
        try {
            Pagamento.StatusPagamento statusEnum = Pagamento.StatusPagamento.valueOf(status);
            pagamento.setStatus(statusEnum);
            
            if (statusEnum == Pagamento.StatusPagamento.PAGO) {
                pagamento.setDataPagamento(LocalDate.now());
            }
            
            return salvar(pagamento);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status inválido: " + status);
        }
    }
    
    // Métodos para relatórios
    
    public long contarPagamentosPorStatus(String status, Long grupoId, YearMonth periodo) {
        LocalDate inicio = periodo.atDay(1);
        LocalDate fim = periodo.atEndOfMonth();
        
        try {
            Pagamento.StatusPagamento statusEnum = Pagamento.StatusPagamento.valueOf(status);
            
            if (grupoId != null) {
                return pagamentoRepository.countByStatusAndCotaGrupoIdAndDataVencimentoBetween(statusEnum, grupoId, inicio, fim);
            } else {
                return pagamentoRepository.countByStatusAndDataVencimentoBetween(statusEnum, inicio, fim);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status inválido: " + status);
        }
    }
    
    public long contarPagamentosVencidos(Long grupoId) {
        LocalDate hoje = LocalDate.now();
        
        if (grupoId != null) {
            return pagamentoRepository.countByStatusNotAndDataVencimentoBeforeAndCotaGrupoId(Pagamento.StatusPagamento.PAGO, hoje, grupoId);
        } else {
            return pagamentoRepository.countByStatusNotAndDataVencimentoBefore(Pagamento.StatusPagamento.PAGO, hoje);
        }
    }
    
    public double calcularValorTotalPorStatus(String status, Long grupoId, YearMonth periodo) {
        LocalDate inicio = periodo.atDay(1);
        LocalDate fim = periodo.atEndOfMonth();
        
        try {
            Pagamento.StatusPagamento statusEnum = Pagamento.StatusPagamento.valueOf(status);
            List<Pagamento> pagamentos;
            
            if (grupoId != null) {
                pagamentos = pagamentoRepository.findByStatusAndCotaGrupoIdAndDataVencimentoBetween(statusEnum, grupoId, inicio, fim);
            } else {
                pagamentos = pagamentoRepository.findByStatusAndDataVencimentoBetween(statusEnum, inicio, fim);
            }
            
            return pagamentos.stream()
                    .mapToDouble(p -> p.getValor().doubleValue())
                    .sum();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status inválido: " + status);
        }
    }
    
    public double calcularTaxaAdimplencia(Long grupoId, YearMonth periodo) {
        LocalDate inicio = periodo.atDay(1);
        LocalDate fim = periodo.atEndOfMonth();
        
        long totalPagamentos;
        long pagamentosEmDia;
        
        if (grupoId != null) {
            totalPagamentos = pagamentoRepository.countByCotaGrupoIdAndDataVencimentoBetween(grupoId, inicio, fim);
            pagamentosEmDia = pagamentoRepository.countByStatusAndCotaGrupoIdAndDataVencimentoBetween(Pagamento.StatusPagamento.PAGO, grupoId, inicio, fim);
        } else {
            totalPagamentos = pagamentoRepository.countByDataVencimentoBetween(inicio, fim);
            pagamentosEmDia = pagamentoRepository.countByStatusAndDataVencimentoBetween(Pagamento.StatusPagamento.PAGO, inicio, fim);
        }
        
        if (totalPagamentos == 0) {
            return 0.0;
        }
        
        return (double) pagamentosEmDia / totalPagamentos * 100;
    }
    
    public Map<YearMonth, Double> gerarHistoricoMensalPagamentos(int meses, Long grupoId) {
        Map<YearMonth, Double> historico = new TreeMap<>();
        YearMonth mesAtual = YearMonth.now();
        
        for (int i = meses - 1; i >= 0; i--) {
            YearMonth mes = mesAtual.minusMonths(i);
            double valorTotal = calcularValorTotalPorStatus("PAGO", grupoId, mes);
            historico.put(mes, valorTotal);
        }
        
        return historico;
    }
} 