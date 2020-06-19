package com.marinabragatti.obebecresceu.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.marinabragatti.obebecresceu.R;
import com.marinabragatti.obebecresceu.helper.ConfiguracaoFirebase;
import com.marinabragatti.obebecresceu.model.Anuncio;
import com.marinabragatti.obebecresceu.model.Usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;

public class AddAnuncioActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView imagemUm, imagemDois, imagemTres;
    private EditText campoTitulo, campoDescricao;
    private CurrencyEditText campoValor;
    private Spinner campoTamanho;
    private RadioButton campoVenda, campoDoacao;
    private RadioGroup opcaoVendaDoacao;
    private CheckBox campoTelefone;
    private LinearLayout layoutPagto;
    private List<String> listaFotosRecuperada = new ArrayList<>(); //Caminho das fotos dentro do aparelho do usuário
    private List<String> listaUrlFotos = new ArrayList<>(); //Caminho das fotos já salvas no firebase
    private Anuncio anuncio;
    private StorageReference firebaseStorage;
    private DatabaseReference userRef;
    private String tipoAnuncio;
    private String telefone = "";
    private String cidade = "";
    private String estado = "";
    private AlertDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_anuncio);

        //Configuração Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Novo Anúncio");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        inicializarComponentes();
        carregarDadosSpinner();
        firebaseStorage = ConfiguracaoFirebase.getStorageReference();

        //Configura localidade para pt/BR
        Locale locale = new Locale("pt", "BR");
        campoValor.setLocale(locale);

        opcaoDeVendaOuDoacao();
        recuperarTelefone();
        recuperarEstadoCidade();
    }

    //Método da implementação do View.OnClickListener para ouvir eventos de clique
    @Override
    public void onClick(View v) {
        switch (v.getId()) { //saber qual imagem foi selecionada
            case R.id.imageView1:
                escolherImagem(1);
                break;
            case R.id.imageView2:
                escolherImagem(2);
                break;
            case R.id.imageView3:
                escolherImagem(3);
                break;
        }
    }

    private void escolherImagem(int requestCode){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, requestCode); //identifico a imagem selecionada
    }

    //Recuperar a imagem escolhida
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            //Recuperar imagem
            Uri imagemEscolhida = data.getData();
            String caminhoImagem = imagemEscolhida.toString();

            //Configura imagem no ImageView
            if(requestCode == 1){
                imagemUm.setImageURI(imagemEscolhida);
            }else if(requestCode == 2){
                imagemDois.setImageURI(imagemEscolhida);
            }else if(requestCode == 3){
                imagemTres.setImageURI(imagemEscolhida);
            }
            listaFotosRecuperada.add(caminhoImagem);
        }
    }

    private void opcaoDeVendaOuDoacao(){
        opcaoVendaDoacao.setOnCheckedChangeListener((group, checkedId) -> {
            if(checkedId == R.id.venda){
                layoutPagto.setVisibility(View.VISIBLE);
                tipoAnuncio = "v";
            }
            else{
                layoutPagto.setVisibility(View.GONE);
                tipoAnuncio = "d";
            }
        });
    }

    private void carregarDadosSpinner(){
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
        campoTamanho.setAdapter(adapter);
    }

    public void salvarAnuncio(View view){
        String titulo = campoTitulo.getText().toString();
        String descricao = campoDescricao.getText().toString();
        String tamanho = campoTamanho.getSelectedItem().toString();
        String valor = campoValor.getText().toString();

        if(validarCampos()){
            anuncio = new Anuncio();
            anuncio.setTitulo(titulo);
            anuncio.setDescricao(descricao);
            anuncio.setTamanho(tamanho);
            anuncio.setTipo(tipoAnuncio);
            anuncio.setValor(valor);
            anuncio.setTelefone(telefone);
            anuncio.setCidade(cidade);
            anuncio.setEstado(estado);
            //Salvar imagem no Storage
            int tamanhoLista = listaFotosRecuperada.size();
            for(int i = 0; i < listaFotosRecuperada.size(); i++){
                String urlImagem = listaFotosRecuperada.get(i);
                salvarFotosStorage(urlImagem, tamanhoLista, i);
            }
            loadingDialog = new SpotsDialog.Builder()
                    .setContext(this)
                    .setMessage("Salvando anúncio")
                    .setCancelable(false)
                    .build();
            loadingDialog.show();
        }
    }

    private void salvarFotosStorage(String urlString, int tamanhoLista, int contador){
        StorageReference imagemAnuncio = firebaseStorage.child("imagens")
                .child("anuncios")
                .child(anuncio.getIdAnuncio())
                .child("imagem"+contador);

        //Fazer upload do arquivo
        UploadTask uploadTask = imagemAnuncio.putFile(Uri.parse(urlString));
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            Task<Uri> firebaseUrl = taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(task -> {
                String urlConvertida = task.getResult().toString();
                listaUrlFotos.add(urlConvertida);
                if(tamanhoLista == listaUrlFotos.size()){
                    anuncio.setFotos(listaUrlFotos);
                    anuncio.salvar();
                    loadingDialog.dismiss();
                    exibirToast("Anúncio salvo com sucesso!");
                    finish();
                }
            }).addOnFailureListener(e -> exibirToast("Falha ao fazer upload da imagem"));
        });
    }

    private boolean validarCampos() {
        //Valida se os campos foram preenchidos
        String titulo = campoTitulo.getText().toString();
        String descricao = campoDescricao.getText().toString();
        String valor = campoValor.getText().toString();

        if (listaFotosRecuperada.size() != 0) {
            if (!titulo.isEmpty()) {
                if (!descricao.isEmpty()) {
                    if (campoVenda.isChecked() || campoDoacao.isChecked()) {
                        if ((campoVenda.isChecked() && !valor.isEmpty() && !valor.equals("0")) || campoDoacao.isChecked())
                            return true;
                        else {
                            exibirToast("Preencha o valor");
                            return false;
                        }
                    } else {
                        exibirToast("Informe se é venda ou doação");
                        return false;
                    }
                } else {
                    exibirToast("Preencha a descrição");
                    return false;
                }
            } else {
                exibirToast("Preencha o título");
                return false;
            }
        }else{
            exibirToast("Insira ao menos uma foto");
            return false;
        }
    }

    private void recuperarTelefone(){
        campoTelefone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    userRef = ConfiguracaoFirebase.recuperarUsuario();
                    userRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Usuario usuario = dataSnapshot.getValue(Usuario.class);//Recuperar todos os dados do usuario logado
                            telefone = usuario.getTelefone();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }
        });
    }

    private void recuperarEstadoCidade(){
        userRef = ConfiguracaoFirebase.recuperarUsuario();
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);//Recuperar todos os dados do usuario logado
                cidade = usuario.getEndereco().getLocalidade();
                estado = usuario.getEndereco().getUf();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void exibirToast(String texto){
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show();
    }

    private void inicializarComponentes(){
        imagemUm = findViewById(R.id.imageView1);
        imagemDois = findViewById(R.id.imageView2);
        imagemTres = findViewById(R.id.imageView3);
        imagemUm.setOnClickListener(this);
        imagemDois.setOnClickListener(this);
        imagemTres.setOnClickListener(this);
        campoVenda = findViewById(R.id.venda);
        campoDoacao = findViewById(R.id.doacao);
        opcaoVendaDoacao = findViewById(R.id.radioGroup);
        campoTitulo = findViewById(R.id.tituloAnuncio);
        campoDescricao = findViewById(R.id.descricao);
        campoValor = findViewById(R.id.valorAnuncio);
        campoTamanho = findViewById(R.id.spinnerTamanho);
        campoTelefone = findViewById(R.id.checkBox);
        layoutPagto = findViewById(R.id.layoutPagto);
    }

}
