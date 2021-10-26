package app.family.family;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import app.family.NavModules.Home;
import app.family.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    ActionBar actionBar;
    DatePickerDialog picker;

    private RequestQueue mRequestQueue;
    String selectedgender="";

    Button reg;
    TextView dob;
    EditText fname,sname,passwd,cpasswd,phoneno,email;
    Spinner gender;
    String selectedUrl="http://192.168.137.1/Familylocalhost/Registeruser.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lr_activity_register);
        actionBar=getSupportActionBar();
        actionBar.setTitle("Register");
        fname=findViewById(R.id.Rfname);
        sname=findViewById(R.id.Rsname);
        gender=findViewById(R.id.Rgender);
        phoneno=findViewById(R.id.Rphone);
        email=findViewById(R.id.Remail);
        cpasswd=findViewById(R.id.Cpaswd);

        passwd=findViewById(R.id.Rpaswd);

        dob=(TextView)findViewById(R.id.dob);
        String [] genderdet={"Male","Female","Custom"};
        ArrayAdapter<String> adapter=new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,genderdet);
        gender.setAdapter(adapter);
        gender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){

                    case 0:
                        selectedgender="male";
                        Toast.makeText(getApplicationContext(), "male", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        selectedgender="female";
                        Toast.makeText(getApplicationContext(), "female", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        selectedgender="custom";
                        Toast.makeText(getApplicationContext(), "custom", Toast.LENGTH_SHORT).show();
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                picker = new DatePickerDialog(Register.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                dob.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                            }
                        }, year, month, day);
                picker.show();
            }
        });
        reg=findViewById(R.id.reg);
        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name_f=fname.getText().toString();
                String name_s=sname.getText().toString();
                String email_=email.getText().toString();
                String cpaswd_=cpasswd.getText().toString();
                String passwd_=passwd.getText().toString();
                String phoneno_=phoneno.getText().toString();
                String dob_=dob.getText().toString();

                Toast.makeText(getApplicationContext(), name_f+name_s+email_+passwd_+phoneno_, Toast.LENGTH_SHORT).show();
                if (!name_f.isEmpty()||!name_s.isEmpty()||!email_.isEmpty()||!cpaswd_.isEmpty()||!passwd_.isEmpty()||!phoneno_.isEmpty()||!dob.equals("Choose your Date of Birth")) {


                    if (cpaswd_.equals(passwd_)) {


                        register(name_f, name_s, dob_, email_, passwd_, phoneno_, selectedgender);

                    } else {

                        Toast.makeText(Register.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    }
                }else{

                    Toast.makeText(Register.this, "Fill all Fields", Toast.LENGTH_SHORT).show();
                }



            }
        });
        }
    private void register(final String fname,final String sname,final String dob, final String email, final String password, final String mobile, final String gender) {

        mRequestQueue= Volley.newRequestQueue(Register.this);
            final ProgressDialog progressDialog = new ProgressDialog(Register.this);
            progressDialog.setTitle("Registering your account");
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.setIndeterminate(false);

            progressDialog.show();


            StringRequest request = new StringRequest(Request.Method.POST,selectedUrl,new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    if (response.equals("You are registered successfully")) {

                        startActivity(new Intent(Register.this, Login.class));
                        progressDialog.dismiss();
                        finish();
                    } else {
                        Toast.makeText(Register.this, response, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(Register.this, error.toString(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String, String> param = new HashMap<>();
                    param.put("fname", fname);
                    param.put("sname", sname);
                    param.put("dob", dob);
                    param.put("email", email);
                    param.put("psw", password);
                    param.put("mobile", mobile);
                    param.put("gender", gender);
                    return param;

                }
            };

            request.setShouldCache(false);
            mRequestQueue.add(request);

          //  request.setRetryPolicy(new DefaultRetryPolicy(50000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
          //  Singleton.getmInstance(Register.this).addToRequestQueue(request);

        }
    }
