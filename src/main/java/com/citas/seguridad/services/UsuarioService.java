package com.citas.seguridad.services;

import com.citas.libreria.entidades.models.entity.Usuario;

public interface UsuarioService {

	public Usuario consultarLogin(String username);
	
	public Usuario actualizar(Usuario usuario, String username);
	
}
