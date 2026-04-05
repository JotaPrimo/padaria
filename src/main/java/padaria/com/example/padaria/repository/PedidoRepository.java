package padaria.com.example.padaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import padaria.com.example.padaria.entity.Pedido;
import padaria.com.example.padaria.enums.StatusPedido;

import java.time.LocalDateTime;

public interface PedidoRepository extends JpaRepository<Pedido, Long>, JpaSpecificationExecutor<Pedido> {

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.dataHoraEntrega >= :inicio AND p.dataHoraEntrega < :fim")
    long countByDataHoraEntregaBetween(LocalDateTime inicio, LocalDateTime fim);

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.pagamentoIntegral = false AND p.statusPedido <> :statusCancelado")
    long countPagamentoPendente(StatusPedido statusCancelado);
}
