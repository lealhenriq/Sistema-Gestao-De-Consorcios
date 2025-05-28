package br.edu.uniruy.consorcio.controller;

import br.edu.uniruy.consorcio.service.*;
import br.edu.uniruy.consorcio.model.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/relatorios")
@PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
public class RelatorioController {

    private final ClienteService clienteService;
    private final GrupoConsorcioService grupoConsorcioService;
    private final CotaService cotaService;
    private final PagamentoService pagamentoService;
    private final SorteioService sorteioService;

    @Autowired
    public RelatorioController(ClienteService clienteService, 
                              GrupoConsorcioService grupoConsorcioService,
                              CotaService cotaService,
                              PagamentoService pagamentoService,
                              SorteioService sorteioService) {
        this.clienteService = clienteService;
        this.grupoConsorcioService = grupoConsorcioService;
        this.cotaService = cotaService;
        this.pagamentoService = pagamentoService;
        this.sorteioService = sorteioService;
    }

    @GetMapping
    public String paginaRelatorios() {
        return "relatorios/index";
    }

    @GetMapping("/financeiro")
    public String relatorioFinanceiro(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth periodo,
            @RequestParam(required = false) Long grupoId,
            Model model) {
        
        if (periodo == null) {
            periodo = YearMonth.now();
        }
        
        // Estatísticas de pagamentos
        Map<String, Object> estatisticas = new HashMap<>();
        estatisticas.put("totalPagamentos", pagamentoService.contarPagamentosPorStatus("PAGO", grupoId, periodo));
        estatisticas.put("totalPendentes", pagamentoService.contarPagamentosPorStatus("PENDENTE", grupoId, periodo));
        estatisticas.put("totalVencidos", pagamentoService.contarPagamentosVencidos(grupoId));
        estatisticas.put("valorTotalRecebido", pagamentoService.calcularValorTotalPorStatus("PAGO", grupoId, periodo));
        estatisticas.put("valorTotalPendente", pagamentoService.calcularValorTotalPorStatus("PENDENTE", grupoId, periodo));
        estatisticas.put("taxaAdimplencia", pagamentoService.calcularTaxaAdimplencia(grupoId, periodo));
        
        // Histórico mensal (últimos 6 meses)
        Map<YearMonth, Double> historicoMensal = pagamentoService.gerarHistoricoMensalPagamentos(6, grupoId);
        
        model.addAttribute("periodo", periodo);
        model.addAttribute("periodoFormatado", periodo.format(DateTimeFormatter.ofPattern("MMMM/yyyy")));
        model.addAttribute("estatisticas", estatisticas);
        model.addAttribute("historicoMensal", historicoMensal);
        model.addAttribute("grupos", grupoConsorcioService.listarTodos());
        model.addAttribute("grupoSelecionado", grupoId != null ? grupoConsorcioService.buscarPorId(grupoId) : null);
        
        return "relatorios/financeiro";
    }

    @GetMapping("/grupos")
    public String relatorioGrupos(
            @RequestParam(required = false) String status,
            Model model) {
        
        List<GrupoConsorcio> grupos;
        if (status != null && !status.isEmpty()) {
            grupos = grupoConsorcioService.listarPorStatus(status);
        } else {
            grupos = grupoConsorcioService.listarTodos();
        }
        
        // Estatísticas por grupo
        Map<Long, Map<String, Object>> estatisticasPorGrupo = new HashMap<>();
        for (GrupoConsorcio grupo : grupos) {
            Map<String, Object> estatisticas = new HashMap<>();
            estatisticas.put("totalCotas", cotaService.contarCotasPorGrupo(grupo.getId()));
            estatisticas.put("cotasAtivas", cotaService.contarCotasPorGrupoEStatus(grupo.getId(), Cota.StatusCota.ATIVA.name()));
            estatisticas.put("cotasContempladas", cotaService.contarCotasContempladas(grupo.getId()));
            estatisticas.put("percentualContemplacao", cotaService.calcularPercentualContemplacao(grupo.getId()));
            estatisticasPorGrupo.put(grupo.getId(), estatisticas);
        }
        
        // Contadores para substituir as expressões stream no template
        long totalGruposAtivos = 0;
        long totalGruposFormacao = 0;
        long totalGruposEncerrados = 0;
        
        for (GrupoConsorcio grupo : grupos) {
            if (grupo.getStatus() != null) {
                if (grupo.getStatus().equals("ATIVO")) {
                    totalGruposAtivos++;
                } else if (grupo.getStatus().equals("FORMACAO")) {
                    totalGruposFormacao++;
                } else if (grupo.getStatus().equals("ENCERRADO")) {
                    totalGruposEncerrados++;
                }
            }
        }
        
        model.addAttribute("grupos", grupos);
        model.addAttribute("statusSelecionado", status);
        model.addAttribute("todosStatus", GrupoConsorcio.StatusGrupo.values());
        model.addAttribute("estatisticasPorGrupo", estatisticasPorGrupo);
        model.addAttribute("totalGruposAtivos", totalGruposAtivos);
        model.addAttribute("totalGruposFormacao", totalGruposFormacao);
        model.addAttribute("totalGruposEncerrados", totalGruposEncerrados);
        
        return "relatorios/grupos";
    }

