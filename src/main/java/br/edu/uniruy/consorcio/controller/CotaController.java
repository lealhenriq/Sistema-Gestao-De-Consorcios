package br.edu.uniruy.consorcio.controller;

import br.edu.uniruy.consorcio.model.Cota;
import br.edu.uniruy.consorcio.service.ClienteService;
import br.edu.uniruy.consorcio.service.CotaService;
import br.edu.uniruy.consorcio.service.GrupoConsorcioService;
import br.edu.uniruy.consorcio.service.PagamentoService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cotas")
public class CotaController {

    private final CotaService cotaService;
    private final ClienteService clienteService;
    private final GrupoConsorcioService grupoService;
    private final PagamentoService pagamentoService;

    @Autowired
    public CotaController(CotaService cotaService, 
                           ClienteService clienteService, 
                           GrupoConsorcioService grupoService,
                           PagamentoService pagamentoService) {
        this.cotaService = cotaService;
        this.clienteService = clienteService;
        this.grupoService = grupoService;
        this.pagamentoService = pagamentoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("cotas", cotaService.listarTodas());
        return "cotas/lista";
    }

    @GetMapping("/cadastro")
    public String formCadastro(Model model, @RequestParam(required = false) Long grupoId) {
        Cota cota = new Cota();
        
        if (grupoId != null) {
            cota.setGrupo(grupoService.buscarPorId(grupoId));
        }
        
        model.addAttribute("cota", cota);
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("grupos", grupoService.listarTodos());
        return "cotas/form";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@Valid @ModelAttribute("cota") Cota cota, 
                            BindingResult result, 
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("clientes", clienteService.listarTodos());
            model.addAttribute("grupos", grupoService.listarTodos());
            return "cotas/form";
        }
        
        try {
            cotaService.salvar(cota);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Cota cadastrada com sucesso!");
            
            // Redireciona para detalhes do grupo se a cota foi criada a partir dele
            if (cota.getGrupo() != null) {
                return "redirect:/grupos/" + cota.getGrupo().getId() + "/detalhes";
            }
            return "redirect:/cotas";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao cadastrar cota: " + e.getMessage());
            return "redirect:/cotas/cadastro";
        }
    }

    @GetMapping("/{id}/editar")
    public String formEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Cota cota = cotaService.buscarPorId(id);
            model.addAttribute("cota", cota);
            model.addAttribute("clientes", clienteService.listarTodos());
            model.addAttribute("grupos", grupoService.listarTodos());
            return "cotas/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao buscar cota: " + e.getMessage());
            return "redirect:/cotas";
        }
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Long id, 
                         @Valid @ModelAttribute("cota") Cota cota, 
                         BindingResult result, 
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("clientes", clienteService.listarTodos());
            model.addAttribute("grupos", grupoService.listarTodos());
            return "cotas/form";
        }
        
        try {
            cota.setId(id);
            
            // Preserve the existing grupo if it's not set in the form
            if (cota.getGrupo() == null || cota.getGrupo().getId() == null) {
                Cota existingCota = cotaService.buscarPorId(id);
                cota.setGrupo(existingCota.getGrupo());
            }
            
            cotaService.atualizar(id, cota);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Cota atualizada com sucesso!");
            return "redirect:/cotas";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao atualizar cota: " + e.getMessage());
            return "redirect:/cotas/" + id + "/editar";
        }
    }

    @GetMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Cota cota = cotaService.buscarPorId(id);
            Long grupoId = cota.getGrupo().getId();
            
            cotaService.excluir(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Cota excluída com sucesso!");
            
            // Retorna para a página de detalhes do grupo
            return "redirect:/grupos/" + grupoId + "/detalhes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao excluir cota: " + e.getMessage());
            return "redirect:/cotas";
        }
    }

    @GetMapping("/{id}/detalhes")
    public String detalhes(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Cota cota = cotaService.buscarPorId(id);
            model.addAttribute("cota", cota);
            model.addAttribute("pagamentos", pagamentoService.buscarPorCota(id));
            return "cotas/detalhes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Cota não encontrada!");
            return "redirect:/cotas";
        }
    }

    @GetMapping("/{id}/inativar")
    public String inativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            cotaService.inativarCota(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Cota inativada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao inativar cota: " + e.getMessage());
        }
        return "redirect:/cotas/" + id + "/detalhes";
    }

    @GetMapping("/{id}/contemplar")
    public String contemplar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            cotaService.contemplarCota(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Cota contemplada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao contemplar cota: " + e.getMessage());
        }
        return "redirect:/cotas/" + id + "/detalhes";
    }

    @GetMapping("/grupo/{grupoId}")
    public String cotasPorGrupo(@PathVariable Long grupoId, Model model) {
        model.addAttribute("cotas", cotaService.buscarPorGrupo(grupoId));
        model.addAttribute("grupo", grupoService.buscarPorId(grupoId));
        return "cotas/lista";
    }

    @GetMapping("/cliente/{clienteId}")
    public String cotasPorCliente(@PathVariable Long clienteId, Model model) {
        model.addAttribute("cotas", cotaService.buscarPorCliente(clienteId));
        model.addAttribute("cliente", clienteService.buscarPorId(clienteId));
        return "cotas/lista";
    }
} 