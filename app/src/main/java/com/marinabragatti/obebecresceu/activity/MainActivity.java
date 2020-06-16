package com.marinabragatti.obebecresceu.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.marinabragatti.obebecresceu.R;
import com.marinabragatti.obebecresceu.helper.ConfiguracaoFirebase;
import com.marinabragatti.obebecresceu.model.Usuario;

public class MainActivity extends AppCompatActivity {

    private EditText campoNome, campoEmail, campoSenha;
    private TextView tenhoConta;
    private Button buttonCadastrar;
    private FirebaseAuth autenticacao;
    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        autenticacao = ConfiguracaoFirebase.getFirebaseAuth();

        inicializarComponentes();

        buttonCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Recuperar dados preenchidos
                String textoNome = campoNome.getText().toString();
                String textoEmail = campoEmail.getText().toString();
                String textoSenha = campoSenha.getText().toString();

                //Validar se os campos foram preenchidos
                if(!textoNome.isEmpty()){
                    if (!textoEmail.isEmpty()) {
                        if(!textoSenha.isEmpty()){

                            usuario = new Usuario();
                            usuario.setNome(textoNome);
                            usuario.setEmail(textoEmail);
                            usuario.setSenha(textoSenha);
                            cadastrarUsuario();

                        }else{
                            Toast.makeText(MainActivity.this, "Preencha a senha!", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(MainActivity.this, "Preencha o email!", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(MainActivity.this, "Preencha o nome!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tenhoConta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });

    }
    public void cadastrarUsuario(){
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){ //Verifica se o cadastro deu certo
                    usuario.salvar();
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                }else{
                    String exception = "";
                    try{
                        throw task.getException();
                    }catch (FirebaseAuthWeakPasswordException e){
                        exception = "Digite uma senha mais forte!";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        exception = "Digite um email válido!";
                    }catch (FirebaseAuthUserCollisionException e){
                        exception = "Usuário já cadastrado!";
                    }catch(Exception e){
                        exception = "Erro ao cadastrar usuário: " + e.getMessage();
                        e.printStackTrace(); //Mostra o erro exato
                    }
                    Toast.makeText(MainActivity.this, exception, Toast.LENGTH_SHORT).show();
                }
                //Exceções do FirebaseAuth em
                //https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseAuth?authuser=0#createUserWithEmailAndPassword(java.lang.String,%20java.lang.String)
            }
        });
    }

    // OnStart é chamado quando a Activity de Cadastro é fechada e voltamos para a MainActivity(que já executou o OnCreate anteriormente.
    // O método verificarUsuarioLogado é chamado, então o método abrirTelaPrincipal é chamado, que finalmente dá start em uma nova activity, PrincipalActivity.
    @Override
    protected void onStart() {
        super.onStart();
        verificarUsuarioLogado();
    }

    public void verificarUsuarioLogado(){
        //autenticacao.signOut();
        //Verifica se o usuário está logado
        if(autenticacao.getCurrentUser() != null){
            abrirTelaPrincipal();
        }
    }

    public void abrirTelaPrincipal(){
        startActivity(new Intent(this, HomeActivity.class));
    }

    public void inicializarComponentes(){
        campoNome = findViewById(R.id.name);
        campoEmail = findViewById(R.id.email);
        campoSenha = findViewById(R.id.password);
        buttonCadastrar = findViewById(R.id.buttonCadastre);
        tenhoConta = findViewById(R.id.buttonTenhoConta);
    }

}
