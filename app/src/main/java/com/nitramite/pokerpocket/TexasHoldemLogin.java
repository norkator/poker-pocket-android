package com.nitramite.pokerpocket;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nitramite.socket.OnWebSocketEvent;
import com.nitramite.socket.WebSocketClientHoldEm;

import org.json.JSONException;
import org.json.JSONObject;

public class TexasHoldemLogin extends AppCompatActivity implements OnWebSocketEvent {

    // Logging
    private final static String TAG = TexasHoldemLogin.class.getSimpleName();

    // WebSocket
    WebSocketClientHoldEm webSocketClient;
    private int CONNECTION_ID = 0;
    private String SOCKET_KEY = null;

    // Components
    TextView formTopTextTV, currentUserNameTV;

    // Components (register)
    LinearLayout registerFormLL;
    EditText registerUsernameET, registerPasswordET1, registerPasswordET2, registerEmailET;
    Button registerSignUpBtn, registerLogInBtn, registerForgotPasswordBtn;

    // Components (login)
    LinearLayout loginFormLL;
    EditText loginUsernameET, loginPasswordET;
    Button loginLogInBtn, loginSignUpBtn, loginForgotPasswordBtn;
    Button loginLogoutBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texas_holdem_login);

        // Find components
        formTopTextTV = findViewById(R.id.formTopTextTV);
        currentUserNameTV = findViewById(R.id.currentUserNameTV);
        registerFormLL = findViewById(R.id.registerFormLL);
        loginFormLL = findViewById(R.id.loginFormLL);
        registerUsernameET = findViewById(R.id.registerUsernameET);
        registerPasswordET1 = findViewById(R.id.registerPasswordET1);
        registerPasswordET2 = findViewById(R.id.registerPasswordET2);
        registerEmailET = findViewById(R.id.registerEmailET);
        registerSignUpBtn = findViewById(R.id.registerSignUpBtn);
        registerLogInBtn = findViewById(R.id.registerLogInBtn);
        registerForgotPasswordBtn = findViewById(R.id.registerForgotPasswordBtn);
        loginUsernameET = findViewById(R.id.loginUsernameET);
        loginPasswordET = findViewById(R.id.loginPasswordET);
        loginLogInBtn = findViewById(R.id.loginLogInBtn);
        loginSignUpBtn = findViewById(R.id.loginSignUpBtn);
        loginForgotPasswordBtn = findViewById(R.id.loginForgotPasswordBtn);
        loginLogoutBtn = findViewById(R.id.loginLogoutBtn);

        initOnClickListeners();
        initWebSocketClient();
    } // End of onCreate();


    private void initWebSocketClient() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        final String userName = sharedPreferences.getString(Constants.SP_ONLINE_HOLDEM_USERNAME, null);
        if (userName != null) {
            currentUserNameTV.setText("Logged in as: " + userName);
        } else {
            currentUserNameTV.setVisibility(View.GONE);
        }
        final boolean isDevelopmentServer = sharedPreferences.getBoolean(Constants.SP_DEVELOPMENT_SERVER, false);
        if (sharedPreferences.getBoolean(Constants.SP_ONLINE_HOLDEM_IS_LOGGED_IN, false)) {
            loginLogoutBtn.setVisibility(View.VISIBLE);
        }
        if (isDevelopmentServer) {
            webSocketClient = new WebSocketClientHoldEm(this, Constants.DEVELOPMENT_SERVER);
        } else {
            webSocketClient = new WebSocketClientHoldEm(this, Constants.PRODUCTION_SERVER);
        }
    }


    @Override
    public void onConnectedEvent() {
        if (webSocketClient.webSocketClient == null) {
            runOnUiThread(() -> {
                Toast.makeText(TexasHoldemLogin.this, "Error creating socket connection!", Toast.LENGTH_LONG).show();
                TexasHoldemLogin.this.finish();
            });
        }
    }

    @Override
    public void onStringMessage(String msg) {
        try {
            JSONObject jsonObject = new JSONObject(msg);
            switch (jsonObject.getString("key")) {
                case "connectionId":
                    CONNECTION_ID = jsonObject.getInt("connectionId");
                    SOCKET_KEY = jsonObject.getString("socketKey");
                    break;
                case "accountCreated":
                    onAccountCreated(jsonObject.getJSONObject("data"));
                    break;
                case "loginResult":
                    onLoginResult(jsonObject.getJSONObject("data"));
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------------------------------------------


    private void onAccountCreated(final JSONObject resultData) {
        try {
            if (resultData.getBoolean("result")) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        registerUsernameET.setText("");
                        registerPasswordET1.setText("");
                        registerPasswordET2.setText("");
                        registerEmailET.setText("");
                        loginFormLL.setVisibility(View.VISIBLE);
                        registerFormLL.setVisibility(View.GONE);
                        Toast.makeText(TexasHoldemLogin.this, "Account successfully created, you can now login.", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(TexasHoldemLogin.this, "Account already exists. Please try another one.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void onLoginResult(final JSONObject resultData) {
        try {
            if (resultData.getBoolean("result")) {
                final String username = resultData.getString("username");
                final String passwordHash = resultData.getString("password");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SharedPreferences setSharedPreferences = PreferenceManager.getDefaultSharedPreferences(TexasHoldemLogin.this.getBaseContext());
                        SharedPreferences.Editor editor = setSharedPreferences.edit();
                        editor.putBoolean(Constants.SP_ONLINE_HOLDEM_IS_LOGGED_IN, true);
                        editor.putString(Constants.SP_ONLINE_HOLDEM_USERNAME, username);
                        editor.putString(Constants.SP_ONLINE_HOLDEM_PASSWORD, passwordHash);
                        editor.apply();
                        Toast.makeText(TexasHoldemLogin.this, "You are now logged in and your log in parameters have been saved.", Toast.LENGTH_LONG).show();
                        webSocketClient.disconnect(CONNECTION_ID, SOCKET_KEY, -1);
                        TexasHoldemLogin.this.finish();
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(TexasHoldemLogin.this, "Login failed! Check your username and password.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // ---------------------------------------------------------------------------------------------

    private void createAccount() {
        final String username = registerUsernameET.getText().toString();
        final String password1 = registerPasswordET1.getText().toString();
        final String password2 = registerPasswordET2.getText().toString();
        final String email = registerEmailET.getText().toString();
        if (username.length() > 5) {
            if (username.length() <= 20) {
                if (password1.length() > 0 && password2.length() > 0) {
                    if (password1.equals(password2)) {
                        if (email.length() > 0 && email.contains("@")) {
                            webSocketClient.createAccount(CONNECTION_ID, SOCKET_KEY, username, password1, email);
                        } else {
                            Toast.makeText(this, "Email does not contain @ or it's not set.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Password's do not match.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Password and re-enter password must have input.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Username cannot be longer than 20 characters", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Username must be longer than five characters.", Toast.LENGTH_SHORT).show();
        }
    }


    private void userLogin() {
        final String username = loginUsernameET.getText().toString();
        final String password = loginPasswordET.getText().toString();
        if (username.length() > 0 && password.length() > 0) {
            webSocketClient.userLogin(CONNECTION_ID, SOCKET_KEY, username, password);
        } else {
            Toast.makeText(this, "Please check your input.", Toast.LENGTH_SHORT).show();
        }
    }


    private void userLogout() {
        SharedPreferences setSharedPreferences = PreferenceManager.getDefaultSharedPreferences(TexasHoldemLogin.this.getBaseContext());
        SharedPreferences.Editor editor = setSharedPreferences.edit();
        editor.putBoolean(Constants.SP_ONLINE_HOLDEM_IS_LOGGED_IN, false);
        editor.remove(Constants.SP_ONLINE_HOLDEM_USERNAME);
        editor.remove(Constants.SP_ONLINE_HOLDEM_PASSWORD);
        editor.apply();
        Toast.makeText(this, "Logged out.", Toast.LENGTH_SHORT).show();
        TexasHoldemLogin.this.finish();
    }

    // ---------------------------------------------------------------------------------------------

    private void initOnClickListeners() {
        registerLogInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginFormLL.setVisibility(View.VISIBLE);
                registerFormLL.setVisibility(View.GONE);
                formTopTextTV.setText("Login to your Poker Pocket account");
            }
        });
        loginSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginFormLL.setVisibility(View.GONE);
                registerFormLL.setVisibility(View.VISIBLE);
                formTopTextTV.setText("Register your Poker Pocket account");
            }
        });
        loginForgotPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "http://www.nitramite.com/contact.html";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
        registerForgotPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "http://www.nitramite.com/contact.html";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
        loginLogInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userLogin();
            }
        });
        registerSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount();
            }
        });
        loginLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userLogout();
            }
        });
    }


    // ---------------------------------------------------------------------------------------------

} 