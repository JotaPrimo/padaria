package padaria.com.example.padaria.service;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import padaria.com.example.padaria.dto.pagamento.PagamentoFiltroDTO;
import padaria.com.example.padaria.dto.pedido.PedidoFiltroDTO;
import padaria.com.example.padaria.entity.Pagamento;
import padaria.com.example.padaria.entity.Pedido;
import padaria.com.example.padaria.entity.Usuario;
import padaria.com.example.padaria.enums.StatusPedido;
import padaria.com.example.padaria.utils.StringValidator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PagamentoFilterSpec {

    private PagamentoFilterSpec() {}

    public static Specification<Pagamento> comFiltros(PagamentoFiltroDTO filtro) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            addIdPredicate(filtro, root, cb, predicates);
            addPedidoPredicate(filtro, root, cb, predicates);
            addValorMinPredicate(filtro, root, cb, predicates);
            addRegistradoPorPredicate(filtro, root, cb, predicates);
            addAdiantamentoPredicate(filtro, root, cb, predicates);
            addDataRegistroPredicate(filtro, root, cb, predicates);
            addValidoPredicate(filtro, root, cb, predicates);
            addInvalidadoPorPredicate(filtro, root, cb, predicates);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void addIdPredicate(PagamentoFiltroDTO filtro, Root<Pagamento> root,
                                       CriteriaBuilder cb, List<Predicate> predicates) {
        if (filtro.id() != null) {
            predicates.add(cb.equal(root.get("id"), filtro.id()));
        }
    }

    private static void addPedidoPredicate(PagamentoFiltroDTO filtro, Root<Pagamento> root,
                                           CriteriaBuilder cb, List<Predicate> predicates) {
        if (filtro.pedido() != null && filtro.pedido().getId() != null) {
            predicates.add(cb.equal(root.get("pedido").get("id"), filtro.pedido().getId()));
        }
    }

    private static void addValorMinPredicate(PagamentoFiltroDTO filtro, Root<Pagamento> root,
                                             CriteriaBuilder cb, List<Predicate> predicates) {
        if (filtro.valorMin() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("valor"), filtro.valorMin()));
        }
    }

    private static void addRegistradoPorPredicate(PagamentoFiltroDTO filtro, Root<Pagamento> root,
                                                  CriteriaBuilder cb, List<Predicate> predicates) {
        if (filtro.registradoPor() != null && filtro.registradoPor().getId() != null) {
            predicates.add(cb.equal(root.get("registradoPor").get("id"), filtro.registradoPor().getId()));
        }
    }

    private static void addAdiantamentoPredicate(PagamentoFiltroDTO filtro, Root<Pagamento> root,
                                                 CriteriaBuilder cb, List<Predicate> predicates) {
        if (filtro.adiantamento() != null) {
            predicates.add(cb.equal(root.get("adiantamento"), filtro.adiantamento()));
        }
    }

    private static void addDataRegistroPredicate(PagamentoFiltroDTO filtro, Root<Pagamento> root,
                                                 CriteriaBuilder cb, List<Predicate> predicates) {
        if (filtro.dataRegistroInicial() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("dataRegistro"), filtro.dataRegistroInicial()));
        }
    }

    private static void addValidoPredicate(PagamentoFiltroDTO filtro, Root<Pagamento> root,
                                           CriteriaBuilder cb, List<Predicate> predicates) {
        if (filtro.valido() != null) {
            predicates.add(cb.equal(root.get("valido"), filtro.valido()));
        }
    }

    private static void addInvalidadoPorPredicate(PagamentoFiltroDTO filtro, Root<Pagamento> root,
                                                  CriteriaBuilder cb, List<Predicate> predicates) {
        if (filtro.invaliadoPor() != null && filtro.invaliadoPor().getId() != null) {
            predicates.add(cb.equal(root.get("invaliadoPor").get("id"), filtro.invaliadoPor().getId()));
        }
    }
}