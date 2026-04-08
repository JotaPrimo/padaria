package padaria.com.example.padaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import padaria.com.example.padaria.entity.Pagamento;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
}
