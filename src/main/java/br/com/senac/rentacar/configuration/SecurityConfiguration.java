package br.com.senac.rentacar.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain (HttpSecurity http) throws Exception{

        return http.cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login",
                                "/swagger-ui/**",
                                "/webjars/**",
                                "/swagger-resources/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // vitrine da landing page (somente leitura, sem dados sensiveis)
                        .requestMatchers(HttpMethod.GET, "/publico/veiculos").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/cadastro").permitAll()
                        // pagina de erro do Spring: sem isso um JSON malformado virava 403 em vez de 400
                        .requestMatchers("/error").permitAll()
                        // usado pelo desktop: protegido pela secretKey (ver UsuarioService.criarAdmin)
                        .requestMatchers(HttpMethod.POST, "/usuarios/admin").permitAll()
                        // qualquer usuario logado pode consultar os proprios dados
                        .requestMatchers(HttpMethod.GET, "/usuarios/buscarusuariologado").authenticated()
                        // categorias: qualquer usuario logado consulta; so ADMIN cria/altera/exclui (feito pelo desktop)
                        .requestMatchers(HttpMethod.GET, "/categorias", "/categorias/**").authenticated()
                        .requestMatchers("/categorias/**", "/categorias").hasRole("ADMIN")
                        // area do cliente (auto-cadastro na landing page): so ele pede/ve as proprias locacoes
                        .requestMatchers("/minhas-locacoes", "/minhas-locacoes/**").hasRole("CLIENTE")
                        .requestMatchers("/usuarios/**")
                        .hasRole("ADMIN")
                        // cliente (ROLE_CLIENTE) nao acessa o restante do sistema do atendente
                        .anyRequest().hasAnyRole("ADMIN", "USER")
                )
                .addFilterBefore( jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();

    }


}
