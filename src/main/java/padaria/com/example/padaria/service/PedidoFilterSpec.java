package padaria.com.example.padaria.service;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import padaria.com.example.padaria.dto.pedido.PedidoFiltroDTO;
import padaria.com.example.padaria.entity.Pedido;
import padaria.com.example.padaria.enums.StatusPedido;
import padaria.com.example.padaria.utils.StringValidator;

import java.util.ArrayList;
import java.util.List;

public class PedidoFilterSpec {

    private PedidoFilterSpec() {}

    public static Specification<Pedido> comFiltros(PedidoFiltroDTO filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtro.getDataEntregaInicio() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dataHoraEntrega"), filtro.getDataEntregaInicio()));
            }

            if (filtro.getDataEntregaFim() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dataHoraEntrega"), filtro.getDataEntregaFim()));
            }

            if (!StringValidator.isNullOrBlank(filtro.getCliente())) {
                predicates.add(cb.like(cb.lower(root.get("cliente")), "%" + filtro.getCliente().toLowerCase() + "%"));
            }

            if (filtro.getStatusPedido() != null) {
                predicates.add(cb.equal(root.get("statusPedido"), filtro.getStatusPedido()));
            }

            if (Boolean.TRUE.equals(filtro.getPagamentoPendente())) {
                predicates.add(cb.equal(root.get("pagamentoIntegral"), false));
                predicates.add(cb.notEqual(root.get("statusPedido"), StatusPedido.CANCELADO));
            }

            if (filtro.getCadastradoPorId() != null) {
                predicates.add(cb.equal(root.get("cadastradoPor").get("id"), filtro.getCadastradoPorId()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
