package com.marinabragatti.obebecresceu.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.marinabragatti.obebecresceu.R;
import com.marinabragatti.obebecresceu.adapter.AnuncioAdapter;
import com.marinabragatti.obebecresceu.adapter.AnuncioGeralAdapter;
import com.marinabragatti.obebecresceu.helper.ConfiguracaoFirebase;
import com.marinabragatti.obebecresceu.helper.Permissoes;
import com.marinabragatti.obebecresceu.model.Anuncio;
import com.marinabragatti.obebecresceu.model.Usuario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dmax.dialog.SpotsDialog;

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
    private Anuncio anuncio;
    private android.app.AlertDialog loadingDialog;
    private TextView semAnuncio;

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
        semAnuncio = findViewById(R.id.semAnuncio);

        configurarRecyclerView();
        swipe();

        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), AddAnuncioActivity.class));
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

    private void configurarRecyclerView(){
        //Configurar RecyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this); //Tipo de layout que eu quero
        recyclerView.setLayoutManager(layoutManager); //set do layout escolhido acima
        recyclerView.setHasFixedSize(true); //set de layout fixo por recomendação do Google
        anuncioAdapter = new AnuncioAdapter(anuncios, this);
        recyclerView.setAdapter(anuncioAdapter);
        recuperaAnuncios();
    }

    private void loadCarregando(){
        loadingDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Carregando")
                .setCancelable(false)
                .build();
        loadingDialog.show();
    }

    private void recuperaAnuncios(){
        loadCarregando();
        anunciosUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                anuncios.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    anuncios.add(snapshot.getValue(Anuncio.class));
                }
                if(anuncios.isEmpty())
                    semAnuncio.setVisibility(View.VISIBLE);
                else
                    semAnuncio.setVisibility(View.GONE);
                Collections.reverse(anuncios);
                anuncioAdapter.notifyDataSetChanged();
                loadingDialog.dismiss();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void swipe(){
        ItemTouchHelper.Callback itCallback = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                        @NonNull RecyclerView.ViewHolder viewHolder) {
                int draFlags = ItemTouchHelper.ACTION_STATE_IDLE; //Deixar eventos drag and drop inativos
                int swipeFlags = ItemTouchHelper.START;
                return makeMovementFlags(draFlags, swipeFlags);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                excluirServico(viewHolder); //ViewHolder recupera a posição do item da lista
            }
        };

        new ItemTouchHelper(itCallback).attachToRecyclerView(recyclerView); //Adiciona o swipe no recycler view
    }

    public void excluirServico(final RecyclerView.ViewHolder viewHolder){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        //Configura AlertDialog
        alertDialog.setTitle("Excluir Anúncio");
        alertDialog.setMessage("Você tem certeza que deseja excluir este anúncio?");
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int position = viewHolder.getAdapterPosition(); //Recupera a posição do item deslisado
                anuncio = anuncios.get(position); //Recupera o objeto anúncio

                 anunciosUserRef = firebaseRef.child("meus_anuncios")
                        .child(idUserLogado);//Acesso aos anúncios daquele usuario no firebase

                anunciosUserRef.child(anuncio.getIdAnuncio()).removeValue(); //Remove item da lista no firebase
                anuncioAdapter.notifyItemRemoved(position);//Atualiza lista sem item removido

                anunciosUserRef = firebaseRef.child("anuncios")
                        .child(anuncio.getTamanho())
                        .child(anuncio.getTipo());
                anunciosUserRef.child(anuncio.getIdAnuncio()).removeValue();
            }
        });
        alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                anuncioAdapter.notifyDataSetChanged(); //Manter o item na lista após cancelar a caixa de dialog
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.show();
    }
}
