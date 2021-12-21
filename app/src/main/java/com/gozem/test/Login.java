package com.gozem.test;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.gozem.test.providers.localDB.DbUser;

import java.text.DateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class Login extends AppCompatActivity {

    private static final String TAG = "Login";
    /*Récupération des id des widgets xml avec la library ButterKnife*/
    @BindView(R.id.til_email)
    TextInputLayout til_email;
    @BindView(R.id.til_password)
    TextInputLayout til_password;
    @BindView(R.id.tie_email)
    TextInputEditText tie_email;
    @BindView(R.id.tie_password)
    TextInputEditText tie_password;


    private ProgressDialog dialog;
    private AlertDialog.Builder build_error;
    private Context context;

    private AwesomeValidation validator;

    private DbUser dbUser;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
    }

    /**
     * This method initialize
     *
     */
    void init(){
        context = this;
        validator = new AwesomeValidation(ValidationStyle.TEXT_INPUT_LAYOUT);
        ButterKnife.bind(this);
        setupRulesValidatForm();
        build_error = new AlertDialog.Builder(context);
        dbUser = new DbUser(context);
        scrollView = (ScrollView) findViewById(R.id.login);
    }


    /**
     * login() méthode permettant d'éffectuer la requête
     *
     * @since 2019
     */
    @OnClick(R.id.btnlogin)
    void login() {

        if (!validateEmail(til_email) | !validatePassword(til_password)) {
            return;
        }

        String email = til_email.getEditText().getText().toString();
        String psw = til_password.getEditText().getText().toString();
        validator.clear();

        /*Action à poursuivre si tous les champs sont remplis*/
        if (validator.validate()) {
            dialog = ProgressDialog.show(this, getString(R.string.connexion), getString(R.string.encours), true);
            dialog.show();

            submitData(email, psw);
        }

    }

    private void submitData(String email, String psw) {

        dialog.dismiss();

        if(!dbUser.checkUser(email, psw)){
            // Snack Bar to show success message that record saved successfully
            Snackbar.make(scrollView, getString(R.string.error_msg), Snackbar.LENGTH_LONG).show();
            errorAlert(getString(R.string.error_msg));
        }else {
            Intent intent = new Intent(context, MainActivity.class);
            startActivity(intent);
        }

    }


    private void errorAlert(String msg){
        View view = LayoutInflater.from(context).inflate(R.layout.alert_dialog, null);
        TextView title = (TextView) view.findViewById(R.id.title);
        TextView statutOperation = (TextView) view.findViewById(R.id.statutOperation);
        ImageButton imageButton = (ImageButton) view.findViewById(R.id.image);
        title.setText(getString(R.string.information));
        imageButton.setImageResource(R.drawable.ic_baseline_cancel_24);
        statutOperation.setText(msg);
        build_error.setPositiveButton("OK", null);
        build_error.setCancelable(false);
        build_error.setView(view);
        build_error.show();
    }

    /**
     * validateEmail() méthode permettant de verifier si le mot de passe inséré est valide
     *
     * @see validatePassword
     * @param til_password1
     * @return Boolean
     */
    private boolean validatePassword(TextInputLayout til_password1) {
        String psw = til_password1.getEditText().getText().toString().trim();
        if (psw.isEmpty()) {
            til_password1.setError(getString(R.string.insererPassword));
            til_password1.requestFocus();
            return false;
        } else if (psw.length() < 5) {
            til_password1.setError(getString(R.string.passwordCourt));
            til_password1.requestFocus();
            return false;
        } else {
            til_password1.setError(null);
            return true;
        }
    }

    /**
     * validateEmail() méthode permettant de verifier si le numéro de téléphone inséré est valide
     *
     * @see validateEmail
     * @param til_email1
     * @return Boolean
     */
    private Boolean validateEmail(TextInputLayout til_email1) {
        String myTel = til_email1.getEditText().getText().toString().trim();
        if (myTel.isEmpty()) {
            til_email1.setError(getString(R.string.insererEmail));
            til_email1.requestFocus();
            return false;
        } else if (myTel.length() < 3) {
            til_email1.setError(getString(R.string.emailCourt));
            til_email1.requestFocus();
            return false;
        } else {
            til_email1.setError(null);
            return true;
        }
    }

    /**
     * setupRulesValidatForm() méthode permettant de changer la couleur des champs de saisie en cas d'érreur et vérifi si les champs de saisie sont vides
     * @return change la couleur des champs de saisi selectionnés en cas d'erreur
     */
    private void setupRulesValidatForm() {

        //coloration des champs lorsqu'il y a erreur
        til_email.setErrorTextColor(ColorStateList.valueOf(Color.rgb(255, 0, 0)));
        til_password.setErrorTextColor(ColorStateList.valueOf(Color.rgb(255, 0, 0)));

        validator.addValidation(this, R.id.til_email, RegexTemplate.NOT_EMPTY, R.string.verifierEmail);
        validator.addValidation(this, R.id.til_password, RegexTemplate.NOT_EMPTY, R.string.insererPassword);
    }

    @OnClick(R.id.btnRegister)
    void register(){
        Intent intent = new Intent(context, Register.class);
        startActivity(intent);
    }
}