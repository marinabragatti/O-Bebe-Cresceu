package com.marinabragatti.obebecresceu.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Fade;
import android.transition.Transition;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.marinabragatti.obebecresceu.R;
import com.marinabragatti.obebecresceu.api.ViaCepService;
import com.marinabragatti.obebecresceu.helper.ConfiguracaoFirebase;
import com.marinabragatti.obebecresceu.model.Cep;
import com.marinabragatti.obebecresceu.model.Usuario;
import com.santalu.maskedittext.MaskEditText;

import org.json.JSONObject;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ConfigUserActivity extends AppCompatActivity {

    private EditText campoNome, campoCep;
    private MaskEditText campoTelefone;
    private TextView campoLogradouro, campoBairro, campoCidade, campoEstado;
    private Retrofit retrofit;
    private DatabaseReference firebaseRef;
    private String idUserLogado;
    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_user);

        //Configuração Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Minha Conta");
        setSupportActionBar(toolbar);

        inicializarComponentes();
        firebaseRef = ConfiguracaoFirebase.getFirebaseRef();
        idUserLogado = ConfiguracaoFirebase.getIdUser();

        recuperarDadosUser();

        campoCep.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String zipCode = s.toString();
                if(s.length() == 9){
                    retrofit = new Retrofit.Builder()
                            .baseUrl("https://viacep.com.br/ws/")
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    recuperarEnderecoCep(zipCode);
                }
            }
        });
    }

    private void recuperarDadosUser(){
        DatabaseReference userRef = firebaseRef.child("usuarios").child(idUserLogado);//Recupera referência do user logado
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    Usuario usuario = dataSnapshot.getValue(Usuario.class);//Recupero o objeto user salvo no firebase
                    campoNome.setText(usuario.getNome());
                    if(usuario.getTelefone() != null){
                        campoTelefone.setText(usuario.getTelefone());
                    }
                    if(usuario.getEndereco() != null){
                        campoCep.setText(usuario.getEndereco().getCep());
                        campoLogradouro.setText(usuario.getEndereco().getLogradouro());
                        campoBairro.setText(usuario.getEndereco().getBairro());
                        campoCidade.setText(usuario.getEndereco().getLocalidade());
                        campoEstado.setText(usuario.getEndereco().getUf());
                    }
                    if(usuario.getTelefone() != null && usuario.getEndereco().getLocalidade() != null)
                        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    else
                        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void recuperarEnderecoCep(String cep) {

        ViaCepService cepService = retrofit.create(ViaCepService.class);
        Call<Cep> call = cepService.recuperarCep(cep);

        call.enqueue(new Callback<Cep>() { //criado tarefa assíncrona
            @Override
            public void onResponse(Call<Cep> call, Response<Cep> response) {
                if (response.isSuccessful()) {
                    Cep cep = response.body();

                    if(cep.getLogradouro() == null) {
                        Toast.makeText(ConfigUserActivity.this, "Insira um CEP válido!", Toast.LENGTH_LONG).show();
                        layout.setVisibility(View.GONE);
                    }
                    else {
                        layout.setVisibility(View.VISIBLE);
                        campoLogradouro.setText(cep.getLogradouro());
                        campoBairro.setText(cep.getBairro());
                        campoCidade.setText(cep.getLocalidade());
                        campoEstado.setText(cep.getUf());
                    }
                } else {
                    Toast.makeText(ConfigUserActivity.this, "Erro: " + response.message(), Toast.LENGTH_LONG).show();
                    layout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<Cep> call, Throwable t) {
                Toast.makeText(ConfigUserActivity.this, "Erro: " + t.getMessage(), Toast.LENGTH_LONG).show();
                layout.setVisibility(View.GONE);
            }
        });
    }

    public void salvarDadosUser(View view){
        String nome = campoNome.getText().toString();
        String logradouro = campoLogradouro.getText().toString();
        String bairro = campoBairro.getText().toString();
        String cidade = campoCidade.getText().toString();
        String estado = campoEstado.getText().toString();
        String cep = campoCep.getText().toString();
        String telefone = campoTelefone.getRawText();

        if(!nome.isEmpty()) {
            if (validarDados()) {
                if (layout.getVisibility() != View.GONE) {
                    Usuario usuario = new Usuario();
                    Cep endereco = new Cep();
                    usuario.setNome(nome);
                    usuario.setTelefone(telefone);
                    usuario.setEmail(usuario.getEmail());
                    endereco.setLogradouro(logradouro);
                    endereco.setBairro(bairro);
                    endereco.setLocalidade(cidade);
                    endereco.setUf(estado);
                    endereco.setCep(cep);
                    usuario.setEndereco(endereco);
                    usuario.salvar();
                    Toast.makeText(this, "Dados salvos com sucesso!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    finish();
                }else {
                    Toast.makeText(this, "Insira um CEP válido", Toast.LENGTH_SHORT).show();
                }
            }
        }else {
            Toast.makeText(this, "Digite seu nome", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validarDados() {
        if (campoTelefone.getText().length() == 14) {
            if (campoCep.getText().length() == 9){
                return true;
            }else {
                Toast.makeText(this, "CEP inválido", Toast.LENGTH_SHORT).show();
                return false;
            }
        }else {
            Toast.makeText(this, "Telefone inválido", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void inicializarComponentes(){
        campoNome = findViewById(R.id.nomeUser);
        campoCep = findViewById(R.id.cepUser);
        campoTelefone = findViewById(R.id.telUser);
        campoLogradouro = findViewById(R.id.textLogradouro);
        campoBairro = findViewById(R.id.textBairro);
        campoCidade = findViewById(R.id.textCidade);
        campoEstado = findViewById(R.id.textUf);
        layout = findViewById(R.id.layout);
    }
}
