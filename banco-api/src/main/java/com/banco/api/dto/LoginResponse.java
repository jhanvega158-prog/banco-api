package com.banco.api.dto;

public class LoginResponse {

    private String token;
    private String nombre;
    private String email;
    private String rol;

    public LoginResponse(String token, String nombre, String email, String rol) {
        this.token = token;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
    }

    public String getToken() {
        return token;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getRol() {
        return rol;
    }
}