package com.example.apporganize.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.widget.Toast;

import com.example.apporganize.R;
import com.example.apporganize.config.ConfiguracaoFirebase;
import com.example.apporganize.databinding.ActivityReceitaBinding;
import com.example.apporganize.helper.Base64Custom;
import com.example.apporganize.helper.DateUtil;
import com.example.apporganize.model.Movimentacao;
import com.example.apporganize.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class ReceitaActivity extends AppCompatActivity {

    private ActivityReceitaBinding binding;
    private Movimentacao movimentacao;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Double receitaTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_receita );

        //Preenche data atual
        binding.editDataReceita.setText(DateUtil.dataAtual());

        recuperarReceitaTotal();

        binding.fabSalvarReceita.setOnClickListener(view -> {

            if (validarCamposReceita()){

                movimentacao = new Movimentacao();
                Double valorRecuperado = Double.parseDouble(binding.editValorReceita.getText().toString());
                String data = binding.editDataReceita.getText().toString();
                movimentacao.setValor(valorRecuperado);
                movimentacao.setData(data);
                movimentacao.setCategoria(binding.editCategoriaReceita.getText().toString());
                movimentacao.setDescricao(binding.editDescricaoReceita.getText().toString());
                movimentacao.setTipo("r");

                Double receitaAtualizada = receitaTotal + valorRecuperado;
                atualizarReceita(receitaAtualizada);

                movimentacao.salvar(data);

                finish();
            }

        });

    }

    public Boolean validarCamposReceita(){

        if (!binding.editValorReceita.getText().toString().isEmpty()){
            if (!binding.editDataReceita.getText().toString().isEmpty()){
                if (!binding.editCategoriaReceita.getText().toString().isEmpty()){
                    if (!binding.editDescricaoReceita.getText().toString().isEmpty()){

                        return true;

                    }else{
                        Toast.makeText(getApplicationContext(), "Descrição não preenchido", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Categoria não preenchido", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }else{
                Toast.makeText(getApplicationContext(), "Data não preenchido", Toast.LENGTH_SHORT).show();
                return false;
            }
        }else{
            Toast.makeText(getApplicationContext(), "Valor não preenchido", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void recuperarReceitaTotal(){

        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);

        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);
        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Usuario usuario = snapshot.getValue(Usuario.class);
                receitaTotal = usuario.getReceitaTotal();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void atualizarReceita(Double receita){

        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);

        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);
        usuarioRef.child("receitaTotal").setValue(receita);
    }

}