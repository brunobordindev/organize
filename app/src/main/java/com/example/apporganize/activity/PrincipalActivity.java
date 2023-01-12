package com.example.apporganize.activity;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.apporganize.R;
import com.example.apporganize.adapter.AdapterMovimentacao;
import com.example.apporganize.config.ConfiguracaoFirebase;
import com.example.apporganize.databinding.ActivityPrincipalBinding;
import com.example.apporganize.databinding.ContentPrincipalBinding;
import com.example.apporganize.helper.Base64Custom;
import com.example.apporganize.model.Movimentacao;
import com.example.apporganize.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PrincipalActivity extends AppCompatActivity {

    private ActivityPrincipalBinding binding;
    private MaterialCalendarView calendarView;
    private RecyclerView recyclerView;
    private AdapterMovimentacao adapterMovimentacao;
    private TextView campoSaudacaoNome, campoValorSaldo;
    private FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private ValueEventListener valueEventListenerUsuario;
    private DatabaseReference usuarioRef;
    private DatabaseReference movimentacaoRef;
    private ValueEventListener valueEventListenerMovimentacao;
    private Double despesaTotal = 0.00;
    private Double receitaTotal = 0.00;
    private Double valorTotalUsuario = 0.00;

    private String mesAnoSelecionado;

    private List<Movimentacao> movimentacoes = new ArrayList<>();
    private Movimentacao movimentacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_principal);

        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle("Organizee");


        instanciando();

        binding.fabDespesa.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), DespesaActivity.class));
        });

        binding.fabReceita.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), ReceitaActivity.class));
        });

        configurarCalendarView();
        swipe();

        //Adapter
        adapterMovimentacao = new AdapterMovimentacao(movimentacoes, this);

        //recyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapterMovimentacao);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.itemSair:
                auth.signOut();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
                Toast.makeText(getApplicationContext(), "Sair", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //Arrasta de um lado pro outro a recyclerView
    public  void swipe(){

        ItemTouchHelper.Callback itemTouch = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {

                int draFlags = ItemTouchHelper.ACTION_STATE_IDLE; //O _IDLE deixa inativo fazer o movimento pra cima, pra baixo de um lado pro outro.
                int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END; // arrasta pra um lado e pro outro, pode deixar so um lado se quiser

                return makeMovementFlags(draFlags, swipeFlags);
            }

            @Override //onMove pra movimentar, pra cima pra baixo, pro lado e pro outro
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override //Arrasta pro lado e pro outro
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    excluirMovimentacao(viewHolder);

            }
        };

        new ItemTouchHelper(itemTouch).attachToRecyclerView(recyclerView);

    }

    public void excluirMovimentacao(RecyclerView.ViewHolder viewHolder){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        //congiguracao alertDialog
        alertDialog.setTitle("Excluir Movimentaçao da Conta");
        alertDialog.setMessage("Você tem certeza que deseja realmente exlcuir essa movimentação de sua conta?");
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int position = viewHolder.getAdapterPosition();
                movimentacao = movimentacoes.get(position);

                String emailUsuario = auth.getCurrentUser().getEmail();
                String idUsuario = Base64Custom.codificarBase64(emailUsuario);

                movimentacaoRef = firebaseRef.child("movimentacao").child(idUsuario).child(mesAnoSelecionado);
                movimentacaoRef.child(movimentacao.getKey()).removeValue();
                adapterMovimentacao.notifyItemRemoved(position);

                atualizarSaldoAposDeletarMovimentacao();

            }
        });

        alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(), "Cancelado", Toast.LENGTH_SHORT).show();
                adapterMovimentacao.notifyDataSetChanged();
            }
        });

        alertDialog.create();
        alertDialog.show();

    }

    public void atualizarSaldoAposDeletarMovimentacao(){

        String emailUsuario = auth.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

        if (movimentacao.getTipo().equals("r")){
            receitaTotal = receitaTotal - movimentacao.getValor();
            usuarioRef.child("receitaTotal").setValue(receitaTotal);
        }

        if (movimentacao.getTipo().equals("d")){
            despesaTotal = despesaTotal - movimentacao.getValor();
            usuarioRef.child("despesaTotal").setValue(despesaTotal);
        }
    }

    public void recuperarMovimentacoes(){

        String emailUsuario = auth.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);

        movimentacaoRef = firebaseRef.child("movimentacao").child(idUsuario).child(mesAnoSelecionado);
        valueEventListenerMovimentacao = movimentacaoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                movimentacoes.clear();

                //Esse for é pra pegar todas as movimentacoes
                for (DataSnapshot dados: snapshot.getChildren()){

                    //pega somente uma movimentacao inteira
                    Movimentacao movimentacao = dados.getValue(Movimentacao.class);
                    movimentacao.setKey(dados.getKey());
                    movimentacoes.add(movimentacao);
                }

                adapterMovimentacao.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void recuperarResumo(){

        String emailUsuario = auth.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

        valueEventListenerUsuario = usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Usuario usuario = snapshot.getValue(Usuario.class);
                campoSaudacaoNome.setText("Olá, " + usuario.getNome());

                despesaTotal = usuario.getDespesaTotal();
                receitaTotal = usuario.getReceitaTotal();
                valorTotalUsuario = receitaTotal - despesaTotal;

                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                String resultadoFormatado = decimalFormat.format(valorTotalUsuario);

                if (valorTotalUsuario <= 0){
                    campoValorSaldo.setText("R$ " + resultadoFormatado );
                    campoValorSaldo.setTextColor(getResources().getColor(R.color.despesaDark));
                }else{
                    campoValorSaldo.setText("R$ " + resultadoFormatado );
                    campoValorSaldo.setTextColor(getResources().getColor(R.color.white));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void configurarCalendarView(){

        CharSequence meses[]= {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};
        calendarView.setTitleMonths(meses);

        CalendarDay dataAtual = calendarView.getCurrentDate();
        String mesSelecionado = String.format("%02d", (dataAtual.getMonth() + 1 ) );
        mesAnoSelecionado = String.valueOf(mesSelecionado + "" + dataAtual.getYear());

        calendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {

                String mesSelecionado = String.format("%02d",(date.getMonth() + 1 ) );
                mesAnoSelecionado = String.valueOf( mesSelecionado + "" + date.getYear());

                movimentacaoRef.removeEventListener(valueEventListenerMovimentacao);
                recuperarMovimentacoes();

                Log.i("mes", "proximo mes: " + mesAnoSelecionado);
            }
        });
    }

    public void instanciando(){
        calendarView = findViewById(R.id.calendarView);
        campoSaudacaoNome = findViewById(R.id.text_saudacao_nome);
        campoValorSaldo = findViewById(R.id.text_valor_saldo);
        recyclerView = findViewById(R.id.recycler_movimentacao);
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarResumo();
        recuperarMovimentacoes();

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("Evento", "evento removido");
        usuarioRef.removeEventListener(valueEventListenerUsuario);
        movimentacaoRef.removeEventListener(valueEventListenerMovimentacao);
    }
}