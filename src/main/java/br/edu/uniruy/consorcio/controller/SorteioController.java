package br.edu.uniruy.consorcio.controller;

import br.edu.uniruy.consorcio.model.Cota;
import br.edu.uniruy.consorcio.model.Sorteio;
import br.edu.uniruy.consorcio.service.CotaService;
import br.edu.uniruy.consorcio.service.GrupoConsorcioService;
import br.edu.uniruy.consorcio.service.SorteioService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/sorteios")
public class SorteioController {

    private static final Logger logger = LoggerFactory.getLogger(SorteioController.class);
    
    private final SorteioService sorteioService;
    private final GrupoConsorcioService grupoService;
    private final CotaService cotaService;

    @Autowired
    public SorteioController(SorteioService sorteioService,
                             GrupoConsorcioService grupoService,
                             CotaService cotaService) {
        this.sorteioService = sorteioService;
        this.grupoService = grupoService;
        this.cotaService = cotaService;
    }

    @GetMapping
    public String listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Sorteio> sorteiosPage = sorteioService.listarTodosPaginado(pageable);
        model.addAttribute("sorteios", sorteiosPage);
        return "sorteios/lista";
    }

    @GetMapping("/cadastro")
    public String formCadastro(Model model, @RequestParam(required = false) Long grupoId) {
        Sorteio sorteio = new Sorteio();
        
        if (grupoId != null) {
            sorteio.setGrupo(grupoService.buscarPorId(grupoId));
        }
        
        model.addAttribute("sorteio", sorteio);
        model.addAttribute("grupos", grupoService.listarTodos());
        return "sorteios/form";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@Valid @ModelAttribute("sorteio") Sorteio sorteio,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        logger.info("Recebido cadastro de sorteio: {}", sorteio);
        
        if (result.hasErrors()) {
            logger.error("Erros de validação: {}", result.getAllErrors());
            model.addAttribute("grupos", grupoService.listarTodos());
            return "sorteios/form";
        }
        
        try {
            // Ensure grupo object is properly set
            if (sorteio.getGrupo() != null && sorteio.getGrupo().getId() != null) {
                sorteio.setGrupo(grupoService.buscarPorId(sorteio.getGrupo().getId()));
                logger.info("Grupo encontrado: {}", sorteio.getGrupo());
            } else {
                logger.warn("Grupo não encontrado ou não informado");
            }
            
            // Set default date if none is provided
            if (sorteio.getDataSorteio() == null) {
                sorteio.setDataSorteio(LocalDate.now());
                logger.info("Data do sorteio foi definida para hoje: {}", sorteio.getDataSorteio());
            }
            
            Sorteio sorteioSalvo = sorteioService.salvar(sorteio);
            logger.info("Sorteio salvo com sucesso: {}", sorteioSalvo);
            
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Sorteio cadastrado com sucesso!");
            
            // Redireciona para detalhes do grupo se o sorteio foi criado a partir dele
            if (sorteio.getGrupo() != null) {
                return "redirect:/grupos/" + sorteio.getGrupo().getId() + "/detalhes";
            }
            
            return "redirect:/sorteios";
        } catch (Exception e) {
            logger.error("Erro ao cadastrar sorteio", e);
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao cadastrar sorteio: " + e.getMessage());
            return "redirect:/sorteios/cadastro";
        }
    }

    @GetMapping("/{id}/editar")
    public String formEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Sorteio sorteio = sorteioService.buscarPorId(id);
            model.addAttribute("sorteio", sorteio);
            model.addAttribute("grupos", grupoService.listarTodos());
            
            if (sorteio.getGrupo() != null) {
                model.addAttribute("cotas", cotaService.buscarCotasAtivasPorGrupo(sorteio.getGrupo().getId()));
            }
            
            return "sorteios/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Sorteio não encontrado!");
            return "redirect:/sorteios";
        }
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Long id,
                         @Valid @ModelAttribute("sorteio") Sorteio sorteio,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            logger.error("Erros de validação ao editar: {}", result.getAllErrors());
            model.addAttribute("grupos", grupoService.listarTodos());
            if (sorteio.getGrupo() != null) {
                model.addAttribute("cotas", cotaService.buscarCotasAtivasPorGrupo(sorteio.getGrupo().getId()));
            }
            return "sorteios/form";
        }
        
        try {
            sorteio.setId(id);
            
            // Ensure grupo object is properly set
            if (sorteio.getGrupo() != null && sorteio.getGrupo().getId() != null) {
                sorteio.setGrupo(grupoService.buscarPorId(sorteio.getGrupo().getId()));
            }
            
            // Ensure cotaContemplada is properly set if provided
            if (sorteio.getCotaContemplada() != null && sorteio.getCotaContemplada().getId() != null) {
                sorteio.setCotaContemplada(cotaService.buscarPorId(sorteio.getCotaContemplada().getId()));
            }
            
            // Set default date if none is provided
            if (sorteio.getDataSorteio() == null) {
                sorteio.setDataSorteio(LocalDate.now());
            }
            
            sorteioService.atualizar(id, sorteio);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Sorteio atualizado com sucesso!");
            return "redirect:/sorteios";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao atualizar sorteio: " + e.getMessage());
            return "redirect:/sorteios/" + id + "/editar";
        }
    }

    @GetMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Sorteio sorteio = sorteioService.buscarPorId(id);
            Long grupoId = sorteio.getGrupo().getId();
            
            sorteioService.excluir(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Sorteio excluído com sucesso!");
            
            // Retorna para a página de detalhes do grupo
            return "redirect:/grupos/" + grupoId + "/detalhes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao excluir sorteio: " + e.getMessage());
            return "redirect:/sorteios";
        }
    }

    @GetMapping("/{id}/detalhes")
    public String detalhes(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Sorteio sorteio = sorteioService.buscarPorId(id);
            model.addAttribute("sorteio", sorteio);
            
            if (sorteio.getGrupo() != null) {
                model.addAttribute("cotasElegiveis", 
                    cotaService.buscarCotasAtivasPorGrupo(sorteio.getGrupo().getId()));
            }
            
            return "sorteios/detalhes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Sorteio não encontrado!");
            return "redirect:/sorteios";
        }
    }

    @GetMapping("/realizar")
    public String formRealizarSorteio(Model model) {
        model.addAttribute("grupos", grupoService.buscarPorStatus(br.edu.uniruy.consorcio.model.GrupoConsorcio.StatusGrupo.ATIVO));
        model.addAttribute("mesAtual", 
                LocalDate.now().format(DateTimeFormatter.ofPattern("MM/yyyy")));
        return "sorteios/realizar";
    }

    @PostMapping("/realizar")
    public String realizarSorteio(@RequestParam Long grupoId,
                                 @RequestParam String mesAno,
                                 RedirectAttributes redirectAttributes) {
        try {
            Sorteio sorteio = sorteioService.realizarSorteio(grupoId, mesAno);
            
            redirectAttributes.addFlashAttribute("mensagemSucesso", 
                "Sorteio realizado com sucesso! A cota contemplada foi: " + 
                sorteio.getCotaContemplada().getNumero() + " - Cliente: " + 
                sorteio.getCotaContemplada().getCliente().getNome());
            
            return "redirect:/sorteios/" + sorteio.getId() + "/detalhes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao realizar sorteio: " + e.getMessage());
            return "redirect:/sorteios/realizar";
        }
    }

    @GetMapping("/grupo/{grupoId}")
    public String sorteiosPorGrupo(@PathVariable Long grupoId, 
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Sorteio> sorteiosPage = sorteioService.buscarPorGrupoPaginado(grupoId, pageable);
        model.addAttribute("sorteios", sorteiosPage);
        model.addAttribute("grupo", grupoService.buscarPorId(grupoId));
        return "sorteios/lista";
    }

    @GetMapping("/periodo")
    public String formFiltrarPorPeriodo() {
        return "sorteios/filtro-periodo";
    }

    @GetMapping("/filtrar-periodo")
    public String sorteiosPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Sorteio> sorteiosPage = sorteioService.buscarPorPeriodoPaginado(dataInicio, dataFim, pageable);
        model.addAttribute("sorteios", sorteiosPage);
        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);
        return "sorteios/lista-periodo";
    }

    @GetMapping("/{id}/sincronizar-cota")
    public String sincronizarCotaContemplada(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Sorteio sorteio = sorteioService.buscarPorId(id);
            if (sorteio.getRealizado() && sorteio.getCotaContemplada() != null) {
                // Atualiza manualmente o status da cota
                Cota cota = sorteio.getCotaContemplada();
                cota.setContemplada(true);
                cota.setStatus(Cota.StatusCota.CONTEMPLADA);
                cotaService.atualizar(cota.getId(), cota);
                
                redirectAttributes.addFlashAttribute("mensagemSucesso", 
                    "Status da cota " + cota.getNumero() + " sincronizado com sucesso!");
            } else {
                redirectAttributes.addFlashAttribute("mensagemErro", 
                    "Este sorteio não possui cota contemplada para sincronizar");
            }
            return "redirect:/sorteios/" + id + "/detalhes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao sincronizar cota: " + e.getMessage());
            return "redirect:/sorteios";
        }
    }

    @GetMapping("/{id}/realizar")
    public String realizarSorteioExistente(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Sorteio sorteio = sorteioService.buscarPorId(id);
            
            if (sorteio.getRealizado()) {
                redirectAttributes.addFlashAttribute("mensagemErro", "Este sorteio já foi realizado!");
                return "redirect:/sorteios/" + id + "/detalhes";
            }
            
            Sorteio sorteioRealizado = sorteioService.realizarSorteioExistente(id);
            
            redirectAttributes.addFlashAttribute("mensagemSucesso", 
                "Sorteio realizado com sucesso! A cota contemplada foi: " + 
                sorteioRealizado.getCotaContemplada().getNumero() + " - Cliente: " + 
                sorteioRealizado.getCotaContemplada().getCliente().getNome());
            
            return "redirect:/sorteios/" + id + "/detalhes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao realizar sorteio: " + e.getMessage());
            return "redirect:/sorteios";
        }
    }

    @GetMapping("/{id}/cancelar")
    public String cancelarSorteio(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Sorteio sorteio = sorteioService.buscarPorId(id);
            
            if (sorteio.getRealizado()) {
                redirectAttributes.addFlashAttribute("mensagemErro", "Não é possível cancelar um sorteio já realizado!");
                return "redirect:/sorteios/" + id + "/detalhes";
            }
            
            sorteioService.excluir(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Sorteio cancelado com sucesso!");
            return "redirect:/sorteios";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao cancelar sorteio: " + e.getMessage());
            return "redirect:/sorteios/" + id + "/detalhes";
        }
    }
} 