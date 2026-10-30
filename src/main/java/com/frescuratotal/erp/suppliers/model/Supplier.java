package com.frescuratotal.erp.suppliers.model;

import java.time.LocalDateTime;

public class Supplier {
    private int id_proveedor;
    private String nombre;
    private String nit;
    private String persona_contacto;
    private String email;
    private String telefono;
    private String direccion;
    private int activo;
    private LocalDateTime fecha_registro;

    public Supplier(int id_proveedor, String nombre, String nit, String persona_contacto, String email, String telefono, String direccion, int activo, LocalDateTime fecha_registro) {
        this.id_proveedor = id_proveedor;
        this.nombre = nombre;
        this.nit = nit;
        this.persona_contacto = persona_contacto;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
        this.activo = activo;
        this.fecha_registro = fecha_registro;
    }

    public int getId_proveedor() {
        return id_proveedor;
    }

    public void setId_proveedor(int id_proveedor) {
        this.id_proveedor = id_proveedor;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getPersona_contacto() {
        return persona_contacto;
    }

    public void setPersona_contacto(String persona_contacto) {
        this.persona_contacto = persona_contacto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public int getActivo() {
        return activo;
    }

    public void setActivo(int activo) {
        this.activo = activo;
    }

    public LocalDateTime getFecha_registro() {
        return fecha_registro;
    }

    public void setFecha_registro(LocalDateTime fecha_registro) {
        this.fecha_registro = fecha_registro;
    }

    @Override
    public String toString() {
        return "Supplier{" +
                "id_proveedor=" + id_proveedor +
                ", nombre='" + nombre + '\'' +
                ", nit='" + nit + '\'' +
                ", persona_contacto='" + persona_contacto + '\'' +
                ", email='" + email + '\'' +
                ", telefono='" + telefono + '\'' +
                ", direccion='" + direccion + '\'' +
                ", activo=" + activo +
                ", fecha_registro=" + fecha_registro +
                '}';
    }
}
