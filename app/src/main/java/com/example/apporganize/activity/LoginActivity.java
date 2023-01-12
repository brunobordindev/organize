package com.example.apporganize.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.apporganize.R;
import com.example.apporganize.config.ConfiguracaoFirebase;
import com.example.apporganize.databinding.ActivityCadastroBinding;
import com.example.apporganize.databinding.ActivityLoginBinding;
import com.example.apporganize.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private Usuario usuario;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        getSupportActionBar().setTitle("Login");

        binding.btnEntrar.setOnClickListener(view -> {

            String email = binding.editEmailLogin.getText().toString();
            String senha = binding.editSenhaLogin.getText().toString();

            if (!email.isEmpty()){
                if (!senha.isEmpty()){

                    usuario = new Usuario(email, senha);
                    validarLogin();

                }else{
                    Toast.makeText(getApplicationContext(), "Preencha a senha", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(getApplicationContext(), "Preencha o e-mail", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void validarLogin(){

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){

                    abrirTelaPrincipal();
                    Toast.makeText(getApplicationContext(), "Sucesso ao se logar!", Toast.LENGTH_SHORT).show();
                }else{

                    String excecao = "";
                    try {
                        throw task.getException();
                    }catch (FirebaseAuthInvalidUserException e){
                        excecao = "Usuário não está cadastrado";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        excecao = "E-mail e senha não corresponde a um usuário cadastrado";
                    }catch (Exception e){
                        excecao = "Erro ao logar usuário" + e.getMessage();
                    }

                    Toast.makeText(getApplicationContext(), excecao, Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    public void abrirTelaPrincipal(){
        startActivity(new Intent(this, PrincipalActivity.class));
        finish();
    }
}