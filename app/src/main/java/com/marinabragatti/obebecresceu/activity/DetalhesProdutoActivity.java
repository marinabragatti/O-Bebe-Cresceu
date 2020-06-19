package com.marinabragatti.obebecresceu.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.marinabragatti.obebecresceu.R;
import com.marinabragatti.obebecresceu.model.Anuncio;
import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import dmax.dialog.SpotsDialog;

public class DetalhesProdutoActivity extends AppCompatActivity {

    private CarouselView carouselView;
    private TextView campoTitulo, campoValor, campoCidade, campoEstado, campoDescricao, campoTamanho;
    private Anuncio anuncioSelecionado;
    private String email;
    private android.app.AlertDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_produto);

        //Configuração Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Anúncio");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        inicializarComponentes();

        recuperarAnuncio();
    }

    private void recuperarAnuncio(){
        //Recupera anúncio para exibição
        anuncioSelecionado = (Anuncio) getIntent().getSerializableExtra("anuncioSelecionado");
        loadCarregando();
        if(anuncioSelecionado != null){
            campoTitulo.setText(anuncioSelecionado.getTitulo());
            if(anuncioSelecionado.getTipo().equals("v"))
                campoValor.setText(anuncioSelecionado.getValor());
            else
                campoValor.setText("Doação");
            campoCidade.setText(anuncioSelecionado.getCidade());
            campoEstado.setText(anuncioSelecionado.getEstado());
            campoDescricao.setText(anuncioSelecionado.getDescricao());
            campoTamanho.setText(anuncioSelecionado.getTamanho());

            ImageListener imageListener = new ImageListener() {
                @Override
                public void setImageForPosition(int position, ImageView imageView) {
                    String urlString = anuncioSelecionado.getFotos().get(position);
                    Picasso.get().load(urlString).into(imageView);
                }
            };
            carouselView.setPageCount(anuncioSelecionado.getFotos().size());
            carouselView.setImageListener(imageListener);
            loadingDialog.dismiss();
        }
    }

    public void entrarEmContato(View view){
        loadCarregando();
        if(anuncioSelecionado.getTelefone().equals("") || anuncioSelecionado == null){
            email = anuncioSelecionado.getEmail();
            String [] recebendoEmail = email.split(",");
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_EMAIL, recebendoEmail);
            intent.putExtra(Intent.EXTRA_SUBJECT, "Interesse no anúncio - O Bebê Cresceu");
            intent.putExtra(Intent.EXTRA_TEXT, "Olá, \n\nVi um anúncio seu no app O Bebê Cresceu que me interessou, podemos conversar?");

            intent.setType("message/rfc822");
            loadingDialog.dismiss();
            startActivity(intent);
        }else{
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/0550" + anuncioSelecionado.getTelefone()));
            loadingDialog.dismiss();
            startActivity(intent);
        }
    }

    private void loadCarregando(){
        loadingDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Carregando")
                .setCancelable(false)
                .build();
        loadingDialog.show();
    }

    private void inicializarComponentes(){
        carouselView = findViewById(R.id.carouselView);
        campoTitulo = findViewById(R.id.detalheNome);
        campoValor = findViewById(R.id.detalheValor);
        campoCidade = findViewById(R.id.detalheCidade);
        campoEstado = findViewById(R.id.detalheEstado);
        campoDescricao = findViewById(R.id.detalheDescricao);
        campoTamanho = findViewById(R.id.detalheTamanho);
    }
}
