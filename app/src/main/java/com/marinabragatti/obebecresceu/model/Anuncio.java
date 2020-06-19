package com.marinabragatti.obebecresceu.model;

import com.google.firebase.database.DatabaseReference;
import com.marinabragatti.obebecresceu.helper.ConfiguracaoFirebase;

import java.io.Serializable;
import java.util.List;

public class Anuncio implements Serializable {

    private String idAnuncio;
    private String titulo;
    private String descricao;
    private String tamanho;
    private String tipo;
    private String valor;
    private String telefone;
    private String cidade;
    private String estado;
    private String email;
    private List<String> fotos;

    public Anuncio() {
        DatabaseReference anuncioRef = ConfiguracaoFirebase.getFirebaseRef()
                .child("meus_anuncios");
        setIdAnuncio(anuncioRef.push().getKey());
    }

    public void salvar(){
        DatabaseReference anuncioRef = ConfiguracaoFirebase.getFirebaseRef()
                .child("meus_anuncios");
        anuncioRef.child(ConfiguracaoFirebase.getIdUser())
                .child(getIdAnuncio())
                .setValue(this);
        salvarAnuncioTodos();
    }

    public void salvarAnuncioTodos(){
        DatabaseReference anuncioRef = ConfiguracaoFirebase.getFirebaseRef()
                .child("anuncios");
        anuncioRef.child(getTamanho())
                .child(getTipo())
                .child(getIdAnuncio())
                .setValue(this);
    }

    public String getIdAnuncio() {
        return idAnuncio;
    }

    public void setIdAnuncio(String idAnuncio) {
        this.idAnuncio = idAnuncio;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTamanho() {
        return tamanho;
    }

    public void setTamanho(String tamanho) {
        this.tamanho = tamanho;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getFotos() {
        return fotos;
    }

    public void setFotos(List<String> fotos) {
        this.fotos = fotos;
    }
}
