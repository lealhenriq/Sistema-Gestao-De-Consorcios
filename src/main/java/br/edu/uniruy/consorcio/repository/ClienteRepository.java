package br.edu.uniruy.consorcio.repository;

import br.edu.uniruy.consorcio.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    
    Optional<Cliente> findByCpf(String cpf);
    
    List<Cliente> findByNomeContainingIgnoreCase(String nome);
    
    boolean existsByCpf(String cpf);
} 