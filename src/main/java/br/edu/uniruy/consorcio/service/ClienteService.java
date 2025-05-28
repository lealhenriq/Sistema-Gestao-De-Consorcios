package br.edu.uniruy.consorcio.service;

import br.edu.uniruy.consorcio.model.Cliente;
import br.edu.uniruy.consorcio.model.Cota;
import br.edu.uniruy.consorcio.repository.ClienteRepository;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Autowired
    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado com ID: " + id));
    }

    public Cliente buscarPorCpf(String cpf) {
        return clienteRepository.findByCpf(cpf)
            .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado com CPF: " + cpf));
    }

    public List<Cliente> buscarPorNome(String nome) {
        return clienteRepository.findByNomeContainingIgnoreCase(nome);
    }

    @Transactional
    public Cliente salvar(Cliente cliente) {
        if (cliente.getId() == null && clienteRepository.existsByCpf(cliente.getCpf())) {
            throw new IllegalArgumentException("Já existe um cliente cadastrado com o CPF: " + cliente.getCpf());
        }
        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente atualizar(Long id, Cliente cliente) {
        if (!clienteRepository.existsById(id)) {
            throw new EntityNotFoundException("Cliente não encontrado com ID: " + id);
        }
        
        // Verifica se o CPF já existe para outro cliente
        clienteRepository.findByCpf(cliente.getCpf()).ifPresent(c -> {
            if (!c.getId().equals(id)) {
                throw new IllegalArgumentException("Já existe outro cliente cadastrado com o CPF: " + cliente.getCpf());
            }
        });
        
        // Retrieve the existing client to preserve relationships
        Cliente existingCliente = clienteRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado com ID: " + id));
        
        // Update only the basic information, preserving relationships
        cliente.setId(id);
        cliente.setCotas(existingCliente.getCotas()); // Preserve existing cotas
        
        return clienteRepository.save(cliente);
    }

    @Transactional
    public void excluir(Long id) {
        Cliente cliente = buscarPorId(id);
        
        if (!cliente.getCotas().isEmpty()) {
            // Check if any of the client's cotas are referenced in sorteios table
            for (Cota cota : cliente.getCotas()) {
                if (!cota.getSorteios().isEmpty()) {
                    throw new IllegalStateException("Não é possível excluir o cliente pois ele possui cotas contempladas em sorteios");
                }
            }
            
            throw new IllegalStateException("Não é possível excluir o cliente pois ele possui cotas associadas. Remova as cotas primeiro.");
        }
        
        clienteRepository.delete(cliente);
    }
} 