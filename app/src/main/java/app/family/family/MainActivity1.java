package app.family.family;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import app.family.Facade;
import app.family.NavModules.Drawer;
import app.family.NavModules.Home;
import app.family.NavModules.Messages;
import app.family.NavModules.Notification;
import app.family.NavModules.Profile;
import app.family.R;

public class MainActivity1 extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;

    public void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content, fragment).commit();

//        fragmentTransaction.addToBackStack(null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lr_activity_main);
        bottomNavigationView=findViewById(R.id.bottomnav);
        getSupportFragmentManager().beginTransaction().replace(R.id.content,new Home())
                .addToBackStack(" ")
                .commit();
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {


                Fragment fag=null;
                switch (item.getItemId()){
                    case R.id.Home:
                      fag=new Home();
                       loadFragment(fag);
                        Toast.makeText(getApplicationContext(),"Home", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.noti:
                        fag=new Notification();
                        loadFragment(fag);
                        startActivity(new Intent(MainActivity1.this, Facade.class));
                        Toast.makeText(getApplicationContext(),"notification", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.comm:
                        fag=new Messages();
                        loadFragment(fag);
                        Toast.makeText(getApplicationContext(),"comm", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.profile:
                        fag=new Profile();
                        loadFragment(fag);
                        Toast.makeText(getApplicationContext(),"profile", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.drawer:
                    fag=new Drawer();
                        loadFragment(fag);
                        Toast.makeText(getApplicationContext(),"drawer", Toast.LENGTH_SHORT).show();
                        break;


                }
                return true;
            }
        });
    }



}