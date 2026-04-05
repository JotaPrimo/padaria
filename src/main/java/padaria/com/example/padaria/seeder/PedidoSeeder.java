package padaria.com.example.padaria.seeder;

import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.stereotype.Service;
import padaria.com.example.padaria.entity.Pedido;
import padaria.com.example.padaria.entity.Usuario;
import padaria.com.example.padaria.enums.MotivoCancelamento;
import padaria.com.example.padaria.enums.StatusPedido;
import padaria.com.example.padaria.repository.PedidoRepository;
import padaria.com.example.padaria.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PedidoSeeder implements ISeeder {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;

    private final Faker faker = new Faker(new Locale("pt", "BR"));
    private final Random random = new Random();

    private static final List<String> PRODUTOS = List.of(
            "Bolo de chocolate com cobertura de brigadeiro",
            "Bolo de cenoura com cobertura de chocolate",
            "Torta de frango",
            "Torta de palmito",
            "Torta de morango",
            "Pão de mel recheado",
            "Coxinha de frango (dúzia)",
            "Enroladinho de salsicha (dúzia)",
            "Pão de queijo especial (500g)",
            "Bolo de aniversário personalizado",
            "Sonho de creme (dúzia)",
            "Rosca de Páscoa",
            "Panetone recheado com chocolate",
            "Quiche de queijo e presunto",
            "Empada de palmito (dúzia)"
    );

    @Override
    public void executar(int quantidade) {
        List<Usuario> usuarios = usuarioRepository.findAll();
        if (usuarios.isEmpty()) {
            throw new IllegalStateException(
                    "Nenhum usuário encontrado no banco. Execute o seeder de usuários antes de semear pedidos.");
        }

        for (int i = 0; i < quantidade; i++) {
            var pedido = new Pedido();
            pedido.setCliente(faker.name().fullName());
            pedido.setTelefone(gerarTelefone());
            pedido.setDescricaoPedido(gerarDescricao());
            pedido.setCadastradoPor(usuarioAleatorio(usuarios));
            pedido.setAlteradoPor(usuarioAleatorio(usuarios));

            if (random.nextInt(10) < 4) {
                pedido.setObservacao(faker.lorem().sentence(6));
            }

            BigDecimal valor = BigDecimal.valueOf(random.nextInt(47001) + 3000)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            pedido.setValorPedido(valor);

            boolean pagamentoIntegral = random.nextBoolean();
            pedido.setPagamentoIntegral(pagamentoIntegral);
            if (!pagamentoIntegral) {
                double percentual = 0.20 + (random.nextDouble() * 0.30);
                BigDecimal adiantamento = valor.multiply(BigDecimal.valueOf(percentual))
                        .setScale(2, RoundingMode.HALF_UP);
                pedido.setValorAdiantamento(adiantamento);
            }

            int sorteio = random.nextInt(100);
            if (sorteio < 60) {
                aplicarPendente(pedido);
            } else if (sorteio < 85) {
                aplicarEntregue(pedido);
            } else {
                aplicarCancelado(pedido);
            }

            pedidoRepository.save(pedido);
        }
    }

    private void aplicarPendente(Pedido pedido) {
        pedido.setStatusPedido(StatusPedido.PENDENTE);
        int diasFuturos = random.nextInt(30) + 1;
        int hora = random.nextInt(12) + 7;
        pedido.setDataHoraEntrega(LocalDateTime.now().plusDays(diasFuturos)
                .withHour(hora).withMinute(0).withSecond(0).withNano(0));
    }

    private void aplicarEntregue(Pedido pedido) {
        pedido.setStatusPedido(StatusPedido.ENTREGUE);
        int diasAtras = random.nextInt(90) + 1;
        int hora = random.nextInt(12) + 7;
        pedido.setDataHoraEntrega(LocalDateTime.now().minusDays(diasAtras)
                .withHour(hora).withMinute(0).withSecond(0).withNano(0));
    }

    private void aplicarCancelado(Pedido pedido) {
        pedido.setStatusPedido(StatusPedido.CANCELADO);

        int diasAtras = random.nextInt(90) + 1;
        int hora = random.nextInt(12) + 7;
        pedido.setDataHoraEntrega(LocalDateTime.now().minusDays(diasAtras + 1)
                .withHour(hora).withMinute(0).withSecond(0).withNano(0));

        pedido.setDataCancelamento(LocalDate.now().minusDays(diasAtras));

        MotivoCancelamento[] motivos = MotivoCancelamento.values();
        MotivoCancelamento motivo = motivos[random.nextInt(motivos.length)];
        pedido.setMotivoCancelamento(motivo);

        if (motivo == MotivoCancelamento.OUTRO) {
            pedido.setObsCancelamento(faker.lorem().sentence(8));
        }
    }

    private String gerarTelefone() {
        int ddd = random.nextInt(89) + 11;
        String parte1 = String.format("%04d", random.nextInt(10000));
        String parte2 = String.format("%04d", random.nextInt(10000));
        return "(" + ddd + ") 9" + parte1 + "-" + parte2;
    }

    private String gerarDescricao() {
        int qtd = random.nextInt(4) + 1;
        String produto = PRODUTOS.get(random.nextInt(PRODUTOS.size()));
        return qtd + "x " + produto;
    }

    private Usuario usuarioAleatorio(List<Usuario> usuarios) {
        return usuarios.get(random.nextInt(usuarios.size()));
    }
}
