package br.com.senac.rentacar.application.services;

import br.com.senac.rentacar.application.DTOs.VeiculoRequest;
import br.com.senac.rentacar.application.DTOs.VeiculoResponse;
import br.com.senac.rentacar.domain.entities.EnumStatusVeiculo;
import br.com.senac.rentacar.domain.entities.Veiculo;
import br.com.senac.rentacar.domain.respository.CategoriaRepository;
import br.com.senac.rentacar.domain.respository.VeiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VeiculoService {

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    // Diferencial do projeto: quando km atual - km da ultima revisao chega a este valor, avisa a revisao
    @Value("${rentacar.revisao.limite-km:10000}")
    private int limiteRevisaoKm;

    public int getLimiteRevisaoKm() {
        return limiteRevisaoKm;
    }

    public boolean revisaoPendente(Veiculo v) {
        return v.getQuilometragem() - v.getKmUltimaRevisao() >= limiteRevisaoKm;
    }

    public List<VeiculoResponse> listar(boolean apenasDisponiveis) {
        List<Veiculo> veiculos = apenasDisponiveis
                ? veiculoRepository.findByStatus(EnumStatusVeiculo.DISPONIVEL).stream().filter(v -> !revisaoPendente(v)).toList()
                : veiculoRepository.findAll();
        return veiculos.stream().map(v -> new VeiculoResponse(v, limiteRevisaoKm)).toList();
    }

    public VeiculoResponse buscar(Long id) {
        return veiculoRepository.findById(id).map(v -> new VeiculoResponse(v, limiteRevisaoKm)).orElse(null);
    }

    @Transactional
    public VeiculoResponse criar(VeiculoRequest r) {
        validarCampos(r);
        validarPlacaLivre(r.placa(), null);

        EnumStatusVeiculo status = r.status() == null ? EnumStatusVeiculo.DISPONIVEL : r.status();
        if (status == EnumStatusVeiculo.ALUGADO) {
            throw new IllegalArgumentException("O status Alugado e definido pelo sistema ao registrar uma locacao!");
        }

        Veiculo v = new Veiculo();
        aplicarCampos(v, r);
        v.setStatus(status);
        int km = r.quilometragem() == null ? 0 : r.quilometragem();
        v.setQuilometragem(km);
        // sem informar, assume que a ultima revisao e a km atual (veiculo novo nao nasce com alerta)
        int kmRevisao = r.kmUltimaRevisao() == null ? km : r.kmUltimaRevisao();
        validarKmRevisao(km, kmRevisao);
        v.setKmUltimaRevisao(kmRevisao);

        return new VeiculoResponse(veiculoRepository.save(v), limiteRevisaoKm);
    }

    /** @return null se o veiculo nao existe */
    @Transactional
    public VeiculoResponse atualizar(Long id, VeiculoRequest r) {
        Veiculo v = veiculoRepository.buscarParaAtualizar(id).orElse(null);
        if (v == null) {
            return null;
        }
        validarCampos(r);
        validarPlacaLivre(r.placa(), id);

        EnumStatusVeiculo pedido = r.status() == null ? v.getStatus() : r.status();
        if (v.getStatus() == EnumStatusVeiculo.ALUGADO && pedido != EnumStatusVeiculo.ALUGADO) {
            throw new IllegalArgumentException("Veiculo alugado: registre a devolucao da locacao antes de mudar o status!");
        }
        if (v.getStatus() != EnumStatusVeiculo.ALUGADO && pedido == EnumStatusVeiculo.ALUGADO) {
            throw new IllegalArgumentException("O status Alugado e definido pelo sistema ao registrar uma locacao!");
        }

        aplicarCampos(v, r);
        v.setStatus(pedido);
        if (r.quilometragem() != null) {
            if (r.quilometragem() < v.getQuilometragem()) {
                throw new IllegalArgumentException("A quilometragem nao pode diminuir (atual: " + v.getQuilometragem() + " km)!");
            }
            v.setQuilometragem(r.quilometragem());
        }
        if (r.kmUltimaRevisao() != null) {
            v.setKmUltimaRevisao(r.kmUltimaRevisao());
        }
        validarKmRevisao(v.getQuilometragem(), v.getKmUltimaRevisao());

        return new VeiculoResponse(veiculoRepository.save(v), limiteRevisaoKm);
    }

    /** @return false se o veiculo nao existe */
    @Transactional
    public boolean excluir(Long id) {
        Veiculo v = veiculoRepository.findById(id).orElse(null);
        if (v == null) {
            return false;
        }
        if (v.getStatus() == EnumStatusVeiculo.ALUGADO) {
            throw new IllegalArgumentException("Veiculo alugado nao pode ser excluido: registre a devolucao antes!");
        }
        veiculoRepository.delete(v);
        return true;
    }

    /** Revisao feita: a km atual passa a ser a base do proximo alerta. */
    @Transactional
    public VeiculoResponse registrarRevisao(Long id) {
        Veiculo v = veiculoRepository.buscarParaAtualizar(id).orElse(null);
        if (v == null) {
            return null;
        }
        v.setKmUltimaRevisao(v.getQuilometragem());
        return new VeiculoResponse(veiculoRepository.save(v), limiteRevisaoKm);
    }

    private void aplicarCampos(Veiculo v, VeiculoRequest r) {
        v.setMarca(r.marca().trim());
        v.setModelo(r.modelo().trim());
        v.setPlaca(r.placa().trim().toUpperCase());
        v.setAno(r.ano());
        v.setValorDiaria(r.valorDiaria());
        v.setCategoria(categoriaRepository.findById(r.categoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria nao encontrada!")));
    }

    private void validarCampos(VeiculoRequest r) {
        if (r == null
                || r.marca() == null || r.marca().isBlank()
                || r.modelo() == null || r.modelo().isBlank()
                || r.placa() == null || r.placa().isBlank()) {
            throw new IllegalArgumentException("Informe marca, modelo e placa do veiculo!");
        }
        validarAno(r.ano());
        if (r.valorDiaria() == null || r.valorDiaria() <= 0) {
            throw new IllegalArgumentException("O valor da diaria deve ser maior que zero!");
        }
        if (r.categoriaId() == null) {
            throw new IllegalArgumentException("Selecione a categoria do veiculo!");
        }
        if (r.quilometragem() != null && r.quilometragem() < 0) {
            throw new IllegalArgumentException("A quilometragem nao pode ser negativa!");
        }
    }

    private void validarAno(String ano) {
        int max = java.time.Year.now().getValue() + 1;
        if (ano == null || !ano.trim().matches("\\d{4}") || Integer.parseInt(ano.trim()) < 1980 || Integer.parseInt(ano.trim()) > max) {
            throw new IllegalArgumentException("Informe um ano valido (4 digitos, entre 1980 e " + max + ")!");
        }
    }

    private void validarKmRevisao(int km, int kmRevisao) {
        if (kmRevisao < 0 || kmRevisao > km) {
            throw new IllegalArgumentException("A km da ultima revisao deve estar entre 0 e a quilometragem atual!");
        }
    }

    private void validarPlacaLivre(String placa, Long idAtual) {
        var existente = veiculoRepository.findFirstByPlacaIgnoreCase(placa.trim()).orElse(null);
        if (existente != null && !existente.getId().equals(idAtual)) {
            throw new IllegalArgumentException("Ja existe um veiculo com esta placa!");
        }
    }
}
