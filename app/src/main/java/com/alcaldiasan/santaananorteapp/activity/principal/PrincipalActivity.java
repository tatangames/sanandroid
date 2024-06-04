package com.alcaldiasan.santaananorteapp.activity.principal;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.alcaldiasan.santaananorteapp.R;
import com.alcaldiasan.santaananorteapp.activity.login.LoginActivity;
import com.alcaldiasan.santaananorteapp.fragmentos.principal.FragmentPrincipal;
import com.alcaldiasan.santaananorteapp.network.TokenManager;
import com.developer.kalert.KAlertDialog;
import com.google.android.material.navigation.NavigationView;

public class PrincipalActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private TextView navNombre;
    private TokenManager tokenManager;
    private boolean boolSeguroAlertCerrar = true;
    private TextView txtToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        Toolbar toolbar = findViewById(R.id.toolbar);
        NavigationView navigationView = findViewById(R.id.nav_view);
        txtToolbar = findViewById(R.id.txtToolbar);

        txtToolbar.setText(getString(R.string.app_name));

        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));

        setSupportActionBar(toolbar);
        navigationView.setNavigationItemSelectedListener(this);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        // COLOR DE LAS 3 LINEAS PARA ABRIR MENU LATERAL
        toggle.getDrawerArrowDrawable().setColor(getColor(R.color.c_blanco));

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // OBTENER EL TEXTVIEW DEL HEADER QUE ESTA DENTRO DE MENU LATERAL
        View headerView = navigationView.getHeaderView(0);
        navNombre = headerView.findViewById(R.id.txtNavBar);
        navNombre.setText(getString(R.string.app_name));

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContenedor, new FragmentPrincipal())
                .commit();
    }


    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;

        if (id == R.id.nav_servicios) {
            fragment = new FragmentPrincipal();
        }
        else if(id == R.id.nav_cerrar){
            cerrarSesion();
        }

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContenedor, fragment)
                    .commit();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void cerrarSesion(){
        if(boolSeguroAlertCerrar) {
            boolSeguroAlertCerrar = false;
            new Handler().postDelayed(() -> {
                boolSeguroAlertCerrar = true;
            }, 2000);

            KAlertDialog pDialog = new KAlertDialog(this, KAlertDialog.WARNING_TYPE, false);

            pDialog.setTitleText(getString(R.string.cerrar_sesion));
            pDialog.setTitleTextGravity(Gravity.CENTER);
            pDialog.setTitleTextSize(19);

            pDialog.setContentText("");
            pDialog.setContentTextAlignment(View.TEXT_ALIGNMENT_CENTER, Gravity.START);
            pDialog.setContentTextSize(17);

            pDialog.setCancelable(false);
            pDialog.setCanceledOnTouchOutside(false);

            pDialog.confirmButtonColor(R.drawable.codigo_kalert_dialog_corners_confirmar);
            pDialog.setConfirmClickListener(getString(R.string.si), sDialog -> {
                sDialog.dismissWithAnimation();
                salir();
            });

            pDialog.cancelButtonColor(R.drawable.codigo_kalert_dialog_corners_cancelar);
            pDialog.setCancelClickListener(getString(R.string.no), sDialog -> {
                sDialog.dismissWithAnimation();

            });

            pDialog.show();
        }
    }


    private void salir(){
        tokenManager.deletePreferences();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }




}