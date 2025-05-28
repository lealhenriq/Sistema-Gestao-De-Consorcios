package br.edu.uniruy.consorcio.config;

import br.edu.uniruy.consorcio.model.*;
import br.edu.uniruy.consorcio.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Configuration
public class DataInitializer {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData(
            UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository,
            GrupoConsorcioRepository grupoRepository,
            CotaRepository cotaRepository,
            PagamentoRepository pagamentoRepository,
            SorteioRepository sorteioRepository) {
        
        return args -> {
            // Criar usuários
            if (usuarioRepository.count() == 0) {
                Usuario admin = new Usuario();
                admin.setNome("Administrador");
                admin.setEmail("admin@sistema.com");
                admin.setSenha(passwordEncoder.encode("admin"));
                admin.setRole(Usuario.Role.ADMIN);
                admin.setAtivo(true);
                
                Usuario operador = new Usuario();
                operador.setNome("Operador");
                operador.setEmail("operador@sistema.com");
                operador.setSenha(passwordEncoder.encode("operador"));
                operador.setRole(Usuario.Role.OPERADOR);
                operador.setAtivo(true);
                
                usuarioRepository.saveAll(List.of(admin, operador));
            }
            
            // Criar clientes
            if (clienteRepository.count() == 0) {
                Cliente cliente1 = new Cliente();
                cliente1.setNome("João Silva");
                cliente1.setCpf("123.456.789-10");
                cliente1.setTelefone("(71) 98765-4321");
                cliente1.setEndereco("Rua A, 123 - Salvador");
                cliente1.setEmail("joao@email.com");
                
                Cliente cliente2 = new Cliente();
                cliente2.setNome("Maria Santos");
                cliente2.setCpf("987.654.321-00");
                cliente2.setTelefone("(71) 91234-5678");
                cliente2.setEndereco("Avenida B, 456 - Salvador");
                cliente2.setEmail("maria@email.com");
                
                Cliente cliente3 = new Cliente();
                cliente3.setNome("Carlos Oliveira");
                cliente3.setCpf("456.789.123-45");
                cliente3.setTelefone("(71) 92345-6789");
                cliente3.setEndereco("Praça C, 789 - Salvador");
                cliente3.setEmail("carlos@email.com");
                
                clienteRepository.saveAll(List.of(cliente1, cliente2, cliente3));
            }
            
            // Criar grupos de consórcio
            if (grupoRepository.count() == 0) {
                GrupoConsorcio grupo1 = new GrupoConsorcio();
                grupo1.setNome("Consórcio Automóveis 2024");
                grupo1.setValorBem(new BigDecimal("120000.00"));
                grupo1.setNumeroCotas(60);
                grupo1.setPrazoMeses(60);
                grupo1.setDataInicio(LocalDate.now().minusMonths(2));
                grupo1.setStatus(GrupoConsorcio.StatusGrupo.ATIVO);
                
                GrupoConsorcio grupo2 = new GrupoConsorcio();
                grupo2.setNome("Consórcio Motocicletas 2024");
                grupo2.setValorBem(new BigDecimal("30000.00"));
                grupo2.setNumeroCotas(36);
                grupo2.setPrazoMeses(36);
                grupo2.setDataInicio(LocalDate.now().minusMonths(1));
                grupo2.setStatus(GrupoConsorcio.StatusGrupo.ATIVO);
                
                GrupoConsorcio grupo3 = new GrupoConsorcio();
                grupo3.setNome("Consórcio Premium 2024");
                grupo3.setValorBem(new BigDecimal("250000.00"));
                grupo3.setNumeroCotas(80);
                grupo3.setPrazoMeses(80);
                grupo3.setStatus(GrupoConsorcio.StatusGrupo.FORMACAO);
                
                grupoRepository.saveAll(List.of(grupo1, grupo2, grupo3));
                
                // Associar clientes a cotas
                List<Cliente> clientes = clienteRepository.findAll();
                List<GrupoConsorcio> grupos = grupoRepository.findAll();
                
                // Grupo 1 - Cliente 1 - Cota 1
                Cota cota1 = new Cota();
                cota1.setCliente(clientes.get(0));
                cota1.setGrupo(grupos.get(0));
                cota1.setNumero(1);
                cota1.setStatus(Cota.StatusCota.ATIVA);
                cota1.setValorParcela(grupos.get(0).getValorBem().divide(
                    BigDecimal.valueOf(grupos.get(0).getNumeroCotas()),
                    2, java.math.RoundingMode.HALF_UP));
                
                // Grupo 1 - Cliente 2 - Cota 2
                Cota cota2 = new Cota();
                cota2.setCliente(clientes.get(1));
                cota2.setGrupo(grupos.get(0));
                cota2.setNumero(2);
                cota2.setStatus(Cota.StatusCota.ATIVA);
                cota2.setValorParcela(grupos.get(0).getValorBem().divide(
                    BigDecimal.valueOf(grupos.get(0).getNumeroCotas()),
                    2, java.math.RoundingMode.HALF_UP));
                
                // Grupo 2 - Cliente 3 - Cota 1
                Cota cota3 = new Cota();
                cota3.setCliente(clientes.get(2));
                cota3.setGrupo(grupos.get(1));
                cota3.setNumero(1);
                cota3.setStatus(Cota.StatusCota.ATIVA);
                cota3.setValorParcela(grupos.get(1).getValorBem().divide(
                    BigDecimal.valueOf(grupos.get(1).getNumeroCotas()),
                    2, java.math.RoundingMode.HALF_UP));
                
                cotaRepository.saveAll(List.of(cota1, cota2, cota3));
                
                // Criar pagamentos
                DateTimeFormatter mesRefFormatter = DateTimeFormatter.ofPattern("MM/yyyy");
                String mesAnterior = LocalDate.now().minusMonths(1).format(mesRefFormatter);
                String mesAtual = LocalDate.now().format(mesRefFormatter);
                
                // Pagamento do mês anterior - Cota 1 - PAGO
                Pagamento pag1 = new Pagamento();
                pag1.setCota(cota1);
                pag1.setMesReferencia(mesAnterior);
                pag1.setValor(cota1.getValorParcela());
                pag1.setStatus(Pagamento.StatusPagamento.PAGO);
                pag1.setDataVencimento(LocalDate.now().minusMonths(1).withDayOfMonth(10));
                pag1.setDataPagamento(LocalDate.now().minusMonths(1).withDayOfMonth(9));
                
                // Pagamento do mês atual - Cota 1 - PENDENTE
                Pagamento pag2 = new Pagamento();
                pag2.setCota(cota1);
                pag2.setMesReferencia(mesAtual);
                pag2.setValor(cota1.getValorParcela());
                pag2.setStatus(Pagamento.StatusPagamento.PENDENTE);
                pag2.setDataVencimento(LocalDate.now().withDayOfMonth(10));
                
                // Pagamento do mês anterior - Cota 2 - PAGO
                Pagamento pag3 = new Pagamento();
                pag3.setCota(cota2);
                pag3.setMesReferencia(mesAnterior);
                pag3.setValor(cota2.getValorParcela());
                pag3.setStatus(Pagamento.StatusPagamento.PAGO);
                pag3.setDataVencimento(LocalDate.now().minusMonths(1).withDayOfMonth(10));
                pag3.setDataPagamento(LocalDate.now().minusMonths(1).withDayOfMonth(5));
                
                // Pagamento do mês atual - Cota 2 - PENDENTE
                Pagamento pag4 = new Pagamento();
                pag4.setCota(cota2);
                pag4.setMesReferencia(mesAtual);
                pag4.setValor(cota2.getValorParcela());
                pag4.setStatus(Pagamento.StatusPagamento.PENDENTE);
                pag4.setDataVencimento(LocalDate.now().withDayOfMonth(10));
                
                // Pagamento do mês atual - Cota 3 - PAGO
                Pagamento pag5 = new Pagamento();
                pag5.setCota(cota3);
                pag5.setMesReferencia(mesAtual);
                pag5.setValor(cota3.getValorParcela());
                pag5.setStatus(Pagamento.StatusPagamento.PAGO);
                pag5.setDataVencimento(LocalDate.now().withDayOfMonth(15));
                pag5.setDataPagamento(LocalDate.now().withDayOfMonth(12));
                
                pagamentoRepository.saveAll(List.of(pag1, pag2, pag3, pag4, pag5));
                
                // Criar sorteio do mês anterior
                Sorteio sorteio = new Sorteio();
                sorteio.setGrupo(grupos.get(0));
                sorteio.setMesAno(mesAnterior);
                sorteio.setDataSorteio(LocalDate.now().minusMonths(1).withDayOfMonth(20));
                sorteio.setCotaContemplada(cota1);
                sorteio.setRealizado(true);
                
                sorteioRepository.save(sorteio);
            }
        };
    }
} 