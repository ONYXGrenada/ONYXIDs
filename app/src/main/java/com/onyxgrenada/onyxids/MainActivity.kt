package com.onyxgrenada.onyxids

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.ConnectionResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private val REQUEST_CODE_SIGN_IN = 1234
    private val TAG = "GoogleSignIn"
    private val WEB_CLIENT_ID = "724744964375-u6q98njkfo15egcu537qv3kuereberoa.apps.googleusercontent.com"

    private var mGoogleApiClient: GoogleApiClient? = null
    private var mAuth: FirebaseAuth? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_google_sign_in.setOnClickListener(this)

        // Example of a call to a native method
        //txt_sample_text.text = stringFromJNI()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID)
                .requestEmail()
                .build()

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener*/)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        mAuth = FirebaseAuth.getInstance()

    }

    override fun onStart() {
        super.onStart()
        val currentUser = mAuth!!.currentUser
        updateUI(currentUser)
    }

    override fun onClick(v: View?) {
        val i = v!!.id

        when (i) {
            R.id.btn_google_sign_in -> signIn()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                val account = result.signInAccount
                firebaseAuthWithGoogle(account!!)
            } else {
                updateUI(null)
                Toast.makeText(applicationContext, "SignIn : failed!",
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.e(TAG, "firebaseAuthWithGoogle():" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success
                        Log.e(TAG, "signInWithCredential: Success!")
                        val user = mAuth!!.currentUser
                        updateUI(user)
                    } else {
                        // Sign in fails
                        Log.w(TAG, "signInWithCredential: Failed!", task.exception)
                        Toast.makeText(applicationContext, "Authentication failed!",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }
                }
    }


    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.e(TAG, "onConnectionFailed():" + connectionResult)
        Toast.makeText(applicationContext, "Google Play Services error.", Toast.LENGTH_SHORT).show()
    }

    private fun signIn() {
        val intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(intent, REQUEST_CODE_SIGN_IN)
        Toast.makeText(applicationContext, "Seems like we are having an error.",
                Toast.LENGTH_SHORT).show()
    }



    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            txt_sample_text.text = "Google User email: " + user.email!! + user.uid
            //tvDetail.text = "Firebase User ID: " + user.uid

            btn_google_sign_in.visibility = View.GONE
            //layout_sign_out_and_disconnect.visibility = View.VISIBLE
        } else {
            txt_sample_text.text = "Signed Out"
            //tvDetail.text = null

            btn_google_sign_in.visibility = View.VISIBLE
            //layout_sign_out_and_disconnect.visibility = View.GONE
        }
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    //external fun stringFromJNI(): String

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
