package com.citas.seguridad.services;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.citas.libreria.entidades.models.entity.Usuario;
import com.citas.seguridad.clients.UsuarioClient;

import brave.Tracer;
import feign.FeignException;

@Service
public class UsuarioServiceImpl  implements UsuarioService, UserDetailsService{

	@Autowired
	private UsuarioClient usuarioClient;

	@Autowired
	private Tracer zipkin;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		try {
			Usuario usuario = usuarioClient.consultarUsuario(username);
			
			List<GrantedAuthority> authorities = usuario.getRoles().stream()
					.map(role -> new SimpleGrantedAuthority(role.getDescripcion()))
					.peek(authority -> logger.info("Role: " + authority.getAuthority())).collect(Collectors.toList());

			logger.info("Usuario autenticado: " + username);
			
			boolean enabled = usuario.getEstado() == 0 ? true : false;

			return new User(usuario.getUsername(), usuario.getPassword(), enabled, true, true, true,
					authorities);
			
		} catch (FeignException e) {
			String userNoFound = "usuario " + username + " no existe en la base de datos";
			logger.error(userNoFound);
			zipkin.currentSpan().tag("error.mensaje", userNoFound);
			throw new UsernameNotFoundException(userNoFound);
		}
		
	}

	@Override
	public Usuario consultarLogin(String username) {
		return this.usuarioClient.consultarUsuario(username);
	}

	@Override
	public Usuario actualizar(Usuario usuario, String username) {
		return this.usuarioClient.actualizar(username, usuario);
	}

}
