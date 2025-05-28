package br.edu.uniruy.consorcio.controller;

import br.edu.uniruy.consorcio.model.Cliente;
import br.edu.uniruy.consorcio.service.ClienteService;
import br.edu.uniruy.consorcio.service.CotaService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final CotaService cotaService;

    @Autowired
    public ClienteController(ClienteService clienteService, CotaService cotaService) {
        this.clienteService = clienteService;
        this.cotaService = cotaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clientes", clienteService.listarTodos());
        return "clientes/lista";
    }

    @GetMapping("/cadastro")
    public String formCadastro(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "clientes/form";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@Valid @ModelAttribute("cliente") Cliente cliente, 
                            BindingResult result, 
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "clientes/form";
        }
        
        try {
            clienteService.salvar(cliente);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Cliente cadastrado com sucesso!");
            return "redirect:/clientes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao cadastrar cliente: " + e.getMessage());
            return "redirect:/clientes/cadastro";
        }
    }

    @GetMapping("/{id}/editar")
    public String formEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("cliente", clienteService.buscarPorId(id));
            return "clientes/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Cliente não encontrado!");
            return "redirect:/clientes";
        }
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Long id, 
                         @Valid @ModelAttribute("cliente") Cliente cliente, 
                         BindingResult result, 
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "clientes/form";
        }
        
        try {
            cliente.setId(id);
            clienteService.atualizar(id, cliente);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Cliente atualizado com sucesso!");
            return "redirect:/clientes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao atualizar cliente: " + e.getMessage());
            return "redirect:/clientes/" + id + "/editar";
        }
    }

    @GetMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            clienteService.excluir(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Cliente excluído com sucesso!");
        } catch (IllegalStateException e) {
            if (e.getMessage().contains("cotas associadas") || e.getMessage().contains("cotas contempladas")) {
                redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
                return "redirect:/clientes/" + id + "/detalhes?removerCotas=true";
            }
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao excluir cliente: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao excluir cliente: " + e.getMessage());
        }
        return "redirect:/clientes";
    }

    @GetMapping("/{id}/detalhes")
    public String detalhes(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Cliente cliente = clienteService.buscarPorId(id);
            model.addAttribute("cliente", cliente);
            model.addAttribute("cotas", cotaService.buscarPorCliente(id));
            return "clientes/detalhes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Cliente não encontrado!");
            return "redirect:/clientes";
        }
    }

    @GetMapping("/buscar")
    public String buscar(@RequestParam String termo, Model model) {
        model.addAttribute("clientes", clienteService.buscarPorNome(termo));
        model.addAttribute("termoBusca", termo);
        return "clientes/lista";
    }
} 