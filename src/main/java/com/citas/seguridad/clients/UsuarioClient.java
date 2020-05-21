package com.citas.seguridad.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.citas.libreria.entidades.models.entity.Usuario;


@FeignClient(name="backend-citas-usuario")
public interface UsuarioClient {

	@GetMapping("/{username}")
	public Usuario consultarUsuario(
			@PathVariable("username") String username);

	@PutMapping("/actualizar/{username}")
	public Usuario actualizar(
			@PathVariable String username, @RequestBody Usuario usuario);
	
}