    @GetMapping("/clientes")
    public String relatorioClientes(Model model) {
        List<Cliente> clientes = clienteService.listarTodos();
        
        // Estatísticas por cliente
        Map<Long, Map<String, Object>> estatisticasPorCliente = new HashMap<>();
        for (Cliente cliente : clientes) {
            Map<String, Object> estatisticas = new HashMap<>();
            estatisticas.put("totalCotas", cotaService.contarCotasPorCliente(cliente.getId()));
            estatisticas.put("cotasContempladas", cotaService.contarCotasContempladasPorCliente(cliente.getId()));
            estatisticas.put("valorTotalParcelas", cotaService.calcularValorTotalParcelasPorCliente(cliente.getId()));
            estatisticasPorCliente.put(cliente.getId(), estatisticas);
        }
        
        model.addAttribute("clientes", clientes);
        model.addAttribute("estatisticasPorCliente", estatisticasPorCliente);
        
        return "relatorios/clientes";
    }

    @GetMapping("/sorteios")
    public String relatorioSorteios(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataFim,
            @RequestParam(required = false) Long grupoId,
            Model model) {
        
        if (dataInicio == null) {
            dataInicio = LocalDate.now().minusMonths(6);
        }
        if (dataFim == null) {
            dataFim = LocalDate.now();
        }
        
        List<Sorteio> sorteios = sorteioService.listarPorPeriodoEGrupo(dataInicio, dataFim, grupoId);
        
        // Estatísticas de sorteios
        int totalSorteios = sorteios.size();
        long sorteiosRealizados = sorteios.stream()
                .filter(s -> s.getRealizado())
                .count();
        
        model.addAttribute("sorteios", sorteios);
        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);
        model.addAttribute("totalSorteios", totalSorteios);
        model.addAttribute("sorteiosRealizados", sorteiosRealizados);
        model.addAttribute("grupos", grupoConsorcioService.listarTodos());
        model.addAttribute("grupoSelecionado", grupoId != null ? grupoConsorcioService.buscarPorId(grupoId) : null);
        
        return "relatorios/sorteios";
    }

    @GetMapping("/cotas")
    public String relatorioCotas(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long grupoId,
            Model model) {
        
        List<Cota> cotas;
        if (grupoId != null) {
            if (status != null && !status.isEmpty()) {
                cotas = cotaService.buscarPorGrupoEStatus(grupoId, status);
            } else {
                cotas = cotaService.buscarPorGrupo(grupoId);
            }
        } else if (status != null && !status.isEmpty()) {
            cotas = cotaService.buscarPorStatus(status);
        } else {
            cotas = cotaService.listarTodas();
        }
        
        // Estatísticas gerais
        long totalCotas = cotaService.contarTodasCotas();
        long cotasAtivas = cotaService.contarCotasPorStatus(Cota.StatusCota.ATIVA.name());
        long cotasContempladas = cotaService.contarTodasCotasContempladas();
        double percentualContemplacao = (double) cotasContempladas / totalCotas * 100;
        
        model.addAttribute("cotas", cotas);
        model.addAttribute("totalCotas", totalCotas);
        model.addAttribute("cotasAtivas", cotasAtivas);
        model.addAttribute("cotasContempladas", cotasContempladas);
        model.addAttribute("percentualContemplacao", percentualContemplacao);
        model.addAttribute("statusSelecionado", status);
        model.addAttribute("todosStatus", Cota.StatusCota.values());
        model.addAttribute("grupos", grupoConsorcioService.listarTodos());
        model.addAttribute("grupoSelecionado", grupoId != null ? grupoConsorcioService.buscarPorId(grupoId) : null);
        
        return "relatorios/cotas";
    }
} 