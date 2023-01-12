package com.example.apporganize.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.apporganize.R;
import com.example.apporganize.config.ConfiguracaoFirebase;
import com.example.apporganize.databinding.ActivityDespesaBinding;
import com.example.apporganize.helper.Base64Custom;
import com.example.apporganize.helper.DateUtil;
import com.example.apporganize.model.Movimentacao;
import com.example.apporganize.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class DespesaActivity extends AppCompatActivity {

    private ActivityDespesaBinding binding;
    private Movimentacao movimentacao;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Double despesaTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_despesa);

        //preenche o campo com a data atual
        binding.editDataDespesa.setText(DateUtil.dataAtual());

        recupararDespesaTotal();

        binding.fabSalvarDespesa.setOnClickListener( view -> {

            if (validarCamposDespesa()){

                movimentacao = new Movimentacao();
                String data = binding.editDataDespesa.getText().toString();
                Double valorRecuperado = Double.parseDouble(binding.editValorDespesa.getText().toString());
                movimentacao.setValor(valorRecuperado);
                movimentacao.setData(data);
                movimentacao.setCategoria(binding.editCategoriaDespesa.getText().toString());
                movimentacao.setDescricao(binding.editDescricaoDespesa.getText().toString());
                movimentacao.setTipo("d");

               Double despesaAtualizada = despesaTotal + valorRecuperado;
               atualizarDespesa(despesaAtualizada);

                movimentacao.salvar(data);
                finish();
            }

        });
    }

    public Boolean validarCamposDespesa(){

        if (!binding.editValorDespesa.getText().toString().isEmpty()){
            if (!binding.editDataDespesa.getText().toString().isEmpty()){
                if (!binding.editCategoriaDespesa.getText().toString().isEmpty()){
                    if (!binding.editDescricaoDespesa.getText().toString().isEmpty()){

                        return true;

                    }else{
                        Toast.makeText(getApplicationContext(), "Valor não preenchdido", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Categoria não preenchdido", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }else{
                Toast.makeText(getApplicationContext(), "Data não preenchdido", Toast.LENGTH_SHORT).show();
                return false;
            }
        }else{
            Toast.makeText(getApplicationContext(), "Valor não preenchdido", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void recupararDespesaTotal(){

        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);

        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);
        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Usuario usuario = snapshot.getValue(Usuario.class);
                despesaTotal = usuario.getDespesaTotal();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void atualizarDespesa(Double despesa){

        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);

        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);
        usuarioRef.child("despesaTotal").setValue(despesa);
    }
}