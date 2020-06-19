package com.marinabragatti.obebecresceu.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.marinabragatti.obebecresceu.R;
import com.marinabragatti.obebecresceu.adapter.AnuncioGeralAdapter;
import com.marinabragatti.obebecresceu.helper.ConfiguracaoFirebase;
import com.marinabragatti.obebecresceu.listener.RecyclerItemClickListener;
import com.marinabragatti.obebecresceu.model.Anuncio;
import com.marinabragatti.obebecresceu.model.Usuario;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private MaterialSearchView searchView;
    private Button buttonLimpar;
    private TextView semAnuncio;
    private RecyclerView recyclerView;
    private AnuncioGeralAdapter anuncioAdapter;
    private List<Anuncio> anuncios = new ArrayList<>();
    private android.app.AlertDialog loadingDialog;
    private DatabaseReference anunciosUserRef;
    private String filtroTamanho = "";
    private String filtroTipo = "";
    private boolean filtrandoTamanho = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        autenticacao = ConfiguracaoFirebase.getFirebaseAuth();
        inicializarComponenetes();

        //Configuração Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("O Bebê Cresceu!");
        setSupportActionBar(toolbar);

        configurarRecyclerView();

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                String textoDigitado = newText.toLowerCase();
                pesquisarAnunciosTituloCidade(textoDigitado);
                return true;
            }
        });
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
            }
            @Override
            public void onSearchViewClosed() {
                recuperaAnuncios();
            }
        });

        buttonLimpar.setOnClickListener(v -> recuperaAnuncios());

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(
                        this, recyclerView,
                        new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Anuncio anuncioSelecionado = anuncios.get(position);
                        Intent intent = new Intent(HomeActivity.this, DetalhesProdutoActivity.class);
                        intent.putExtra("anuncioSelecionado", anuncioSelecionado);
                        startActivity(intent);
                    }
                    @Override
                    public void onLongItemClick(View view, int position) {
                    }
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    }
                }
                )
        );
    }

    private void pesquisarAnunciosTituloCidade(String textoDigitado){
        anuncios.clear();
        if(textoDigitado.length()>2){
            anunciosUserRef = ConfiguracaoFirebase.getFirebaseRef()
                    .child("anuncios");
            Query query = anunciosUserRef.orderByChild("titulo");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    anuncios.clear();
                    List<Anuncio> listaAuxiliar = new ArrayList<>();
                    for(DataSnapshot tamanho : dataSnapshot.getChildren()){
                        for(DataSnapshot tipo : tamanho.getChildren()){
                            for(DataSnapshot anunciosTodos : tipo.getChildren()){
                                listaAuxiliar.add(anunciosTodos.getValue(Anuncio.class));
                            }
                        }
                    }
                    if (!listaAuxiliar.isEmpty()) {
                        for (Anuncio u : listaAuxiliar) {
                            if (u.getTitulo().toLowerCase().contains(textoDigitado.toLowerCase()) || u.getCidade().toLowerCase().contains(textoDigitado.toLowerCase())) {
                                anuncios.add(u);
                            }
                        }
                    }
                    if(anuncios.isEmpty())
                        semAnuncio.setVisibility(View.VISIBLE);
                    else
                        semAnuncio.setVisibility(View.GONE);
                    anuncioAdapter.notifyDataSetChanged();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }

    public void filtrarTamamho(View view){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        //Configura AlertDialog
        alertDialog.setTitle("Selecione o tamanho desejado");
        View spinner = getLayoutInflater().inflate(R.layout.dialog_spinner, null);
        Spinner spinnerTamanho = spinner.findViewById(R.id.spinnerFiltro);
        alertDialog.setView(spinner);
        String [] tamanhos = new String[]{
                "RN (recém nascido)",
                "P (0 a 3 meses)",
                "M (4 a 6 meses)",
                "G (7 a 10 meses)",
                "GG (11 a 12 meses)",
                "1 (1 ano)",
                "2 (1 a 2 anos)",
                "3 (2 a 3 anos)",
                "4 (3 a 5 anos)",
                "6 (4 a 5 anos)"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item,
                tamanhos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTamanho.setAdapter(adapter);
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton("OK", (dialog, which) -> {
            filtroTamanho = spinnerTamanho.getSelectedItem().toString();
            recuperarAnunciosTamanhos();
            filtrandoTamanho = true;
        });
                alertDialog.setNegativeButton("Cancelar", (dialog, which) -> {
                });
        AlertDialog dialog = alertDialog.create();
        dialog.show();
    }

    public void filtrarTipo(View view){
        if(filtrandoTamanho == true){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            //Configura AlertDialog
            alertDialog.setTitle("Selecione o tipo desejado");
            View spinner = getLayoutInflater().inflate(R.layout.dialog_spinner, null);
            Spinner spinnerTipo = spinner.findViewById(R.id.spinnerFiltro);
            alertDialog.setView(spinner);
            String [] tamanhos = new String[]{
                    "Venda",
                    "Doação"
            };
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this, android.R.layout.simple_spinner_item,
                    tamanhos);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerTipo.setAdapter(adapter);
            alertDialog.setCancelable(false);
            alertDialog.setPositiveButton("OK", (dialog, which) -> {
                if(spinnerTipo.getSelectedItem().toString().equals("Venda"))
                    filtroTipo = "v";
                else
                    filtroTipo = "d";
                recuperarAnunciosTipo();
            });
            alertDialog.setNegativeButton("Cancelar", (dialog, which) -> {
            });
            AlertDialog dialog = alertDialog.create();
            dialog.show();
        }else{
            Toast.makeText(this, "Selecione um tamanho primeiro!", Toast.LENGTH_SHORT).show();
        }
    }

    private void recuperarAnunciosTamanhos(){
        loadCarregando();
        anunciosUserRef = ConfiguracaoFirebase.getFirebaseRef()
                .child("anuncios")
                .child(filtroTamanho);
        anunciosUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                anuncios.clear();
                for(DataSnapshot tipo : dataSnapshot.getChildren()){
                    for(DataSnapshot anunciosTodos : tipo.getChildren()){
                        anuncios.add(anunciosTodos.getValue(Anuncio.class));
                    }
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

    private void recuperarAnunciosTipo(){
        loadCarregando();
        anunciosUserRef = ConfiguracaoFirebase.getFirebaseRef()
                .child("anuncios")
                .child(filtroTamanho)
                .child(filtroTipo);
        anunciosUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                anuncios.clear();
                for(DataSnapshot anunciosTodos : dataSnapshot.getChildren()){
                    anuncios.add(anunciosTodos.getValue(Anuncio.class));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        //Configuração Menu Pesquisa
        MenuItem item = menu.findItem(R.id.menuPesquisar);
        searchView.setMenuItem(item);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.menuSair :
                deslogarUser();
                break;
            case R.id.menuConfig :
                abrirConfig();
                break;
            case R.id.menuInserirAnuncio :
                abrirAddAnuncio();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void configurarRecyclerView(){
        //Configurar RecyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this); //Tipo de layout que eu quero
        recyclerView.setLayoutManager(layoutManager); //set do layout escolhido acima
        recyclerView.setHasFixedSize(true); //set de layout fixo por recomendação do Google
        anuncioAdapter = new AnuncioGeralAdapter(anuncios, this);
        recyclerView.setAdapter(anuncioAdapter);
        recuperaAnuncios();
    }

    private void recuperaAnuncios() {
        //Mostra todos os anúncios
        filtrandoTamanho = false;
        loadCarregando();
        anunciosUserRef = ConfiguracaoFirebase.getFirebaseRef()
                .child("anuncios");
        anunciosUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                anuncios.clear();
                for(DataSnapshot tamanho : dataSnapshot.getChildren()){
                    for(DataSnapshot tipo : tamanho.getChildren()){
                        for(DataSnapshot anunciosTodos : tipo.getChildren()){
                            anuncios.add(anunciosTodos.getValue(Anuncio.class));
                        }
                    }
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

    private void loadCarregando(){
        loadingDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Carregando")
                .setCancelable(false)
                .build();
        loadingDialog.show();
    }

    private void inicializarComponenetes(){
        searchView = findViewById(R.id.materialSearch);
        recyclerView = findViewById(R.id.recyclerAnunciosTodos);
        buttonLimpar = findViewById(R.id.buttonLimpar);
        semAnuncio = findViewById(R.id.semAnuncioHome);
    }

    private void deslogarUser(){
        try {
            autenticacao.signOut();
            finish();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void abrirConfig(){
        startActivity(new Intent(HomeActivity.this, ConfigUserActivity.class));
    }

    private void abrirAddAnuncio(){
        startActivity(new Intent(HomeActivity.this, MeusAnunciosActivity.class));
    }
}
