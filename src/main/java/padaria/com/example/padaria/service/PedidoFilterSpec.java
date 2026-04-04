package padaria.com.example.padaria.service;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import padaria.com.example.padaria.entity.Pedido;
import padaria.com.example.padaria.enums.StatusPedido;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PedidoFilterSpec {

    private PedidoFilterSpec() {}

    public static Specification<Pedido> comFiltros(
            LocalDateTime dataEntregaInicio,
            LocalDateTime dataEntregaFim,
            String cliente,
            StatusPedido statusPedido,
            Boolean pagamentoPendente,
            Long cadastradoPorId
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (dataEntregaInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dataHoraEntrega"), dataEntregaInicio));
            }

            if (dataEntregaFim != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dataHoraEntrega"), dataEntregaFim));
            }

            if (cliente != null && !cliente.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("cliente")), "%" + cliente.toLowerCase() + "%"));
            }

            if (statusPedido != null) {
                predicates.add(cb.equal(root.get("statusPedido"), statusPedido));
            }

            if (Boolean.TRUE.equals(pagamentoPendente)) {
                predicates.add(cb.equal(root.get("pagamentoIntegral"), false));
                predicates.add(cb.notEqual(root.get("statusPedido"), StatusPedido.CANCELADO));
            }

            if (cadastradoPorId != null) {
                predicates.add(cb.equal(root.get("cadastradoPor").get("id"), cadastradoPorId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
