package br.edu.uniruy.consorcio.controller;

import br.edu.uniruy.consorcio.model.GrupoConsorcio;
import br.edu.uniruy.consorcio.model.Cota;
import br.edu.uniruy.consorcio.service.CotaService;
import br.edu.uniruy.consorcio.service.GrupoConsorcioService;
import br.edu.uniruy.consorcio.service.SorteioService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/grupos")
public class GrupoConsorcioController {

    private final GrupoConsorcioService grupoService;
    private final CotaService cotaService;
    private final SorteioService sorteioService;

    @Autowired
    public GrupoConsorcioController(GrupoConsorcioService grupoService, 
                                    CotaService cotaService,
                                    SorteioService sorteioService) {
        this.grupoService = grupoService;
        this.cotaService = cotaService;
        this.sorteioService = sorteioService;
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) GrupoConsorcio.StatusGrupo status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        // For now, just use the list until we implement proper pagination in the service
        model.addAttribute("grupos", grupoService.listarTodos());
        return "grupos/lista";
    }

    @GetMapping("/cadastro")
    public String formCadastro(Model model) {
        model.addAttribute("grupo", new GrupoConsorcio());
        return "grupos/cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@Valid @ModelAttribute("grupo") GrupoConsorcio grupo, 
                            BindingResult result, 
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "grupos/cadastro";
        }
        
        try {
            grupoService.salvar(grupo);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Grupo de consórcio cadastrado com sucesso!");
            return "redirect:/grupos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao cadastrar grupo: " + e.getMessage());
            return "redirect:/grupos/cadastro";
        }
    }

    @GetMapping("/{id}/editar")
    public String formEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("grupo", grupoService.buscarPorId(id));
            return "grupos/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Grupo não encontrado!");
            return "redirect:/grupos";
        }
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Long id, 
                         @Valid @ModelAttribute("grupo") GrupoConsorcio grupo, 
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "grupos/form";
        }
        
        try {
            // Busca o grupo existente para preservar relacionamentos e campos não editáveis
            GrupoConsorcio grupoExistente = grupoService.buscarPorId(id);
            
            // Verificar se o número de cotas está sendo reduzido e existem cotas
            if (grupo.getNumeroCotas() < grupoExistente.getNumeroCotas() && !grupoExistente.getCotas().isEmpty()) {
                // Verificar o número atual de cotas no sistema
                int cotasAtuais = grupoExistente.getCotas().size();
                // Não permitir reduzir para um número menor que o de cotas existentes
                if (grupo.getNumeroCotas() < cotasAtuais) {
                    redirectAttributes.addFlashAttribute("mensagemErro", 
                        "Não é possível reduzir o número de cotas para um valor menor que o número atual de cotas (" + cotasAtuais + ")");
                    return "redirect:/grupos/" + id + "/editar";
                }
            }
            
            // Atualiza apenas os campos editáveis
            grupoExistente.setNome(grupo.getNome());
            grupoExistente.setValorBem(grupo.getValorBem());
            grupoExistente.setPrazoMeses(grupo.getPrazoMeses());
            grupoExistente.setNumeroCotas(grupo.getNumeroCotas());
            
            // Garantir que dataInicio nunca seja nula
            if (grupo.getDataInicio() != null) {
                grupoExistente.setDataInicio(grupo.getDataInicio());
            } else if (grupoExistente.getDataInicio() == null) {
                grupoExistente.setDataInicio(java.time.LocalDate.now());
            }
            
            // Recalcula a data de término com base no novo prazo
            if (grupoExistente.getDataInicio() != null && grupoExistente.getPrazoMeses() != null) {
                grupoExistente.setDataTermino(grupoExistente.getDataInicio().plusMonths(grupoExistente.getPrazoMeses()));
            }
            
            grupoService.atualizar(id, grupoExistente);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Grupo atualizado com sucesso!");
            return "redirect:/grupos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao atualizar grupo: " + e.getMessage());
            return "redirect:/grupos/" + id + "/editar";
        }
    }

    @GetMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            grupoService.excluir(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Grupo excluído com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao excluir grupo: " + e.getMessage());
        }
        return "redirect:/grupos";
    }

    @GetMapping("/{id}/detalhes")
    public String detalhes(
            @PathVariable Long id, 
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model, 
            RedirectAttributes redirectAttributes) {
        try {
            GrupoConsorcio grupo = grupoService.buscarPorId(id);
            model.addAttribute("grupo", grupo);
            
            // Use pagination for cotas
            Pageable pageable = PageRequest.of(page, size);
            Page<Cota> cotasPage = cotaService.buscarPorGrupoPaginado(id, pageable);
            model.addAttribute("cotas", cotasPage);
            
            // Calculate statistics
            long cotasAtivas = cotasPage.getContent().stream()
                .filter(c -> c.getStatus() == br.edu.uniruy.consorcio.model.Cota.StatusCota.ATIVA)
                .count();
            long cotasContempladas = cotasPage.getContent().stream()
                .filter(c -> c.getStatus() == br.edu.uniruy.consorcio.model.Cota.StatusCota.CONTEMPLADA)
                .count();
            long cotasInativas = cotasPage.getContent().stream()
                .filter(c -> c.getStatus() == br.edu.uniruy.consorcio.model.Cota.StatusCota.INATIVA)
                .count();
            
            model.addAttribute("cotasAtivas", cotasAtivas);
            model.addAttribute("cotasContempladas", cotasContempladas);
            model.addAttribute("cotasInativas", cotasInativas);
            
            // Add sorteios
            model.addAttribute("sorteios", sorteioService.buscarPorGrupo(id));
            
            return "grupos/detalhes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Grupo não encontrado!");
            return "redirect:/grupos";
        }
    }

    @GetMapping("/{id}/ativar")
    public String ativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            grupoService.ativarGrupo(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Grupo ativado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao ativar grupo: " + e.getMessage());
        }
        return "redirect:/grupos/" + id + "/detalhes";
    }

    @GetMapping("/{id}/encerrar")
    public String encerrar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            grupoService.encerrarGrupo(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Grupo encerrado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao encerrar grupo: " + e.getMessage());
        }
        return "redirect:/grupos/" + id + "/detalhes";
    }

    @GetMapping("/buscar")
    public String buscar(@RequestParam String termo, Model model) {
        model.addAttribute("grupos", grupoService.buscarPorNome(termo));
        model.addAttribute("termoBusca", termo);
        return "grupos/lista";
    }

    @GetMapping("/status/{status}")
    public String filtrarPorStatus(@PathVariable GrupoConsorcio.StatusGrupo status, Model model) {
        model.addAttribute("grupos", grupoService.buscarPorStatus(status));
        model.addAttribute("statusAtual", status);
        return "grupos/lista";
    }
} 