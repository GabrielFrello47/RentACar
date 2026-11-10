package br.com.senac.rentacar.application.services;


import br.com.senac.rentacar.application.DTOs.*;
import br.com.senac.rentacar.domain.entities.Cliente;
import br.com.senac.rentacar.domain.entities.EnumStatusUsuario;
import br.com.senac.rentacar.domain.entities.Usuario;
import br.com.senac.rentacar.domain.respository.UsuarioRepository;
import br.com.senac.rentacar.domain.valueobject.CPF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private CpfDuplicidade cpfDuplicidade;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private br.com.senac.rentacar.domain.respository.ClienteRepository clienteRepository;

    @Value("${spring.secret}")
    private String secret;


    public LoginResponse validarUsuarioAutenticadoRetornaToken(LoginRequest resquest) {

        if (resquest == null || resquest.email() == null || resquest.senha() == null) {
            return null;
        }

        Usuario usuario = usuarioRepository.findFirstByEmail(resquest.email()).orElse(null);

        if (usuario == null || usuario.getStatus() != EnumStatusUsuario.ATIVO) {
            return null;
        }

        if (!senhaConfere(usuario, resquest.senha())) {
            return null;
        }

        return new LoginResponse(tokenService.gerarToken(usuario));
    }

    private boolean senhaConfere(Usuario usuario, String senhaDigitada) {
        String senhaBanco = usuario.getSenha();
        if (senhaBanco == null) {
            return false;
        }

        // senha em texto puro, como no projeto do professor (comparacao em tempo constante)
        return MessageDigest.isEqual(senhaBanco.getBytes(StandardCharsets.UTF_8),
                senhaDigitada.getBytes(StandardCharsets.UTF_8));
    }


    public List<UsuarioResponse> listarTodosUsuariosTable(){

        // usuarios excluidos (exclusao logica) nao aparecem na listagem
        return  usuarioRepository.findByStatusNot(EnumStatusUsuario.EXCLUIDO)
                .stream()
                .map(UsuarioResponse::new)
                .toList();
    }

    /**
     * @return a resposta de sucesso, ou null se a secretKey informada nao for a do servidor.
     */
    public CriarAdminResponse criarAdmin(CriarAdminRequest criarAdminRequest) {

        if(!chaveValida(criarAdminRequest.secretKey())){
            return null;
        }

        validarSenha(criarAdminRequest.senha());
        validarEmailDisponivel(criarAdminRequest.email(), null);
        validarCpfLivre(criarAdminRequest.cpf(), null);

        Usuario usuarioAdminSalvar = new Usuario(criarAdminRequest);
        usuarioAdminSalvar.setSenha(criarAdminRequest.senha());
        usuarioRepository.save(usuarioAdminSalvar);

        return new CriarAdminResponse(usuarioAdminSalvar.getId(),"Usuario Salvo com sucesso!");
    }

    private boolean chaveValida(String chaveInformada) {
        if (chaveInformada == null) {
            return false;
        }
        // comparacao em tempo constante
        return MessageDigest.isEqual(
                chaveInformada.getBytes(StandardCharsets.UTF_8),
                secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Auto-cadastro do cliente (landing page): cria o login com ROLE_CLIENTE e o registro de Cliente
     * (reaproveita o Cliente ja cadastrado pelo atendente com o mesmo e-mail).
     */
    public UsuarioResponse cadastrarCliente(CadastroClienteRequest req) {
        if (req == null || req.nome() == null || req.nome().isBlank()) {
            throw new IllegalArgumentException("Informe o nome!");
        }
        validarSenha(req.senha());
        validarEmailDisponivel(req.email(), null);
        validarCpfLivre(req.cpf(), null);
        boolean clienteExiste = req.email() != null && clienteRepository.findFirstByEmailIgnoreCase(req.email().trim()).isPresent();
        if (!clienteExiste && cpfDuplicidade.clienteJaTem(req.cpf(), null)) {
            throw new IllegalArgumentException("CPF ja cadastrado como cliente! Procure a loja para vincular o seu acesso.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(req.nome().trim());
        usuario.setCpf(new CPF(req.cpf()));
        usuario.setEmail(req.email());
        usuario.setSenha(req.senha());
        usuario.setRole("ROLE_CLIENTE");
        usuarioRepository.save(usuario);

        if (clienteRepository.findFirstByEmailIgnoreCase(usuario.getEmail()).isEmpty()) {
            Cliente cliente = new Cliente();
            cliente.setNome(usuario.getNome());
            cliente.setCpf(req.cpf());
            cliente.setTelefone(req.telefone());
            cliente.setEmail(usuario.getEmail());
            clienteRepository.save(cliente);
        }
        return new UsuarioResponse(usuario);
    }

    public UsuarioLogado getUsuarioLogado() {
        return (UsuarioLogado) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public UsuarioResponse salvarUsuario(UsuarioRequest usuario, Long id) {

        //Novo Usuario
        if(id == 0L) {
            validarSenha(usuario.senha());
            validarEmailDisponivel(usuario.email(), null);
            validarCpfLivre(usuario.cpf(), null);

            Usuario usuarioBanco = new Usuario(usuario);
            usuarioBanco.setSenha(usuario.senha());
            usuarioRepository.save(usuarioBanco);

            return new UsuarioResponse(usuarioBanco);
        }else {
            //Usuario existente
            Usuario usuarioBanco = usuarioRepository.findById(id).orElse(null);
            if(usuarioBanco!= null ){
                validarEmailDisponivel(usuario.email(), id);
                validarCpfLivre(usuario.cpf(), id);

                usuarioBanco.setNome(usuario.nome());
                usuarioBanco.setCpf(new CPF(usuario.cpf()));
                usuarioBanco.setEmail(usuario.email());
                // senha em branco = mantem a atual (a API nunca devolve a senha para a tela)
                if (usuario.senha() != null && !usuario.senha().isBlank()) {
                    usuarioBanco.setSenha(usuario.senha());
                }
                usuarioRepository.save(usuarioBanco);
                return  new UsuarioResponse(usuarioBanco);
            }

        }

        return null;
    }

    public UsuarioResponse buscarPorId(Long id) {

        var usuario = usuarioRepository.findById(id).orElse(null);
        if(usuario == null){
            return null;
        }

        return new UsuarioResponse(usuario);


    }

    public UsuarioResponse buscarUsuarioLogado() {
        var usuarioLogado = getUsuarioLogado();
        return usuarioRepository.findById(usuarioLogado.id())
                .map(UsuarioResponse::new)
                .orElse(null);
    }

    public boolean atualizarStatus(EnumStatusUsuario status, Long id) {

        Usuario usuarioBanco = usuarioRepository.findById(id).orElse(null);
        if(usuarioBanco!= null ){
            usuarioBanco.setStatus(status);
            usuarioRepository.save(usuarioBanco);
            return  true;
        }
        return false;

    }

    private void validarCpfLivre(String cpf, Long idAtual) {
        if (cpfDuplicidade.usuarioJaTem(cpf, idAtual)) {
            throw new IllegalArgumentException("CPF ja cadastrado!");
        }
    }

    private void validarSenha(String senha) {
        if (senha == null || senha.length() < 6) {
            throw new IllegalArgumentException("A senha deve ter pelo menos 6 caracteres!");
        }
    }

    private void validarEmailDisponivel(String email, Long idAtual) {
        if (email == null) {
            throw new IllegalArgumentException("Email Invalido!");
        }
        var existente = usuarioRepository.findFirstByEmailIgnoreCase(email.trim()).orElse(null);
        if (existente != null && !existente.getId().equals(idAtual)) {
            throw new IllegalArgumentException("Email ja cadastrado!");
        }
    }
}
