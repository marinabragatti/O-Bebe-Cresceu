package com.marinabragatti.obebecresceu.model;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.marinabragatti.obebecresceu.helper.ConfiguracaoFirebase;

public class Usuario {

    private String idUsuario;
    private String nome;
    private String email;
    private String senha;
    private Cep endereco;

    public Usuario() {

    }

    public void salvar(){
        setIdUsuario(getIdUser());
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseRef();
        DatabaseReference userRef = firebaseRef.child("usuarios")
                .child(getIdUsuario());
        userRef.setValue(this);
    }

    public String getIdUser(){
        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAuth();
        return autenticacao.getCurrentUser().getUid();
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude //Não preciso da senha salva no firebase
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Cep getEndereco() {
        return endereco;
    }

    public void setEndereco(Cep endereco) {
        this.endereco = endereco;
    }
}
