package br.edu.uniruy.consorcio.api;

import br.edu.uniruy.consorcio.model.Cliente;
import br.edu.uniruy.consorcio.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "API para gerenciamento de clientes")
public class ClienteApiController {

    private final ClienteService clienteService;

    @Autowired
    public ClienteApiController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    @Operation(summary = "Listar todos os clientes", description = "Retorna uma lista com todos os clientes cadastrados")
    public ResponseEntity<List<Cliente>> listarTodos() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID", description = "Retorna um cliente pelo ID")
    public ResponseEntity<Cliente> buscarPorId(
            @Parameter(description = "ID do cliente", required = true) @PathVariable Long id) {
        try {
            return ResponseEntity.ok(clienteService.buscarPorId(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/cpf/{cpf}")
    @Operation(summary = "Buscar cliente por CPF", description = "Retorna um cliente pelo CPF")
    public ResponseEntity<Cliente> buscarPorCpf(
            @Parameter(description = "CPF do cliente", required = true) @PathVariable String cpf) {
        try {
            return ResponseEntity.ok(clienteService.buscarPorCpf(cpf));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar clientes por nome", description = "Retorna uma lista de clientes com nome contendo o termo pesquisado")
    public ResponseEntity<List<Cliente>> buscarPorNome(
            @Parameter(description = "Termo para busca no nome", required = true) @RequestParam String nome) {
        return ResponseEntity.ok(clienteService.buscarPorNome(nome));
    }

    @PostMapping
    @Operation(summary = "Cadastrar cliente", description = "Cadastra um novo cliente")
    public ResponseEntity<Cliente> cadastrar(
            @Parameter(description = "Dados do cliente", required = true) @Valid @RequestBody Cliente cliente) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.salvar(cliente));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cliente", description = "Atualiza os dados de um cliente existente")
    public ResponseEntity<Cliente> atualizar(
            @Parameter(description = "ID do cliente", required = true) @PathVariable Long id,
            @Parameter(description = "Dados do cliente", required = true) @Valid @RequestBody Cliente cliente) {
        try {
            return ResponseEntity.ok(clienteService.atualizar(id, cliente));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir cliente", description = "Exclui um cliente pelo ID")
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do cliente", required = true) @PathVariable Long id) {
        try {
            clienteService.excluir(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 