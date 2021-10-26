package app.family.family;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import app.family.NavModules.Home;
import app.family.R;

public class Login extends AppCompatActivity {
    TextView createAccount;
    Button login;
    ActionBar actionBar;
    EditText phoneno,pass;
    String selectedUrl="http://192.168.137.1/Familylocalhost/login.php";
    private RequestQueue mRequestQueue;
    IpAdresses pAddresses;
    String fna="";
    String sna="";
    getuser obj;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lr_activity_login);
        actionBar=getSupportActionBar();
        actionBar.setTitle("Login");
        phoneno=findViewById(R.id.lphoneno);
        pass=findViewById(R.id.lpass);
        createAccount=findViewById(R.id.createAcc);
        login=findViewById(R.id.loginbtn);
        pAddresses=new IpAdresses();
        obj=null;



        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login(phoneno.getText().toString(),pass.getText().toString());


            }
        });
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this,Register.class));

            }
        });
    }

    String getdata(final String phon){

        StringRequest request=new StringRequest(Request.Method.DEPRECATED_GET_OR_POST, pAddresses.getGetdt(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                 fna= jsonObject.getString("fname");
                                sna = jsonObject.getString("sname");
                                Toast.makeText(Login.this, sna+fna, Toast.LENGTH_SHORT).show();

                                 obj = new getuser(fna, sna);
                                //obj.add(object);
                            }

                        } catch (Exception e) {

                        }
                        //adapter = new guiderecyclerview(getContext(), obj);
                      //  recyclerView.setAdapter(adapter);

                    }
                },  new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Login.this, error.toString(), Toast.LENGTH_SHORT).show();

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> param = new HashMap<>();
                param.put("phone",phon );



                return param;

            }
        };



        Volley.newRequestQueue(Login.this).add(request);
        return fna+sna;
    }
    private void Login(final String fname,final String sname) {



            final ProgressDialog progressDialog = new ProgressDialog(Login.this);
            progressDialog.setTitle("Login in your account");
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.setIndeterminate(false);
            mRequestQueue= Volley.newRequestQueue(Login.this);
            progressDialog.show();

            StringRequest request = new StringRequest(Request.Method.POST, selectedUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    if (response.equals("Login succesful")) {

                       // getdata(fname);
                        Toast.makeText(Login.this,obj.getFname(), Toast.LENGTH_SHORT).show();
                        Intent intt=new Intent(Login.this, MainActivity1.class);
                        intt.putExtra("fname",obj.getFname());
                        intt.putExtra("sname",obj.getSname());
                        startActivity(intt);;

                       // startActivity(new Intent(Login.this, Home.class));
                        progressDialog.dismiss();
                        finish();
                    } else {
                        Toast.makeText(Login.this, response, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(Login.this, error.toString(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String, String> param = new HashMap<>();
                    param.put("fname", fname);
                    param.put("sname", sname);

                    return param;

                }
            };

            request.setShouldCache(false);


            request.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mRequestQueue.add(request);

            Singleton.getmInstance(Login.this).addToRequestQueue(request);

        }
    }




























































































































