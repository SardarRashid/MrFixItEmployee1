package com.example.rashidsaddique.mrfixitemployee;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ActivityChooserView;
import android.text.Layout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.rashidsaddique.mrfixitemployee.Common.Common;
import com.example.rashidsaddique.mrfixitemployee.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.awt.font.TextAttribute;

import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {




    Button btnSignIn,btnRegister;
    RelativeLayout rootLyout;

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;

    TextView txt_forget_pwd;



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
        users = db.getReference(Common.employees_tbl);


        //Init View
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        rootLyout = (RelativeLayout) findViewById(R.id.rootLayout);
        txt_forget_pwd= (TextView) findViewById(R.id.txt_forget_password);
        txt_forget_pwd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ShowDialogForgotPwd();
                return false;
            }
        });

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

    private void ShowDialogForgotPwd() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("FORGET PASSWORD");
        alertDialog.setMessage("Please enter your email address");

        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View forget_pwd_layout = inflater.inflate(R.layout.layout_forget_pwd,null);

        final MaterialEditText edtEmail = (MaterialEditText)forget_pwd_layout.findViewById(R.id.edtEmail);
        alertDialog.setView(forget_pwd_layout);


        //Set Button
        alertDialog.setPositiveButton("RESET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int which) {
                final SpotsDialog waitingDialog = new SpotsDialog(MainActivity.this);
                waitingDialog.show();

                auth.sendPasswordResetEmail(edtEmail.getText().toString().trim())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                dialogInterface.dismiss();
                                waitingDialog.dismiss();

                                Snackbar.make(rootLyout, "Reset password link has been send",Snackbar.LENGTH_SHORT)
                                        .show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialogInterface.dismiss();
                        waitingDialog.dismiss();

                        Snackbar.make(rootLyout, ""+e.getMessage(),Snackbar.LENGTH_SHORT)
                                .show();

                    }
                });
            }
        });
        alertDialog.setNegativeButton("CANCLE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();

            }
        });
        alertDialog.show();

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

                //Set Disable Button Sign In If is Processing
                btnSignIn.setEnabled(false);

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

                final SpotsDialog waitingDialog = new SpotsDialog(MainActivity.this);
                waitingDialog.show();


                //Login
                auth.signInWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        waitingDialog.dismiss();

                        FirebaseDatabase.getInstance().getReference(Common.employees_tbl)
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        //After asssigned value
                                        Common.currentUser = dataSnapshot.getValue(User.class);
                                        //start new Activity
                                        startActivity(new Intent(MainActivity.this, EmployeeHome.class));
                                        finish();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        waitingDialog.dismiss();
                        Snackbar.make(rootLyout, "Failed " + e.getMessage(), Snackbar.LENGTH_SHORT)
                                .show();

                        //Active button
                        btnSignIn.setEnabled(true);
                    }
                });

            }

        });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
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

                                    //Use Uid as Key
                                    users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Snackbar.make(rootLyout, "Register Success fully ...", Snackbar.LENGTH_SHORT).show();

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Snackbar.make(rootLyout, "Failed " + e.getMessage(), Snackbar.LENGTH_SHORT)
                                                    .show();
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