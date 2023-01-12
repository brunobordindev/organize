package com.example.apporganize.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apporganize.R;
import com.example.apporganize.activity.PrincipalActivity;
import com.example.apporganize.model.Movimentacao;
import com.google.firebase.database.core.Context;

import java.util.List;

public class AdapterMovimentacao extends RecyclerView.Adapter<AdapterMovimentacao.MyViewHolder> {

    List<Movimentacao> movimentacoes;
    PrincipalActivity context;

    public AdapterMovimentacao(List<Movimentacao> movimentacoes, PrincipalActivity context) {
        this.movimentacoes = movimentacoes;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_movimentacao, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Movimentacao movimentacao = movimentacoes.get(position);

        holder.textCategoria.setText(movimentacao.getCategoria());
        holder.textValor.setText(String.valueOf(movimentacao.getValor()));
        holder.textValor.setTextColor(context.getResources().getColor(R.color.receitaAccent));
        holder.textDescricao.setText(movimentacao.getDescricao());

        if (movimentacao.getTipo() == "d" || movimentacao.getTipo().equals("d")){
            holder.textValor.setText(" - " + movimentacao.getValor());
            holder.textValor.setTextColor(context.getResources().getColor(R.color.despesaAccent));

        }

    }

    @Override
    public int getItemCount() {
        return movimentacoes.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{


        private TextView textCategoria, textDescricao, textValor;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            textCategoria = itemView.findViewById(R.id.text_categoria);
            textDescricao = itemView.findViewById(R.id.text_descricao);
            textValor = itemView.findViewById(R.id.text_valor);
        }
    }
}
