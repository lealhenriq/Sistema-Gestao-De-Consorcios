package br.edu.uniruy.consorcio.service;

import br.edu.uniruy.consorcio.model.GrupoConsorcio;
import br.edu.uniruy.consorcio.repository.GrupoConsorcioRepository;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class GrupoConsorcioService {

    private final GrupoConsorcioRepository grupoConsorcioRepository;

    @Autowired
    public GrupoConsorcioService(GrupoConsorcioRepository grupoConsorcioRepository) {
        this.grupoConsorcioRepository = grupoConsorcioRepository;
    }

    public List<GrupoConsorcio> listarTodos() {
        return grupoConsorcioRepository.findAll();
    }

    public GrupoConsorcio buscarPorId(Long id) {
        return grupoConsorcioRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Grupo não encontrado com ID: " + id));
    }

    public List<GrupoConsorcio> buscarPorNome(String nome) {
        return grupoConsorcioRepository.findByNomeContainingIgnoreCase(nome);
    }

    public List<GrupoConsorcio> buscarPorStatus(GrupoConsorcio.StatusGrupo status) {
        return grupoConsorcioRepository.findByStatus(status);
    }

    @Transactional
    public GrupoConsorcio salvar(GrupoConsorcio grupoConsorcio) {
        if (grupoConsorcio.getDataCadastro() == null) {
            grupoConsorcio.setDataCadastro(LocalDateTime.now());
        }
        grupoConsorcio.setDataAtualizacao(LocalDateTime.now());
        return grupoConsorcioRepository.save(grupoConsorcio);
    }

    @Transactional
    public GrupoConsorcio atualizar(Long id, GrupoConsorcio grupo) {
        if (!grupoConsorcioRepository.existsById(id)) {
            throw new EntityNotFoundException("Grupo de consórcio não encontrado com ID: " + id);
        }
        
        validarGrupo(grupo);
        grupo.setId(id);
        return grupoConsorcioRepository.save(grupo);
    }

    @Transactional
    public void excluir(Long id) {
        GrupoConsorcio grupo = buscarPorId(id);
        
        if (grupo.getStatus() != GrupoConsorcio.StatusGrupo.FORMACAO) {
            throw new IllegalStateException("Não é possível excluir um grupo que não está em formação");
        }
        
        if (!grupo.getCotas().isEmpty()) {
            throw new IllegalStateException("Não é possível excluir o grupo pois ele possui cotas associadas");
        }
        
        grupoConsorcioRepository.delete(grupo);
    }
    
    @Transactional
    public GrupoConsorcio ativarGrupo(Long id) {
        GrupoConsorcio grupo = buscarPorId(id);
        
        if (grupo.getStatus() != GrupoConsorcio.StatusGrupo.FORMACAO) {
            throw new IllegalStateException("O grupo já está ativo ou encerrado");
        }
        
        grupo.setStatus(GrupoConsorcio.StatusGrupo.ATIVO);
        return grupoConsorcioRepository.save(grupo);
    }
    
    @Transactional
    public GrupoConsorcio encerrarGrupo(Long id) {
        GrupoConsorcio grupo = buscarPorId(id);
        
        if (grupo.getStatus() != GrupoConsorcio.StatusGrupo.ATIVO) {
            throw new IllegalStateException("O grupo não está ativo");
        }
        
        grupo.setStatus(GrupoConsorcio.StatusGrupo.ENCERRADO);
        return grupoConsorcioRepository.save(grupo);
    }
    
    private void validarGrupo(GrupoConsorcio grupo) {
        if (grupo.getNumeroCotas() < 2) {
            throw new IllegalArgumentException("O grupo deve ter pelo menos 2 cotas");
        }
        
        if (grupo.getPrazoMeses() <= 0) {
            throw new IllegalArgumentException("O prazo deve ser maior que zero");
        }
        
        if (grupo.getValorBem() == null || grupo.getValorBem().signum() <= 0) {
            throw new IllegalArgumentException("O valor do bem deve ser maior que zero");
        }
    }

    public Page<GrupoConsorcio> listarTodosPaginado(Pageable pageable) {
        return grupoConsorcioRepository.findAll(pageable);
    }

    public List<GrupoConsorcio> listarPorStatus(String status) {
        try {
            GrupoConsorcio.StatusGrupo statusEnum = GrupoConsorcio.StatusGrupo.valueOf(status);
            return grupoConsorcioRepository.findByStatus(statusEnum);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status inválido: " + status);
        }
    }
    
    public long contarGruposPorStatus(String status) {
        try {
            GrupoConsorcio.StatusGrupo statusEnum = GrupoConsorcio.StatusGrupo.valueOf(status);
            return grupoConsorcioRepository.findByStatus(statusEnum).size();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status inválido: " + status);
        }
    }
    
    public List<GrupoConsorcio> listarAtivos() {
        return grupoConsorcioRepository.findByStatus(GrupoConsorcio.StatusGrupo.ATIVO);
    }
    
    public List<GrupoConsorcio> listarEmFormacao() {
        return grupoConsorcioRepository.findByStatus(GrupoConsorcio.StatusGrupo.FORMACAO);
    }
    
    public List<GrupoConsorcio> listarEncerrados() {
        return grupoConsorcioRepository.findByStatus(GrupoConsorcio.StatusGrupo.ENCERRADO);
    }
} 