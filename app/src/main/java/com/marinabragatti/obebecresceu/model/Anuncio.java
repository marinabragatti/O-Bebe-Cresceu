package com.marinabragatti.obebecresceu.model;

import android.provider.ContactsContract;

import com.google.firebase.database.DatabaseReference;
import com.marinabragatti.obebecresceu.helper.ConfiguracaoFirebase;

import java.util.List;

public class Anuncio {

    private String idAnuncio;
    private String titulo;
    private String descricao;
    private String tamanho;
    private String genero;
    private String tipo;
    private String valor;
    private String telefone;
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

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
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

    public List<String> getFotos() {
        return fotos;
    }

    public void setFotos(List<String> fotos) {
        this.fotos = fotos;
    }
}
