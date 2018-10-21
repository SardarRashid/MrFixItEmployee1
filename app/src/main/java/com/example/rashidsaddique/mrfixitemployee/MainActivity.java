package com.example.rashidsaddique.mrfixitemployee;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ActivityChooserView;
import android.text.Layout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.example.rashidsaddique.mrfixitemployee.Model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.awt.font.TextAttribute;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {




    Button btnSignIn,btnRegister;
    RelativeLayout rootLyout;

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_main);

        //Init FireBase
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");


        //Init View
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        rootLyout = (RelativeLayout) findViewById(R.id.rootLayout);

        //Event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterDialog();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoginDialog();
            }
        });

    }

    private void showLoginDialog() {

        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("SIGN IN ");
        dialog.setMessage("Please Use Email to Sign In");

        LayoutInflater inflater = LayoutInflater.from(this);
        View login_layout  = inflater.inflate(R.layout.layout_login, null);

        final MaterialEditText edtEmail = login_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword = login_layout.findViewById(R.id.edtPassword);

        dialog.setView(login_layout);

        //set Button

        dialog.setPositiveButton("SIGN IN", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();


                //Check Validation

                if (TextUtils.isEmpty(edtEmail.getText().toString())) {
                    Snackbar.make(rootLyout, "Please Enter Email Address", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(edtPassword.getText().toString())) {
                    Snackbar.make(rootLyout, "Please Enter Your Password", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (edtPassword.getText().toString().length() < 6) {
                    Snackbar.make(rootLyout, "Password too Short !!!", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                //Login
                auth.signInWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        startActivity(new Intent(MainActivity.this, Welcome.class));
                        finish();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(rootLyout, "Failed " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                });

            }

        });
        dialog.setNegativeButton("CANCLE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();


    }


                private void showRegisterDialog () {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setTitle("REGISTER ");
                    dialog.setMessage("Please Use Email to Register");

                    LayoutInflater inflater = LayoutInflater.from(this);
                    View register_layout = inflater.inflate(R.layout.layout_register, null);

                    final MaterialEditText edtEmail = register_layout.findViewById(R.id.edtEmail);
                    final MaterialEditText edtPassword = register_layout.findViewById(R.id.edtPassword);
                    final MaterialEditText edtName = register_layout.findViewById(R.id.edtName);
                    final MaterialEditText edtPhone = register_layout.findViewById(R.id.edtPhone);

                    dialog.setView(register_layout);

                    //set Button

                    dialog.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();


                            //Check Validation

                            if (TextUtils.isEmpty(edtEmail.getText().toString())) {
                                Snackbar.make(rootLyout, "Please Enter Email Address", Snackbar.LENGTH_SHORT).show();
                                return;
                            }

                            if (TextUtils.isEmpty(edtPhone.getText().toString())) {
                                Snackbar.make(rootLyout, "Please Enter Your Phone Number", Snackbar.LENGTH_SHORT).show();
                                return;
                            }

                            if (edtPhone.getText().toString().length() < 11) {
                                Snackbar.make(rootLyout, "Phone Number is Short ...", Snackbar.LENGTH_SHORT).show();
                                return;
                            }

                            if (TextUtils.isEmpty(edtName.getText().toString())) {
                                Snackbar.make(rootLyout, "Please Enter Your Name", Snackbar.LENGTH_SHORT).show();
                                return;
                            }
                            if (TextUtils.isEmpty(edtPassword.getText().toString())) {
                                Snackbar.make(rootLyout, "Please Enter Your Password", Snackbar.LENGTH_SHORT).show();
                                return;
                            }

                            if (edtPassword.getText().toString().length() < 6) {
                                Snackbar.make(rootLyout, "Password too Short !!!", Snackbar.LENGTH_SHORT).show();
                                return;
                            }


                            //Register new user
                            auth.createUserWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {

                                    //Save User to db
                                    User user = new User();
                                    user.setEmail(edtEmail.getText().toString());
                                    user.setName(edtName.getText().toString());
                                    user.setPhone(edtPhone.getText().toString());
                                    user.setPassword(edtPassword.getText().toString());

                                    //Use email to Key
                                    users.child(user.getEmail()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Snackbar.make(rootLyout, "Register Success fully ...", Snackbar.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Snackbar.make(rootLyout, "Failed " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                        }
                                    });


                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Snackbar.make(rootLyout, "Failed " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                }
                            });


                        }
                    });

                    dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            dialogInterface.dismiss();

                        }
                    });

                    dialog.show();


                }
            }