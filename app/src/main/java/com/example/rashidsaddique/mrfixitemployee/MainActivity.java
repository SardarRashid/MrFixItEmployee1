package com.example.rashidsaddique.mrfixitemployee;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rashidsaddique.mrfixitemployee.Common.Common;
import com.example.rashidsaddique.mrfixitemployee.Model.FixItEmployee;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {


    private static final int REQUEST_CODE = 1000;
    Button btnContinue; //btnSignIn,btnRegister;
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
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/Arkhip_font.ttf").setFontAttrId(R.attr.fontPath).build());
        setContentView(R.layout.activity_main);

        printKeyHash();


        //init paper db
        Paper.init(this);

        //Init FireBase
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference(Common.employees_tbl);


        //Init View
        btnContinue = (Button) findViewById(R.id.btnContinue);
//        btnRegister = (Button) findViewById(R.id.btnRegister);
//        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        rootLyout = (RelativeLayout) findViewById(R.id.rootLayout);
        txt_forget_pwd = (TextView) findViewById(R.id.txt_forget_password);
        txt_forget_pwd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ShowDialogForgotPwd();
                return false;
            }
        });

        //Event
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithPhone();

            }
        });

        //Auto SignIn With FacebookKit
        if(AccountKit.getCurrentAccessToken() != null)
        {

            final android.app.AlertDialog waitingDialog =  new SpotsDialog(this);
            waitingDialog.show();
            waitingDialog.setMessage("Please Wait....");
            waitingDialog.setCancelable(false);

            AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                @Override
                public void onSuccess(Account account) {

                    users.child(account.getId().toString())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Common.currentFixItEmployee = dataSnapshot.getValue(FixItEmployee.class);

                                    Intent homeIntent = new Intent(MainActivity.this,EmployeeHome.class);
                                    startActivity(homeIntent);

                                    waitingDialog.dismiss();
                                    finish();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                }

                @Override
                public void onError(AccountKitError accountKitError) {

                }
            });

        }
//        btnRegister.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                showRegisterDialog();
////            }
////        });
////
////        btnSignIn.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                showLoginDialog();
////            }
////        });

        //Auto Login System
