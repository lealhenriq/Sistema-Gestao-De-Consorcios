package br.edu.uniruy.consorcio.controller;

import br.edu.uniruy.consorcio.model.Usuario;
import br.edu.uniruy.consorcio.service.UsuarioService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Autowired
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Usuario> usuariosPage = usuarioService.listarTodosPaginado(pageable);
        model.addAttribute("usuarios", usuariosPage);
        return "usuarios/lista";
    }

    @GetMapping("/cadastro")
    public String formCadastro(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", Usuario.Role.values());
        return "usuarios/form";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@Valid @ModelAttribute("usuario") Usuario usuario,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Usuario.Role.values());
            return "usuarios/form";
        }
        
        try {
            usuarioService.salvar(usuario);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuário cadastrado com sucesso!");
            return "redirect:/usuarios";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao cadastrar usuário: " + e.getMessage());
            return "redirect:/usuarios/cadastro";
        }
    }

    @GetMapping("/{id}/editar")
    public String formEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("usuario", usuarioService.buscarPorId(id));
            model.addAttribute("roles", Usuario.Role.values());
            return "usuarios/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Usuário não encontrado!");
            return "redirect:/usuarios";
        }
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Long id,
                        @Valid @ModelAttribute("usuario") Usuario usuario,
                        BindingResult result,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Usuario.Role.values());
            return "usuarios/form";
        }
        
        try {
            usuario.setId(id);
            usuarioService.atualizar(id, usuario);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuário atualizado com sucesso!");
            return "redirect:/usuarios";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao atualizar usuário: " + e.getMessage());
            return "redirect:/usuarios/" + id + "/editar";
        }
    }

    @GetMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.excluir(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuário excluído com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao excluir usuário: " + e.getMessage());
        }
        return "redirect:/usuarios";
    }

    @GetMapping("/{id}/ativar")
    public String ativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.ativarOuDesativar(id, true);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuário ativado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao ativar usuário: " + e.getMessage());
        }
        return "redirect:/usuarios";
    }

    @GetMapping("/{id}/desativar")
    public String desativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.ativarOuDesativar(id, false);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuário desativado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao desativar usuário: " + e.getMessage());
        }
        return "redirect:/usuarios";
    }

    @PostMapping("/desativar")
    public String desativarPost(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.ativarOuDesativar(id, false);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuário desativado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao desativar usuário: " + e.getMessage());
        }
        return "redirect:/usuarios";
    }

    @PostMapping("/ativar")
    public String ativarPost(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.ativarOuDesativar(id, true);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuário ativado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao ativar usuário: " + e.getMessage());
        }
        return "redirect:/usuarios";
    }
}

@Controller
@RequestMapping("/perfil")
class PerfilController {

    private final UsuarioService usuarioService;

    @Autowired
    public PerfilController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String perfil(Model model) {
        // Recupera o usuário logado
        // No mundo real, você usaria: SecurityContextHolder.getContext().getAuthentication().getName()
        // e então buscaria o usuário pelo email, mas para simplificar, 
        // vamos apenas pegar o primeiro usuário
        Usuario usuario = usuarioService.listarTodos().get(0);
        model.addAttribute("usuario", usuario);
        return "usuarios/perfil";
    }

    @GetMapping("/alterar-senha")
    public String formAlterarSenha() {
        return "usuarios/alterar-senha";
    }

    @PostMapping("/alterar-senha")
    public String alterarSenha(@RequestParam String senhaAtual,
                              @RequestParam String novaSenha,
                              @RequestParam String confirmaSenha,
                              RedirectAttributes redirectAttributes) {
        // Recupera o usuário logado (simplificado)
        Usuario usuario = usuarioService.listarTodos().get(0);
        
        if (!novaSenha.equals(confirmaSenha)) {
            redirectAttributes.addFlashAttribute("mensagemErro", "A nova senha e a confirmação não coincidem!");
            return "redirect:/perfil/alterar-senha";
        }
        
        try {
            usuarioService.atualizarSenha(usuario.getId(), senhaAtual, novaSenha);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Senha alterada com sucesso!");
            return "redirect:/perfil";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao alterar senha: " + e.getMessage());
            return "redirect:/perfil/alterar-senha";
        }
    }
} 