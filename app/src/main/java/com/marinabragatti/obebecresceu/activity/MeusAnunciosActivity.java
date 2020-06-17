package com.marinabragatti.obebecresceu.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.marinabragatti.obebecresceu.R;
import com.marinabragatti.obebecresceu.adapter.AnuncioAdapter;
import com.marinabragatti.obebecresceu.helper.ConfiguracaoFirebase;
import com.marinabragatti.obebecresceu.helper.Permissoes;
import com.marinabragatti.obebecresceu.model.Anuncio;
import com.marinabragatti.obebecresceu.model.Usuario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MeusAnunciosActivity extends AppCompatActivity {

    private String[] permissoes = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private DatabaseReference firebaseRef;
    private String idUserLogado;
    private RecyclerView recyclerView;
    private List<Anuncio> anuncios = new ArrayList<>();
    private AnuncioAdapter anuncioAdapter;
    private DatabaseReference anunciosUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meus_anuncios);

        //Configuração Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Meus Anúncios");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseRef = ConfiguracaoFirebase.getFirebaseRef();
        idUserLogado = ConfiguracaoFirebase.getIdUser();
        anunciosUserRef = ConfiguracaoFirebase.getFirebaseRef()
                .child("meus_anuncios")
                .child(idUserLogado);
        recyclerView = findViewById(R.id.recyclerAnuncios);

        configurarRecyclerView();

        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(v -> {
            DatabaseReference userRef = firebaseRef.child("usuarios").child(idUserLogado);
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        Usuario usuario = dataSnapshot.getValue(Usuario.class);//Recupero o objeto user salvo no firebase
                        if (usuario.getEndereco() != null && usuario.getTelefone() != null)
                            startActivity(new Intent(getApplicationContext(), AddAnuncioActivity.class));
                        else
                            alertaValidacaoEndereco();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        });

        //Validar permissoes
        Permissoes.validarPermissoes(permissoes, this, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int permissaoResultado : grantResults){
            if(permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermissao();
            }
        }
    }

    private void alertaValidacaoPermissao(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", (dialog, which) -> finish());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void alertaValidacaoEndereco(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Dados incompletos");
        builder.setMessage("Para cadastrar um novo anúncio é necessário completar os seus dados");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", (dialog, which) -> startActivity(new Intent(getApplicationContext(), ConfigUserActivity.class)));
        builder.setNegativeButton("Cancelar", (dialog, which) -> finish());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void configurarRecyclerView(){
        //Configurar RecyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this); //Tipo de layout que eu quero
        recyclerView.setLayoutManager(layoutManager); //set do layout escolhido acima
        recyclerView.setHasFixedSize(true); //set de layout fixo por recomendação do Google
        anuncioAdapter = new AnuncioAdapter(anuncios, this);
        recyclerView.setAdapter(anuncioAdapter);
        recuperaAnuncios();
    }

    private void recuperaAnuncios(){
        anunciosUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                anuncios.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    anuncios.add(snapshot.getValue(Anuncio.class));
                }
                Collections.reverse(anuncios);
                anuncioAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
