package com.citas.seguridad.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.exceptions.UserDeniedAuthorizationException;
import org.springframework.stereotype.Component;

import com.citas.libreria.entidades.models.entity.Usuario;
import com.citas.seguridad.services.UsuarioService;

import brave.Tracer;
import feign.FeignException;

@Component
public class SeguridadManejoErrores implements AuthenticationEventPublisher{

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private UsuarioService usuarioService;
	
	@Autowired
	private Tracer tracer;

	@Override
	public void publishAuthenticationSuccess(Authentication authentication) {
		UserDetails user = (UserDetails) authentication.getPrincipal();
		String mensaje = "Success Login: " + user.getUsername();
		System.out.println(mensaje);
		log.info(mensaje);
		
		Usuario usuario = usuarioService.consultarLogin(authentication.getName());
		
		if(usuario.getIntentos() > 0) {
			usuario.setIntentos(0);
			usuarioService.actualizar(usuario, usuario.getUsername());
		}
	}

	@Override
	public void publishAuthenticationFailure(AuthenticationException exception, Authentication authentication) {
		String mensaje = "Error en el Login: " + exception.getMessage();
		log.error(mensaje);
		System.out.println(mensaje);

		try {
			
			StringBuilder errors = new StringBuilder();
			errors.append(mensaje);
			
			Usuario usuario = usuarioService.consultarLogin(authentication.getName());
			
			if(usuario.getEstado() == 2) {
				String usuarioInactivo = "El usuario " + usuario.getUsername() + " es inactivo";
				log.error(usuarioInactivo);
				System.err.println(usuarioInactivo);
				errors.append(" - " + usuarioInactivo);
				tracer.currentSpan().tag("error.mensaje", errors.toString());
				throw new UserDeniedAuthorizationException(usuarioInactivo);
			}
			
			usuario.setIntentos(usuario.getIntentos()+1);
			
			String numeroIntentos ="Intentos del login: " + usuario.getIntentos(); 
			errors.append(" - " + numeroIntentos);
			System.err.println(numeroIntentos);
			
			if(usuario.getIntentos() >= 3) {
				
				usuario.setEstado(1);
				usuarioService.actualizar(usuario, usuario.getUsername());
				
				String errorMaxIntentos = "El usuario " + usuario.getUsername() + " ha sido bloqueado por exceder el numero intentos permitidos";
				log.error(errorMaxIntentos);
				errors.append(" - " + errorMaxIntentos);
				throw new BadCredentialsException(errorMaxIntentos);
			}
			
			usuarioService.actualizar(usuario, usuario.getUsername());
			tracer.currentSpan().tag("error.mensaje", errors.toString());
			
			throw new BadCredentialsException("Error en las credenciales del usuario");
			
		} catch (FeignException e) {
			String error = "Error en generar el token de seguridad " + e.getMessage();
			e.printStackTrace();
			log.error(error);
			tracer.currentSpan().tag("error.mensaje", error);
			System.err.println(error);
		}

	}
	
}
