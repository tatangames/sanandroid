package com.alcaldiasan.santaananorteapp.fragmentos.login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.alcaldiasan.santaananorteapp.R;

public class FragmentLogin extends Fragment {


    private EditText edtTelefono;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_login, container, false);

        edtTelefono = vista.findViewById(R.id.edtTelefono);

        edtTelefono.addTextChangedListener(new TextWatcher() {
            private String current = "";
            private String previous = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                previous = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    edtTelefono.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[^\\d]", "");
                    String formattedString = "";

                    if (cleanString.length() > 4) {
                        formattedString = cleanString.substring(0, 4) + " " + cleanString.substring(4);
                    } else {
                        formattedString = cleanString;
                    }

                    if (formattedString.length() > 9) {
                        formattedString = formattedString.substring(0, 9);
                    }

                    current = formattedString;
                    edtTelefono.setText(formattedString);
                    edtTelefono.setSelection(formattedString.length());

                    edtTelefono.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed here
            }
        });


        return vista;
    }




}
