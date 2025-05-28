package br.edu.uniruy.consorcio.controller;

import br.edu.uniruy.consorcio.model.Pagamento;
import br.edu.uniruy.consorcio.service.CotaService;
import br.edu.uniruy.consorcio.service.PagamentoService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/pagamentos")
public class PagamentoController {

    private final PagamentoService pagamentoService;
    private final CotaService cotaService;

    @Autowired
    public PagamentoController(PagamentoService pagamentoService, CotaService cotaService) {
        this.pagamentoService = pagamentoService;
        this.cotaService = cotaService;
    }

    @GetMapping
    public String index(Model model) {
        // Update status of overdue payments
        pagamentoService.atualizarStatusPagamentosAtrasados();
        
        // Get all payments
        model.addAttribute("pagamentos", pagamentoService.listarTodos());
        return "pagamentos/lista";
    }

    @GetMapping("/cadastro")
    public String formCadastro(Model model, @RequestParam(required = false) Long cotaId) {
        Pagamento pagamento = new Pagamento();
        
        if (cotaId != null) {
            pagamento.setCota(cotaService.buscarPorId(cotaId));
        }
        
        model.addAttribute("pagamento", pagamento);
        model.addAttribute("cotas", cotaService.listarTodas());
        return "pagamentos/form";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@Valid @ModelAttribute("pagamento") Pagamento pagamento, 
                            BindingResult result, 
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("cotas", cotaService.listarTodas());
            return "pagamentos/form";
        }
        
        try {
            pagamentoService.salvar(pagamento);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Pagamento cadastrado com sucesso!");
            
            // Redireciona para detalhes da cota se o pagamento foi criado a partir dela
            if (pagamento.getCota() != null) {
                return "redirect:/cotas/" + pagamento.getCota().getId() + "/detalhes";
            }
            
            return "redirect:/pagamentos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao cadastrar pagamento: " + e.getMessage());
            return "redirect:/pagamentos/cadastro";
        }
    }

    @GetMapping("/{id}/editar")
    public String formEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("pagamento", pagamentoService.buscarPorId(id));
            model.addAttribute("cotas", cotaService.listarTodas());
            return "pagamentos/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Pagamento não encontrado!");
            return "redirect:/pagamentos";
        }
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Long id, 
                         @Valid @ModelAttribute("pagamento") Pagamento pagamento, 
                         BindingResult result, 
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("cotas", cotaService.listarTodas());
            return "pagamentos/form";
        }
        
        try {
            pagamento.setId(id);
            pagamentoService.atualizar(id, pagamento);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Pagamento atualizado com sucesso!");
            return "redirect:/pagamentos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao atualizar pagamento: " + e.getMessage());
            return "redirect:/pagamentos/" + id + "/editar";
        }
    }

    @GetMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Pagamento pagamento = pagamentoService.buscarPorId(id);
            Long cotaId = pagamento.getCota().getId();
            
            pagamentoService.excluir(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Pagamento excluído com sucesso!");
            
            // Retorna para a página de detalhes da cota
            return "redirect:/cotas/" + cotaId + "/detalhes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao excluir pagamento: " + e.getMessage());
            return "redirect:/pagamentos";
        }
    }

    @GetMapping("/{id}/detalhes")
    public String detalhes(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("pagamento", pagamentoService.buscarPorId(id));
            return "pagamentos/detalhes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Pagamento não encontrado!");
            return "redirect:/pagamentos";
        }
    }

    @GetMapping("/{id}/registrar")
    public String formRegistrarPagamento(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("pagamento", pagamentoService.buscarPorId(id));
            model.addAttribute("dataPagamento", LocalDate.now());
            return "pagamentos/registrar";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Pagamento não encontrado!");
            return "redirect:/pagamentos";
        }
    }

    @PostMapping("/{id}/registrar")
    public String registrarPagamento(@PathVariable Long id, 
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataPagamento,
                                    RedirectAttributes redirectAttributes) {
        try {
            Pagamento pagamento = pagamentoService.registrarPagamento(id, dataPagamento);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Pagamento registrado com sucesso!");
            return "redirect:/pagamentos/" + id + "/detalhes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao registrar pagamento: " + e.getMessage());
            return "redirect:/pagamentos/" + id + "/registrar";
        }
    }

    @GetMapping("/{id}/cancelar")
    public String cancelarPagamento(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Pagamento pagamento = pagamentoService.cancelarPagamento(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Pagamento cancelado com sucesso!");
            return "redirect:/pagamentos/" + id + "/detalhes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao cancelar pagamento: " + e.getMessage());
            return "redirect:/pagamentos";
        }
    }

    @GetMapping("/cota/{cotaId}")
    public String pagamentosPorCota(@PathVariable Long cotaId, Model model) {
        model.addAttribute("pagamentos", pagamentoService.buscarPorCota(cotaId));
        model.addAttribute("cota", cotaService.buscarPorId(cotaId));
        return "pagamentos/lista";
    }

    @GetMapping("/gerar")
    public String formGerarPagamentos(Model model) {
        // Busca todos os grupos ativos para exibir no formulário
        model.addAttribute("grupos", cotaService.listarGruposAtivos());
        return "pagamentos/gerar";
    }

    @PostMapping("/gerar")
    public String gerarPagamentos(
            @RequestParam Long grupoId,
            @RequestParam String mesReferencia,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataVencimento,
            @RequestParam(required = false, defaultValue = "false") boolean sobrescrever,
            RedirectAttributes redirectAttributes) {
        
        try {
            int pagamentosGerados = pagamentoService.gerarPagamentosParaGrupo(
                    grupoId, mesReferencia, dataVencimento, sobrescrever);
            
            redirectAttributes.addFlashAttribute("mensagemSucesso", 
                    "Pagamentos gerados com sucesso! Foram criados " + pagamentosGerados + " pagamentos.");
            return "redirect:/pagamentos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao gerar pagamentos: " + e.getMessage());
            return "redirect:/pagamentos/gerar";
        }
    }
} 