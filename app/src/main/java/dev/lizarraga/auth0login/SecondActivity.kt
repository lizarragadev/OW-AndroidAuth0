package dev.lizarraga.auth0login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.authentication.storage.CredentialsManager
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.UserProfile
import dev.lizarraga.auth0login.ui.theme.OW_AndroidAuth0LoginTheme

class SecondActivity : ComponentActivity() {

    private lateinit var client: Auth0
    private lateinit var apiClient: AuthenticationAPIClient

    private lateinit var credential: CredentialsManager

    lateinit var userProfile: UserProfile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        config()

        if(intent.hasExtra("userProfile")) {
            userProfile = intent.getSerializableExtra("userProfile") as UserProfile
        }

        setContent {
            OW_AndroidAuth0LoginTheme {
                DetailScreen()
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
    fun DetailScreen() {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Sesión Iniciada") },
                    backgroundColor = MaterialTheme.colors.primary,
                    navigationIcon = {
                        IconButton(onClick = {
                            finish()
                        }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier
                                    .padding(16.dp)
                                    .clickable {
                                        finish()
                                    }
                            )
                        }
                    }
                )
            },
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileImage(url = userProfile.pictureURL ?: "https://cdn-icons-png.flaticon.com/512/3135/3135715.png", description = "Image")
                ProfileInfo(hintText = "Nombre:", text = userProfile.name?:"No disponible")
                Spacer(modifier = Modifier.height(10.dp))
                ProfileInfo(hintText = "Correo:", text = userProfile.email?:"No disponible")
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = {
                    logout()
                }) {
                    Text(text = "Cerrar Sesión")
                }
            }
        }
    }

    @Composable
    fun ProfileImage(url: String, description: String, ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = rememberAsyncImagePainter(url),
                contentDescription = description,
                modifier = Modifier
                    .fillMaxSize(0.3f),
            )
        }
    }

    @Composable
    fun ProfileInfo(hintText: String, text: String, ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = hintText,
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp,
                )
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                )
            )
        }
    }

    fun logout() {
        WebAuthProvider
            .logout(client)
            .withScheme(getString(R.string.com_auth0_scheme))
            .start(this, object: Callback<Void?, AuthenticationException> {
                override fun onFailure(error: AuthenticationException) {
                    Toast.makeText(this@SecondActivity, "No se pudo cerrar sesión", Toast.LENGTH_SHORT).show()
                }

                override fun onSuccess(result: Void?) {
                    credential.clearCredentials()
                    startActivity(Intent(this@SecondActivity, MainActivity::class.java))
                    finish()
                }

            })

    }

}
