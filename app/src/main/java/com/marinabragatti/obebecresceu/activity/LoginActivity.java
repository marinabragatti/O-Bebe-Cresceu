package com.marinabragatti.obebecresceu.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.marinabragatti.obebecresceu.R;
import com.marinabragatti.obebecresceu.helper.ConfiguracaoFirebase;
import com.marinabragatti.obebecresceu.model.Usuario;

public class LoginActivity extends AppCompatActivity {

    private EditText campoEmail, campoSenha;
    private Button buttonEntrar;
    private FirebaseAuth autenticacao;
    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inicializarComponenetes();

        //Configuração Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Login");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        buttonEntrar.setOnClickListener(v -> {
            //Recuperar dados preenchidos
            String textoEmail = campoEmail.getText().toString();
            String textoSenha = campoSenha.getText().toString();

            //Validar se os campos foram preenchidos
            if (!textoEmail.isEmpty()) {
                if(!textoSenha.isEmpty()){
                    usuario = new Usuario();
                    usuario.setEmail(textoEmail);
                    usuario.setSenha(textoSenha);
                    validarLogin();

                }else{
                    Toast.makeText(LoginActivity.this, "Preencha a senha!", Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(LoginActivity.this, "Preencha o email!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validarLogin(){
        autenticacao = ConfiguracaoFirebase.getFirebaseAuth();
        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    abrirTelaPrincipal();
                }else{
                    String exception = "";
                    try {
                        throw task.getException();
                    }catch (FirebaseAuthInvalidUserException e){
                        exception = "Email inválido!";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        exception = "Senha inválida!";
                    }catch (Exception e){
                        exception = "Erro ao fazer login: " + e.getMessage();
                        e.printStackTrace(); //Mostra o erro exato
                    }
                    Toast.makeText(LoginActivity.this, exception, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void abrirTelaPrincipal(){
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private void inicializarComponenetes(){
        campoEmail = findViewById(R.id.email);
        campoSenha = findViewById(R.id.password);
        buttonEntrar = findViewById(R.id.buttonAcessar);
    }
}
