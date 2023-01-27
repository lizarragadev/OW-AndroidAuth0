package dev.lizarraga.auth0login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.authentication.storage.CredentialsManager
import com.auth0.android.authentication.storage.CredentialsManagerException
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.auth0.android.result.UserProfile
import dev.lizarraga.auth0login.ui.theme.OW_AndroidAuth0LoginTheme

class MainActivity: ComponentActivity() {
    private lateinit var client: Auth0
    private lateinit var apiClient: AuthenticationAPIClient
    
    private lateinit var credential: CredentialsManager

    private var cachedUserProfile: UserProfile? = null
    private var cachedCredentials: Credentials? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        config()

        if(credential.hasValidCredentials()) {
            generarCredenciales()
        }

        setContent {
            OW_AndroidAuth0LoginTheme {
                LoginScreen()
            }
        }
    }

    fun config() {
        client = Auth0(
            getString(R.string.com_auth0_client_id),
            getString(R.string.com_auth0_domain)
        )
        apiClient = AuthenticationAPIClient(client)
        val storage = SharedPreferencesStorage(this)
        credential = CredentialsManager(apiClient, storage)
    }

    @Composable
    fun LoginScreen() {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Auth0 Login") },
                    backgroundColor = MaterialTheme.colors.primary
                ) },
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = {
                    login()
                }) {
                    Text(text = "Login")
                }
            }
        }
    }

    fun login() {
        WebAuthProvider
            .login(client)
            .withScheme(getString(R.string.com_auth0_scheme))
            .start(this, object: Callback<Credentials, AuthenticationException> {
                override fun onFailure(error: AuthenticationException) {
                    println("Error: ${error.message}")
                    cachedCredentials = null
                }

                override fun onSuccess(result: Credentials) {
                    credential.saveCredentials(result)
                    cachedCredentials = result
                    getUserProfile()
                }
            })
    }

    fun getUserProfile() {
        if(cachedCredentials == null) {
            return
        }
        apiClient
            .userInfo(cachedCredentials!!.accessToken)
            .start(object: Callback<UserProfile, AuthenticationException> {
                override fun onFailure(error: AuthenticationException) {
                    println(error.message)
                }

                override fun onSuccess(result: UserProfile) {
                    cachedUserProfile = result
                    sendUserProfile()
                }

            })
    }

    fun sendUserProfile() {
        val intent = Intent(this@MainActivity, SecondActivity::class.java)
        intent.putExtra("userProfile", cachedUserProfile)
        startActivity(intent)
        finish()
    }

    fun generarCredenciales() {
        credential.getCredentials(object: Callback<Credentials, CredentialsManagerException> {
            override fun onFailure(error: CredentialsManagerException) {
                cachedCredentials = null
            }

            override fun onSuccess(result: Credentials) {
                cachedCredentials = result
                getUserProfile()
            }

        })
    }


}







