package com.marinabragatti.obebecresceu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.marinabragatti.obebecresceu.R;
import com.marinabragatti.obebecresceu.model.Anuncio;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AnuncioGeralAdapter extends RecyclerView.Adapter<AnuncioGeralAdapter.MyViewHolder>{

    private List<Anuncio> anuncioList;
    private Context context;

    public AnuncioGeralAdapter(List<Anuncio> anuncioList, Context context) {
        this.anuncioList = anuncioList;
        this.context = context;
    }

    @NonNull
    @Override
    public AnuncioGeralAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemList = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.anuncios_gerais_adapter, parent, false);
        return new MyViewHolder(itemList);
    }

    @Override
    public void onBindViewHolder(@NonNull AnuncioGeralAdapter.MyViewHolder holder, int position) {
        Anuncio anuncio = anuncioList.get(position);
        holder.tituloAnuncio.setText(anuncio.getTitulo());
        if(anuncio.getTipo().equals("v"))
            holder.valorAnuncio.setText(anuncio.getValor());
        else
            holder.valorAnuncio.setText("Doação");
        holder.cidadeAnuncio.setText(anuncio.getCidade());

        List<String> urlFotos = anuncio.getFotos();
        String urlCapa = urlFotos.get(0);
        //Utilizo a biblioteca Picasso para o upload da foto cadastrada
        if(!anuncio.getFotos().isEmpty()){
            Picasso.get()
                    .load(urlCapa)
                    .into(holder.imgMeuAnuncio);
        }
    }

    @Override
    public int getItemCount() {
        return anuncioList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        //itens que a view ieá exibir
        TextView tituloAnuncio, valorAnuncio, cidadeAnuncio;
        CircleImageView imgMeuAnuncio;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tituloAnuncio = itemView.findViewById(R.id.textNome);
            valorAnuncio = itemView.findViewById(R.id.textValor);
            cidadeAnuncio = itemView.findViewById(R.id.textCidade);
            imgMeuAnuncio = itemView.findViewById(R.id.imageMeusAnuncios);
        }
    }

}
