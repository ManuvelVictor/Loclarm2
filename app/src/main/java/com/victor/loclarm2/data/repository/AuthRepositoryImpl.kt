package com.victor.loclarm2.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.UserProfileChangeRequest
import com.victor.loclarm2.data.model.User
import com.victor.loclarm2.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val credentialManager: CredentialManager
) : AuthRepository {

    override suspend fun register(email: String, password: String, username: String): Result<User> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: return Result.failure(Exception("Registration failed"))
            val user = User(
                id = firebaseUser.uid,
                email = email,
                username = username
            )
            firestore.collection("users").document(firebaseUser.uid).set(user).await()
            firebaseUser.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(username).build()).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithEmail(email: String, password: String): Result<User> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: return Result.failure(Exception("Login failed"))
            val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
            val user = userDoc.toObject(User::class.java) ?: return Result.failure(Exception("User not found"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithGoogle(context: android.content.Context): Result<User> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("649660438917-4j5apk4jtcq18rim85nira1c37blplng.apps.googleusercontent.com")
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credential = withContext(Dispatchers.Main) {
                credentialManager.getCredential(context, request).credential
            }
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()
            val firebaseUser = authResult.user ?: return Result.failure(Exception("Google Sign-In failed"))

            val user = User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                username = firebaseUser.displayName ?: ""
            )
            firestore.collection("users").document(firebaseUser.uid).set(user).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser ?: return null
        val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
        return userDoc.toObject(User::class.java)
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
    }
}