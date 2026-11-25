package com.museodelosmuseos.museo_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "museos")
public class MuseoModel {

    @Id
    @NotBlank
    @Size(max = 20)
    @Column(length = 20)
    private String id;

    @NotBlank
    private String nombre;

    @NotBlank
    private String ciudad;

    @NotBlank
    private String pais;

    @DecimalMin("-90.0") @DecimalMax("90.0")
    private double latitud;

    @DecimalMin("-180.0") @DecimalMax("180.0")
    private double longitud;

    @Column(length = 500)
    private String descripcion;

    @NotBlank
    private String categoria;

    private String proyecto;

    @Column(length = 300)
    private String urlVisitaVirtual;

    @DecimalMin("0.0") @DecimalMax("5.0")
    private double valoracion;

    @Min(0)
    private int resenas;

    public MuseoModel() {}

    public MuseoModel(String id, String nombre, String ciudad, String pais,
                      double latitud, double longitud, String descripcion,
                      String categoria, String proyecto, String urlVisitaVirtual,
                      double valoracion, int resenas) {
        this.id = id;
        this.nombre = nombre;
        this.ciudad = ciudad;
        this.pais = pais;
        this.latitud = latitud;
        this.longitud = longitud;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.proyecto = proyecto;
        this.urlVisitaVirtual = urlVisitaVirtual;
        this.valoracion = valoracion;
        this.resenas = resenas;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getProyecto() {
        return proyecto;
    }

    public void setProyecto(String proyecto) {
        this.proyecto = proyecto;
    }

    public String getUrlVisitaVirtual() {
        return urlVisitaVirtual;
    }

    public void setUrlVisitaVirtual(String urlVisitaVirtual) {
        this.urlVisitaVirtual = urlVisitaVirtual;
    }

    public double getValoracion() {
        return valoracion;
    }

    public void setValoracion(double valoracion) {
        this.valoracion = valoracion;
    }

    public int getResenas() {
        return resenas;
    }

    public void setResenas(int resenas) {
        this.resenas = resenas;
    }
}