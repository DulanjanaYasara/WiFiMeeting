package com.example.wifimeeting.page;

import android.os.Bundle;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.wifimeeting.navigation.NavigationHost;
import com.example.wifimeeting.R;
import com.example.wifimeeting.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LecturerLoginPage extends Fragment {
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.lecturer_login_page, container, false);


        final TextInputLayout passwordTextInput = view.findViewById(R.id.password_text_input);
        final TextInputEditText passwordEditText = view.findViewById(R.id.password_edit_text);
        final TextInputLayout usernameTextInput = view.findViewById(R.id.username_text_input);
        final TextInputEditText usernameEditText = view.findViewById(R.id.username_edit_text);

        MaterialButton loginButton = view.findViewById(R.id.login_button);

        // Clear the error once more than 8 characters are typed.
        passwordEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (isPasswordValid(passwordEditText.getText())) {
                    passwordTextInput.setError(null); //Clear the error
                }
                return false;
            }
        });

        // Set an error if the password is less than 8 characters.
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (usernameEditText.getText() == null || usernameEditText.getText().toString().trim().equals("")) {
                    usernameTextInput.setError(getString(R.string.user_name_mandatory));
                } else {
                    usernameTextInput.setError(null);
                }

                if (passwordEditText.getText() == null || passwordEditText.getText().toString().trim().equals("")) {
                    passwordTextInput.setError(getString(R.string.password_mandatory));
                } else if(!isPasswordValid(passwordEditText.getText())){
                    passwordTextInput.setError(getString(R.string.error_password));
                } else {
                    passwordTextInput.setError(null);
                }

                if (usernameTextInput.getError() == null && passwordTextInput.getError() == null) {
                    // Navigate to the next Fragment
                    ((NavigationHost) getActivity()).navigateTo(new LectureHomePage(), true);
                }
            }
        });

        return view;
    }

    private boolean isPasswordValid(@Nullable Editable text) {
        return text != null && text.length() <= Constants.PASSWORD_MAX_LENGTH;
    }
}
