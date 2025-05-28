package br.edu.uniruy.consorcio.controller;

import br.edu.uniruy.consorcio.model.GrupoConsorcio;
import br.edu.uniruy.consorcio.service.ClienteService;
import br.edu.uniruy.consorcio.service.CotaService;
import br.edu.uniruy.consorcio.service.GrupoConsorcioService;
import br.edu.uniruy.consorcio.service.PagamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final ClienteService clienteService;
    private final GrupoConsorcioService grupoService;
    private final CotaService cotaService;
    private final PagamentoService pagamentoService;

    @Autowired
    public HomeController(ClienteService clienteService,
                         GrupoConsorcioService grupoService,
                         CotaService cotaService,
                         PagamentoService pagamentoService) {
        this.clienteService = clienteService;
        this.grupoService = grupoService;
        this.cotaService = cotaService;
        this.pagamentoService = pagamentoService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Ensure overdue payments are marked as ATRASADO
        pagamentoService.atualizarStatusPagamentosAtrasados();
        
        // Total de clientes
        model.addAttribute("totalClientes", clienteService.listarTodos().size());
        
        // Total de grupos ativos
        model.addAttribute("totalGruposAtivos", 
            grupoService.buscarPorStatus(GrupoConsorcio.StatusGrupo.ATIVO).size());
        
        // Total de grupos em formação
        model.addAttribute("totalGruposFormacao", 
            grupoService.buscarPorStatus(GrupoConsorcio.StatusGrupo.FORMACAO).size());
        
        // Total de cotas
        model.addAttribute("totalCotas", cotaService.listarTodas().size());
        
        // Mês atual para referência em relatórios
        model.addAttribute("mesAtual", 
            LocalDate.now().format(DateTimeFormatter.ofPattern("MM/yyyy")));
        
        // Grupos mais recentes
        model.addAttribute("gruposRecentes", grupoService.listarTodos().stream()
            .sorted((g1, g2) -> g2.getDataCadastro().compareTo(g1.getDataCadastro()))
            .limit(5)
            .collect(Collectors.toList()));
        
        // Pagamentos pendentes (para o mês atual)
        model.addAttribute("pagamentosPendentes", 
            pagamentoService.buscarPorGrupoEStatus(null, br.edu.uniruy.consorcio.model.Pagamento.StatusPagamento.PENDENTE));
        
        // Pagamentos atrasados
        model.addAttribute("pagamentosAtrasados", 
            pagamentoService.buscarPagamentosAtrasadosPorGrupo(null));
            
        return "dashboard";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }
} 