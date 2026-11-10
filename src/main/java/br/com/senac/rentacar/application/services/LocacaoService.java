package br.com.senac.rentacar.application.services;

import br.com.senac.rentacar.application.DTOs.DevolucaoResponse;
import br.com.senac.rentacar.domain.entities.EnumStatusLocacao;
import br.com.senac.rentacar.domain.entities.EnumStatusVeiculo;
import br.com.senac.rentacar.domain.entities.Locacao;
import br.com.senac.rentacar.domain.entities.Veiculo;
import br.com.senac.rentacar.domain.respository.ClienteRepository;
import br.com.senac.rentacar.domain.respository.LocacaoRepository;
import br.com.senac.rentacar.domain.respository.VeiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class LocacaoService {

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager em;

    /**
     * Busca o veiculo com trava (FOR UPDATE) e RELE o estado do banco: se o veiculo ja tinha sido carregado
     * nesta requisicao (cache do Hibernate), sem o refresh a checagem de disponibilidade usaria o status velho
     * e duas locacoes simultaneas do mesmo carro passariam.
     */
    private java.util.Optional<Veiculo> travarVeiculo(Long id) {
        java.util.Optional<Veiculo> v = veiculoRepository.buscarParaAtualizar(id);
        v.ifPresent(em::refresh);
        return v;
    }

    @Autowired
    private LocacaoRepository locacaoRepository;

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private VeiculoService veiculoService;

    /**
     * REGRA CENTRAL: veiculo com status EM_MANUTENCAO ou ALUGADO nao pode ser usado em nova locacao.
     * Ao criar, o veiculo passa a ALUGADO.
     */
    @Transactional
    public Locacao criar(Locacao dados) {
        validarDados(dados);
        Veiculo veiculo = exigirVeiculoDisponivel(dados.getVeiculo());

        Locacao nova = new Locacao();
        copiarDados(dados, nova);
        nova.setStatus(EnumStatusLocacao.EM_ANDAMENTO);

        veiculo.setStatus(EnumStatusVeiculo.ALUGADO);
        veiculoRepository.save(veiculo);

        return locacaoRepository.save(nova);
    }

    /** @return null se a locacao nao existe */
    @Transactional
    public Locacao atualizar(Long id, Locacao dados) {
        Locacao banco = locacaoRepository.findById(id).orElse(null);
        if (banco == null) {
            return null;
        }
        validarDados(dados);

        Long idNovo = idDoVeiculo(dados.getVeiculo());
        Long idAtual = banco.getVeiculo() == null ? null : idDoVeiculo(banco.getVeiculo());

        if (!idNovo.equals(idAtual)) {
            if (!banco.emAndamento()) {
                throw new IllegalArgumentException("Locacao finalizada nao pode trocar de veiculo!");
            }
            // troca de veiculo numa locacao em andamento: o novo tambem precisa estar disponivel
            Veiculo novo = exigirVeiculoDisponivel(dados.getVeiculo());
            novo.setStatus(EnumStatusVeiculo.ALUGADO);
            veiculoRepository.save(novo);
            liberarVeiculo(banco.getVeiculo());
        }

        copiarDados(dados, banco);
        return locacaoRepository.save(banco);
    }

    /** @return false se a locacao nao existe */
    @Transactional
    public boolean excluir(Long id) {
        Locacao banco = locacaoRepository.findById(id).orElse(null);
        if (banco == null) {
            return false;
        }
        if (banco.emAndamento()) {
            liberarVeiculo(banco.getVeiculo());
        }
        locacaoRepository.delete(banco);
        return true;
    }

    /**
     * Devolucao: finaliza a locacao, libera o veiculo, atualiza a quilometragem e
     * DIFERENCIAL: avisa se o veiculo passou do limite de km desde a ultima revisao.
     *
     * @return null se a locacao nao existe
     */
    @Transactional
    public DevolucaoResponse devolver(Long id, Integer quilometragemFinal) {
        Locacao locacao = locacaoRepository.findById(id).orElse(null);
        if (locacao == null) {
            return null;
        }
        if (!locacao.emAndamento()) {
            throw new IllegalArgumentException("Esta locacao ja foi finalizada!");
        }
        if (quilometragemFinal == null || quilometragemFinal < 0) {
            throw new IllegalArgumentException("Informe a quilometragem final do veiculo!");
        }

        Veiculo veiculo = travarVeiculo(idDoVeiculo(locacao.getVeiculo()))
                .orElseThrow(() -> new IllegalArgumentException("Veiculo da locacao nao encontrado!"));

        if (quilometragemFinal < veiculo.getQuilometragem()) {
            throw new IllegalArgumentException("A quilometragem final nao pode ser menor que a atual do veiculo ("
                    + veiculo.getQuilometragem() + " km)!");
        }

        veiculo.setQuilometragem(quilometragemFinal);
        if (veiculo.getStatus() == EnumStatusVeiculo.ALUGADO) {
            veiculo.setStatus(EnumStatusVeiculo.DISPONIVEL);
        }
        veiculoRepository.save(veiculo);

        locacao.setStatus(EnumStatusLocacao.FINALIZADA);
        locacao.setQuilometragemDevolucao(quilometragemFinal);
        locacao.setDataDevolucao(LocalDate.now());
        locacaoRepository.save(locacao);

        int desdeRevisao = quilometragemFinal - veiculo.getKmUltimaRevisao();
        int limite = veiculoService.getLimiteRevisaoKm();
        boolean alerta = desdeRevisao >= limite;

        String nomeVeiculo = veiculo.getMarca() + " " + veiculo.getModelo() + " (" + veiculo.getPlaca() + ")";
        String mensagem = alerta
                ? "ALERTA DE REVISAO: " + nomeVeiculo + " ja rodou " + desdeRevisao
                  + " km desde a ultima revisao (limite: " + limite + " km). Encaminhe para revisao."
                : "Devolucao registrada. " + nomeVeiculo + " rodou " + desdeRevisao
                  + " km desde a ultima revisao (limite: " + limite + " km).";

        return new DevolucaoResponse(locacao.getId(), veiculo.getId(), quilometragemFinal,
                desdeRevisao, limite, alerta, mensagem);
    }

    // ------------------------------------------------------------------ apoio

    private void liberarVeiculo(String idTexto) {
        if (idTexto == null) {
            return;
        }
        travarVeiculo(idDoVeiculo(idTexto)).ifPresent(v -> {
            if (v.getStatus() == EnumStatusVeiculo.ALUGADO) {
                v.setStatus(EnumStatusVeiculo.DISPONIVEL);
                veiculoRepository.save(v);
            }
        });
    }

    private Veiculo exigirVeiculoDisponivel(String idTexto) {
        Veiculo veiculo = travarVeiculo(idDoVeiculo(idTexto))
                .orElseThrow(() -> new IllegalArgumentException("Veiculo nao encontrado!"));

        if (veiculo.getStatus() != EnumStatusVeiculo.DISPONIVEL) {
            String motivo = veiculo.getStatus() == EnumStatusVeiculo.EM_MANUTENCAO ? "Em Manutencao" : "Alugado";
            throw new IllegalArgumentException("Veiculo indisponivel (" + motivo
                    + "): veiculos Em Manutencao ou Alugados nao podem ser selecionados para novas locacoes!");
        }
        if (veiculoService.revisaoPendente(veiculo)) {
            throw new IllegalArgumentException("Veiculo com revisao pendente (" + (veiculo.getQuilometragem() - veiculo.getKmUltimaRevisao())
                    + " km desde a ultima revisao, limite " + veiculoService.getLimiteRevisaoKm() + " km): registre a revisao antes de alugar!");
        }
        return veiculo;
    }

    private Long idDoVeiculo(String texto) {
        try {
            return Long.valueOf(texto.trim());
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException("Veiculo invalido!");
        }
    }

    private void copiarDados(Locacao origem, Locacao destino) {
        destino.setDataInicio(origem.getDataInicio());
        destino.setDataFim(origem.getDataFim());
        destino.setValorTotal(origem.getValorTotal());
        destino.setCliente(origem.getCliente().trim());
        destino.setVeiculo(origem.getVeiculo().trim());
    }

    private void validarDados(Locacao locacao) {
        if (locacao == null || locacao.getDataInicio() == null || locacao.getDataFim() == null) {
            throw new IllegalArgumentException("Informe a data de inicio e a data de fim!");
        }
        if (locacao.getDataFim().isBefore(locacao.getDataInicio())) {
            throw new IllegalArgumentException("A data de fim nao pode ser anterior a data de inicio!");
        }
        if (locacao.getValorTotal() == null || locacao.getValorTotal() < 0) {
            throw new IllegalArgumentException("Informe um valor total valido!");
        }
        if (locacao.getCliente() == null || locacao.getCliente().isBlank()
                || locacao.getVeiculo() == null || locacao.getVeiculo().isBlank()) {
            throw new IllegalArgumentException("Selecione o cliente e o veiculo!");
        }
        Long idCliente;
        try {
            idCliente = Long.valueOf(locacao.getCliente().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cliente invalido!");
        }
        if (!clienteRepository.existsById(idCliente)) {
            throw new IllegalArgumentException("Cliente nao encontrado!");
        }
    }
}
