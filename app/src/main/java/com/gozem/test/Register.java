package com.gozem.test;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.gozem.test.model.User;
import com.gozem.test.providers.localDB.DbUser;

import java.text.DateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Register extends AppCompatActivity {

    private static final String TAG = "Register";

    @BindView(R.id.til_fullName)
    TextInputLayout til_fullName;
    @BindView(R.id.tie_fullName)
    TextInputEditText tie_fullName;

    @BindView(R.id.til_email)
    TextInputLayout til_email;
    @BindView(R.id.tie_email)
    TextInputEditText tie_email;

    @BindView(R.id.til_password)
    TextInputLayout til_password;
    @BindView(R.id.tie_password)
    TextInputEditText tie_password;

    private ProgressDialog dialog;
    private AlertDialog.Builder build_error;
    private Context context;

    private AwesomeValidation validator;

    private DbUser dbUser;
    private Date current;
    private DateFormat shortDateFormat;
    private Toolbar toolbar;
    private User user;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();
    }

    void init(){
        context = this;

        toolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.souscription));
        toolbar.setSubtitle(getString(R.string.fillForm));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        validator = new AwesomeValidation(ValidationStyle.TEXT_INPUT_LAYOUT);
        ButterKnife.bind(this);
        setupRulesValidatForm();
        build_error = new AlertDialog.Builder(context);
        dbUser = new DbUser(context);
        current = new Date();
        shortDateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        user = new User();
        scrollView = (ScrollView) findViewById(R.id.register);
    }



    @OnClick(R.id.btnRegister)
    void register(){

        if (!validateEmail(til_email) | !validatePassword(til_password) | !validateFullName(til_fullName)) {
            return;
        }

        String fullName = til_fullName.getEditText().getText().toString();
        String email = til_email.getEditText().getText().toString();
        String psw = til_password.getEditText().getText().toString();
        validator.clear();

        /*Action à poursuivre si tous les champs sont remplis*/
        if (validator.validate()) {
            dialog = ProgressDialog.show(this, getString(R.string.connexion), getString(R.string.encours), true);
            dialog.show();

            submitData(fullName, email, psw);
        }

    }



    private void submitData(String fullName, String email, String psw) {

        dialog.dismiss();
        
        if(!dbUser.checkEmailUser(email)){
            
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPassword(psw);
            user.setCreated_at(shortDateFormat.format(current));
            dbUser.AddUser(user);
            // Snack Bar to show success message that record saved successfully
            Snackbar.make(scrollView, getString(R.string.success_message), Snackbar.LENGTH_LONG).show();
            successAlert(getString(R.string.success_message));   
        } else {
            // Snack Bar to show error message that record already exists
            Snackbar.make(scrollView, getString(R.string.error_email), Snackbar.LENGTH_LONG).show();
        }

    }

    private void successAlert(String msg){

        View view = LayoutInflater.from(context).inflate(R.layout.alert_dialog, null);
        TextView title = (TextView) view.findViewById(R.id.title);
        TextView statutOperation = (TextView) view.findViewById(R.id.statutOperation);
        ImageButton imageButton = (ImageButton) view.findViewById(R.id.image);
        title.setText(getString(R.string.information));
        imageButton.setImageResource(R.drawable.ic_baseline_check_circle_24);
        statutOperation.setText(msg);
        build_error.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent(context, Login.class);
                startActivity(intent);
            }
        });
        build_error.setCancelable(false);
        build_error.setView(view);
        build_error.show();

        til_fullName.getEditText().setText("");
        til_email.getEditText().setText("");
        til_password.getEditText().setText("");

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


    private Boolean validateFullName(TextInputLayout til_fullName1) {
        String fullName = til_fullName1.getEditText().getText().toString().trim();
        if (fullName.isEmpty()) {
            til_fullName1.setError(getString(R.string.insererFullName));
            til_fullName1.requestFocus();
            return false;
        } else if (fullName.length() < 3) {
            til_fullName1.setError(getString(R.string.fullNameShort));
            til_fullName1.requestFocus();
            return false;
        } else {
            til_fullName1.setError(null);
            return true;
        }
    }

    /**
     * setupRulesValidatForm() méthode permettant de changer la couleur des champs de saisie en cas d'érreur et vérifi si les champs de saisie sont vides
     * @return change la couleur des champs de saisi selectionnés en cas d'erreur
     */
    private void setupRulesValidatForm() {

        //coloration des champs lorsqu'il y a erreur
        til_fullName.setErrorTextColor(ColorStateList.valueOf(Color.rgb(255, 0, 0)));
        til_email.setErrorTextColor(ColorStateList.valueOf(Color.rgb(255, 0, 0)));
        til_password.setErrorTextColor(ColorStateList.valueOf(Color.rgb(255, 0, 0)));

        validator.addValidation(this, R.id.til_fullName, RegexTemplate.NOT_EMPTY, R.string.insererFullName);
        validator.addValidation(this, R.id.til_email, RegexTemplate.NOT_EMPTY, R.string.verifierEmail);
        validator.addValidation(this, R.id.til_password, RegexTemplate.NOT_EMPTY, R.string.insererPassword);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

}