//        String user = Paper.book().read(Common.user_field);
//        String pwd = Paper.book().read(Common.pwd_field);
//        if(user != null && pwd != null)
//        {
//            if(!TextUtils.isEmpty(user) &&
//                    !TextUtils.isEmpty(pwd))
//            {
//                autoLogin(user,pwd);
//            }
//
//        }

    }

    private void signInWithPhone() {
        Intent intent= new Intent(MainActivity.this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(LoginType.PHONE,
                        AccountKitActivity.ResponseType.TOKEN);
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,configurationBuilder.build());
        startActivityForResult(intent,REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE)
        {
            AccountKitLoginResult result = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            if(result.getError() != null)
            {
                Toast.makeText(this, ""+result.getError().getErrorType().getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            else if (result.wasCancelled())
            {
                Toast.makeText(this, "Cancel Login", Toast.LENGTH_SHORT).show();
                return;
            }
            else {
                if(result.getAccessToken() != null)
                {
                    //Show Dialog
                    final android.app.AlertDialog waitingDialog =  new SpotsDialog(this);
                    waitingDialog.show();
                    waitingDialog.setMessage("Please Wait....");
                    waitingDialog.setCancelable(false);


                    //Get Current phone
                    AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                        @Override
                        public void onSuccess(final Account account) {
                            final String userId =account.getId();

                            //check if exist on Firebase
                            users.orderByKey().equalTo(account.getId())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (!dataSnapshot.child(account.getId()).exists()) {
                                                //if not exist create new fixItEmployee and login
                                                FixItEmployee fixItEmployee = new FixItEmployee();
                                                fixItEmployee.setPhone( account.getPhoneNumber().toString());
                                                fixItEmployee.setName( account.getPhoneNumber().toString());
                                                fixItEmployee.setAvtarUrl("");
                                                fixItEmployee.setRates("0.0");
                                                fixItEmployee.setEmployeeType("Plumber");

                                                //Register To Firebase
                                                users.child(account.getId())
                                                        .setValue(fixItEmployee)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                                //Login
                                                                users.child(account.getId())
                                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                Common.currentFixItEmployee = dataSnapshot.getValue(FixItEmployee.class);

                                                                                Intent homeIntent = new Intent(MainActivity.this,EmployeeHome.class);
                                                                                startActivity(homeIntent);

                                                                                waitingDialog.dismiss();
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
                                                        Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                                    }
                                                });
                                            }
                                            else  //if user exist login
                                            {
                                                //Login
                                                users.child(account.getId())
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                Common.currentFixItEmployee = dataSnapshot.getValue(FixItEmployee.class);

                                                                Intent homeIntent = new Intent(MainActivity.this,EmployeeHome.class);
                                                                startActivity(homeIntent);

                                                                waitingDialog.dismiss();
                                                                finish();
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });

                                            }

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        }

                        @Override
                        public void onError(AccountKitError accountKitError) {
                            Toast.makeText(MainActivity.this, ""+accountKitError.getErrorType().getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            }
        }
    }

    private void printKeyHash() {

        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.example.rashidsaddique.mrfixitemployee", PackageManager.GET_SIGNATURES);

            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KEYHASH", Base64.encodeToString(md.digest(), Base64.DEFAULT));

            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

//    private void autoLogin(String user, String pwd) {
//
//        final SpotsDialog waitingDialog = new SpotsDialog(MainActivity.this);
//        waitingDialog.show();
//
//
//        //Login
//        auth.signInWithEmailAndPassword(user,pwd)
//                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
//                    @Override
//                    public void onSuccess(AuthResult authResult) {
//                        waitingDialog.dismiss();
//
//
//                        FirebaseDatabase.getInstance().getReference(Common.employees_tbl)
//                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                                .addListenerForSingleValueEvent(new ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(DataSnapshot dataSnapshot) {
//                                        //After asssigned value
//                                        Common.currentFixItEmployee = dataSnapshot.getValue(FixItEmployee.class);
//                                        //start new Activity
//                                        startActivity(new Intent(MainActivity.this, EmployeeHome.class));
//                                        waitingDialog.dismiss();
//                                        finish();
//                                    }
//                                    @Override
//                                    public void onCancelled(DatabaseError databaseError) {
//                                    }
//                                });
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                waitingDialog.dismiss();
//                Snackbar.make(rootLyout, "Failed " + e.getMessage(), Snackbar.LENGTH_SHORT)
//                        .show();
//
//                //Active button
//                btnSignIn.setEnabled(true);
//            }
//        });
//
//    }

    private void ShowDialogForgotPwd() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("FORGET PASSWORD");
        alertDialog.setMessage("Please enter your email address");

        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View forget_pwd_layout = inflater.inflate(R.layout.layout_forget_pwd, null);

        final MaterialEditText edtEmail = (MaterialEditText) forget_pwd_layout.findViewById(R.id.edtEmail);
        alertDialog.setView(forget_pwd_layout);


        //Set Button
        alertDialog.setPositiveButton("RESET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int which) {
                final SpotsDialog waitingDialog = new SpotsDialog(MainActivity.this);
                waitingDialog.show();

                auth.sendPasswordResetEmail(edtEmail.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        dialogInterface.dismiss();
                        waitingDialog.dismiss();

                        Snackbar.make(rootLyout, "Reset password link has been send", Snackbar.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialogInterface.dismiss();
                        waitingDialog.dismiss();

                        Snackbar.make(rootLyout, "" + e.getMessage(), Snackbar.LENGTH_SHORT).show();

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
}

//    private void showLoginDialog() {
//
//        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//        dialog.setTitle("SIGN IN ");
//        dialog.setMessage("Please Use Email to Sign In");
//        LayoutInflater inflater = LayoutInflater.from(this);
//        View login_layout  = inflater.inflate(R.layout.layout_login, null);
//
//        final MaterialEditText edtEmail = login_layout.findViewById(R.id.edtEmail);
//        final MaterialEditText edtPassword = login_layout.findViewById(R.id.edtPassword);
//
//        dialog.setView(login_layout);
//
//        //set Button
//
//        dialog.setPositiveButton("SIGN IN", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                dialogInterface.dismiss();
//
//                //Set Disable Button Sign In If is Processing
//                btnSignIn.setEnabled(false);
//
//                //Check Validation
//
//                if (TextUtils.isEmpty(edtEmail.getText().toString())) {
//                    Snackbar.make(rootLyout, "Please Enter Email Address", Snackbar.LENGTH_SHORT).show();
//                    return;
//                }
//
//                if (TextUtils.isEmpty(edtPassword.getText().toString())) {
//                    Snackbar.make(rootLyout, "Please Enter Your Password", Snackbar.LENGTH_SHORT).show();
//                    return;
//                }
//
//                if (edtPassword.getText().toString().length() < 6) {
//                    Snackbar.make(rootLyout, "Password too Short !!!", Snackbar.LENGTH_SHORT).show();
//                    return;
//                }
//
//                final SpotsDialog waitingDialog = new SpotsDialog(MainActivity.this);
//                waitingDialog.show();
//
//
//                //Login
//                auth.signInWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
//                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
//                    @Override
//                    public void onSuccess(AuthResult authResult) {
//                        waitingDialog.dismiss();
//
//
//                        FirebaseDatabase.getInstance().getReference(Common.employees_tbl)
//                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                                .addListenerForSingleValueEvent(new ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(DataSnapshot dataSnapshot) {
//
//                                        //save value
//                                        Paper.book().write(Common.user_field,edtEmail.getText().toString());
//                                        Paper.book().write(Common.pwd_field,edtPassword.getText().toString());
//
//
//                                        //After asssigned value
//                                        Common.currentFixItEmployee = dataSnapshot.getValue(FixItEmployee.class);
//                                        //start new Activity
//                                        startActivity(new Intent(MainActivity.this, EmployeeHome.class));
//                                        finish();
//                                    }
//                                    @Override
//                                    public void onCancelled(DatabaseError databaseError) {
//                                    }
//                                });
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        waitingDialog.dismiss();
//                        Snackbar.make(rootLyout, "Failed " + e.getMessage(), Snackbar.LENGTH_SHORT)
//                                .show();
//
//                        //Active button
//                        btnSignIn.setEnabled(true);
//                    }
//                });
//
//            }
//
//        });
//        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//
//        dialog.show();
//
//
//    }
//
//
//                private void showRegisterDialog () {
//                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//                    dialog.setTitle("REGISTER ");
//                    dialog.setMessage("Please Use Email to Register");
//
//                    LayoutInflater inflater = LayoutInflater.from(this);
//                    View register_layout = inflater.inflate(R.layout.layout_register, null);
//
//                    final MaterialEditText edtEmail = register_layout.findViewById(R.id.edtEmail);
//                    final MaterialEditText edtPassword = register_layout.findViewById(R.id.edtPassword);
//                    final MaterialEditText edtName = register_layout.findViewById(R.id.edtName);
//                    final MaterialEditText edtPhone = register_layout.findViewById(R.id.edtPhone);
//
//                    dialog.setView(register_layout);
//
//                    //set Button
//
//                    dialog.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            dialogInterface.dismiss();
//
//                            //Check Validation
//
//                            if (TextUtils.isEmpty(edtEmail.getText().toString())) {
//                                Snackbar.make(rootLyout, "Please Enter Email Address", Snackbar.LENGTH_SHORT).show();
//                                return;
//                            }
//
//                            if (TextUtils.isEmpty(edtPhone.getText().toString())) {
//                                Snackbar.make(rootLyout, "Please Enter Your Phone Number", Snackbar.LENGTH_SHORT).show();
//                                return;
//                            }
//
//                            if (edtPhone.getText().toString().length() < 11) {
//                                Snackbar.make(rootLyout, "Phone Number is Short ...", Snackbar.LENGTH_SHORT).show();
//                                return;
//                            }
//
//                            if (TextUtils.isEmpty(edtName.getText().toString())) {
//                                Snackbar.make(rootLyout, "Please Enter Your Name", Snackbar.LENGTH_SHORT).show();
//                                return;
//                            }
//                            if (TextUtils.isEmpty(edtPassword.getText().toString())) {
//                                Snackbar.make(rootLyout, "Please Enter Your Password", Snackbar.LENGTH_SHORT).show();
//                                return;
//                            }
//
//                            if (edtPassword.getText().toString().length() < 6) {
//                                Snackbar.make(rootLyout, "Password too Short !!!", Snackbar.LENGTH_SHORT).show();
//                                return;
//                            }
//
//
//                            //Register new user
//                            auth.createUserWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
//                                @Override
//                                public void onSuccess(AuthResult authResult) {
//
//                                    //Save FixItEmployee to db
//                                    FixItEmployee user = new FixItEmployee();
//                                    user.setEmail(edtEmail.getText().toString());
//                                    user.setName(edtName.getText().toString());
//                                    user.setPhone(edtPhone.getText().toString());
//                                    user.setPassword(edtPassword.getText().toString());
//
//                                    //Use Uid as Key
//                                    users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                        @Override
//                                        public void onSuccess(Void aVoid) {
//                                            Snackbar.make(rootLyout, "Register Success fully ...", Snackbar.LENGTH_SHORT).show();
//
//                                        }
//                                    }).addOnFailureListener(new OnFailureListener() {
//                                        @Override
//                                        public void onFailure(@NonNull Exception e) {
//                                            Snackbar.make(rootLyout, "Failed " + e.getMessage(), Snackbar.LENGTH_SHORT)
//                                                    .show();
//                                        }
//                                    });
//
//                                }
//
//                            }).addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Snackbar.make(rootLyout, "Failed " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
//                                }
//                            });
//
//
//                        }
//                    });
//
//                    dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int which) {
//                            dialogInterface.dismiss();
//
//                        }
//                    });
//
//                    dialog.show();
//
//
//                }
//            }