package com.museodelosmuseos.museo_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "productos")
public class ProductoModel {

    @Id
    @NotBlank
    @Size(max = 20)
    @Column(length = 20)
    private String id;

    @NotBlank
    private String titulo;

    @Column(length = 500)
    private String descripcion;

    @PositiveOrZero
    private double precio;

    @Column(length = 300)
    private String imagenUrl;

    @NotBlank
    private String categoria;   // Curso, Libro o Material

    @Min(0)
    private int stock;          // 999 = curso digital (sin límite)

    public ProductoModel() {}

    public ProductoModel(String id, String titulo, String descripcion, double precio,
                         String imagenUrl, String categoria, int stock) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.precio = precio;
        this.imagenUrl = imagenUrl;
        this.categoria = categoria;
        this.stock = stock;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion =