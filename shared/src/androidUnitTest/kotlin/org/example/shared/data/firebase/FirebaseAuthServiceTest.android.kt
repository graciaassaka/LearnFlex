package org.example.shared.data.firebase

import android.net.Uri
import android.text.TextUtils
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.example.shared.data.model.User
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
actual class FirebaseAuthServiceTest {

    private lateinit var firebaseAuthService: FirebaseAuthService
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mockAuthResult: AuthResult
    private lateinit var mockUser: FirebaseUser
    private lateinit var mockBuilder: UserProfileChangeRequest.Builder
    private lateinit var mockRequest: UserProfileChangeRequest

    @Before
    fun setUp() {
        // Initialize MockK objects
        firebaseAuth = mockk(relaxed = true)
        mockUser = mockk(relaxed = true)
        mockAuthResult = mockk(relaxed = true)

        // Initialize the service with mocked FirebaseAuth
        firebaseAuthService = FirebaseAuthService(firebaseAuth)
    }

    // Helper function to create a successful Task with a result
    private fun <T> createSuccessfulTask(result: T): Task<T> = Tasks.forResult(result)

    // Helper function to create a failed Task with an exception
    private fun <T> createFailedTask(exception: Exception): Task<T> = Tasks.forException(exception)

    // Helper function to create a successful Task<Void>
    private fun createSuccessfulVoidTask(): Task<Void> = Tasks.forResult(null)

    // Helper function to create a failed Task<Void>
    private fun createFailedVoidTask(exception: Exception): Task<Void> = Tasks.forException(exception)

    // Test for signUp success
    @Test
    fun `signUp should call createUserWithEmailAndPassword and sendEmailVerification on success`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"

        // Mock behavior
        every { firebaseAuth.createUserWithEmailAndPassword(email, password) } returns createSuccessfulTask(mockAuthResult)
        every { mockAuthResult.user } returns mockUser
        every { mockUser.sendEmailVerification() } returns createSuccessfulVoidTask()

        // When
        val result = firebaseAuthService.signUp(email, password)

        // Then
        verify(exactly = 1) { firebaseAuth.createUserWithEmailAndPassword(email, password) }
        verify(exactly = 1) { mockUser.sendEmailVerification() }

        assertTrue(result.isSuccess)
    }

    // Test for signUp failure
    @Test
    fun `signUp should return failure when createUserWithEmailAndPassword fails`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"
        val exception = Exception("Sign up failed")

        // Mock behavior to throw exception
        every { firebaseAuth.createUserWithEmailAndPassword(email, password) } returns createFailedTask<AuthResult>(exception)

        // When
        val result = firebaseAuthService.signUp(email, password)

        // Then
        verify(exactly = 1) { firebaseAuth.createUserWithEmailAndPassword(email, password) }
        // sendEmailVerification should not be called
        verify(exactly = 0) { mockUser.sendEmailVerification() }

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    // Test for signIn success
    @Test
    fun `signIn should call signInWithEmailAndPassword and return success`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"

        // Mock behavior
        every { firebaseAuth.signInWithEmailAndPassword(email, password) } returns createSuccessfulTask(mockAuthResult)

        // When
        val result = firebaseAuthService.signIn(email, password)

        // Then
        verify(exactly = 1) { firebaseAuth.signInWithEmailAndPassword(email, password) }

        assertTrue(result.isSuccess)
    }

    // Test for signIn failure
    @Test
    fun `signIn should return failure when signInWithEmailAndPassword fails`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"
        val exception = Exception("Sign in failed")

        // Mock behavior to throw exception
        every { firebaseAuth.signInWithEmailAndPassword(email, password) } returns createFailedTask<AuthResult>(exception)

        // When
        val result = firebaseAuthService.signIn(email, password)

        // Then
        verify(exactly = 1) { firebaseAuth.signInWithEmailAndPassword(email, password) }

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    // Test for signOut
    @Test
    fun `signOut should call firebaseAuth signOut`() = runTest {
        // Given
        every { firebaseAuth.signOut() } just Runs

        // When
        firebaseAuthService.signOut()

        // Then
        verify(exactly = 1) { firebaseAuth.signOut() }
    }

    // Test for getUserData success
    @Test
    fun `getUserData should return user data when user is signed in`() = runTest {
        // Given
        val displayName = "Test User"
        val email = "test@example.com"
        val photoUrl = "https://example.com/photo.jpg"
        val isEmailVerified = true
        val uid = "user123"

        // Create a mock Uri that returns the desired photoUrl when toString() is called
        val mockUri = mockk<Uri>()
        every { mockUri.toString() } returns photoUrl

        // Mock currentUser
        every { firebaseAuth.currentUser } returns mockUser
        // Mock user properties
        every { mockUser.displayName } returns displayName
        every { mockUser.email } returns email
        every { mockUser.photoUrl } returns mockUri
        every { mockUser.isEmailVerified } returns isEmailVerified
        every { mockUser.uid } returns uid

        // Mock reload
        every { mockUser.reload() } returns createSuccessfulVoidTask()

        // When
        val result = firebaseAuthService.getUserData()

        // Then
        verify { firebaseAuth.currentUser }
        verify { mockUser.reload() }

        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertTrue(user is User)
        user.let {
            assertEquals(displayName, it.displayName)
            assertEquals(email, it.email)
            assertEquals(photoUrl, it.photoUrl)
            assertEquals(isEmailVerified, it.emailVerified)
            assertEquals(uid, it.uid)
        }
    }

    // Test for getUserData failure when no user is signed in
    @Test
    fun `getUserData should return failure when no user is signed in`() = runTest {
        // Given
        every { firebaseAuth.currentUser } returns null

        // When
        val result = firebaseAuthService.getUserData()

        // Then
        verify { firebaseAuth.currentUser }
        // reload and user properties should not be accessed
        verify(exactly = 0) { mockUser.reload() }

        assertTrue(result.isFailure)
        assertEquals("No signed in user", result.exceptionOrNull()?.message)
    }

    // Test for getUserData failure when reload throws exception
    @Test
    fun `getUserData should return failure when reload fails`() = runTest {
        // Given
        every { firebaseAuth.currentUser } returns mockUser
        // Mock reload to throw exception
        every { mockUser.reload() } returns createFailedVoidTask(Exception("Reload failed"))

        // When
        val result = firebaseAuthService.getUserData()

        // Then
        verify { firebaseAuth.currentUser }
        verify { mockUser.reload() }

        assertTrue(result.isFailure)
        assertEquals("Reload failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateUserData with valid user data completes successfully`() = runTest {
       TODO()
    }

    @Test
    fun `updateUserData with null user data returns failure`() = runTest {
       TODO()
    }

    // Test for sendEmailVerification success
    @Test
    fun `sendEmailVerification should call sendEmailVerification on current user`() = runTest {
        // Given
        every { firebaseAuth.currentUser } returns mockUser
        every { mockUser.sendEmailVerification() } returns createSuccessfulVoidTask()

        // When
        val result = firebaseAuthService.sendEmailVerification()

        // Then
        verify { firebaseAuth.currentUser }
        verify(exactly = 1) { mockUser.sendEmailVerification() }

        assertTrue(result.isSuccess)
    }

    // Test for sendEmailVerification failure when no user is signed in
    @Test
    fun `sendEmailVerification should return failure when no user is signed in`() = runTest {
        // Given
        every { firebaseAuth.currentUser } returns null

        // When
        val result = firebaseAuthService.sendEmailVerification()

        // Then
        verify { firebaseAuth.currentUser }
        verify(exactly = 0) { mockUser.sendEmailVerification() }

        assertTrue(result.isFailure)
        assertEquals("No signed in user", result.exceptionOrNull()?.message)
    }

    // Test for sendEmailVerification failure when sendEmailVerification throws exception
    @Test
    fun `sendEmailVerification should return failure when sendEmailVerification fails`() = runTest {
        // Given
        every { firebaseAuth.currentUser } returns mockUser
        val exception = Exception("Verification failed")
        every { mockUser.sendEmailVerification() } returns createFailedVoidTask(exception)

        // When
        val result = firebaseAuthService.sendEmailVerification()

        // Then
        verify { firebaseAuth.currentUser }
        verify(exactly = 1) { mockUser.sendEmailVerification() }

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    // Test for sendPasswordResetEmail success
    @Test
    fun `sendPasswordResetEmail should call firebaseAuth sendPasswordResetEmail and return success`() = runTest {
        // Given
        val email = "test@example.com"
        every { firebaseAuth.sendPasswordResetEmail(email) } returns createSuccessfulVoidTask()

        // When
        val result = firebaseAuthService.sendPasswordResetEmail(email)

        // Then
        verify(exactly = 1) { firebaseAuth.sendPasswordResetEmail(email) }

        assertTrue(result.isSuccess)
    }

    // Test for sendPasswordResetEmail failure
    @Test
    fun `sendPasswordResetEmail should return failure when sendPasswordResetEmail fails`() = runTest {
        // Given
        val email = "test@example.com"
        val exception = Exception("Password reset failed")
        every { firebaseAuth.sendPasswordResetEmail(email) } returns createFailedVoidTask(exception)

        // When
        val result = firebaseAuthService.sendPasswordResetEmail(email)

        // Then
        verify(exactly = 1) { firebaseAuth.sendPasswordResetEmail(email) }

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    // Test for deleteUser success
    @Test
    fun `deleteUser should call delete on current user and return success`() = runTest {
        // Given
        every { firebaseAuth.currentUser } returns mockUser
        every { mockUser.delete() } returns createSuccessfulVoidTask()

        // When
        val result = firebaseAuthService.deleteUser()

        // Then
        verify { firebaseAuth.currentUser }
        verify(exactly = 1) { mockUser.delete() }

        assertTrue(result.isSuccess)
    }

    // Test for deleteUser failure when no user is signed in
    @Test
    fun `deleteUser should return failure when no user is signed in`() = runTest {
        // Given
        every { firebaseAuth.currentUser } returns null

        // When
        val result = firebaseAuthService.deleteUser()

        // Then
        verify { firebaseAuth.currentUser }
        verify(exactly = 0) { mockUser.delete() }

        assertTrue(result.isFailure)
        assertEquals("No signed in user", result.exceptionOrNull()?.message)
    }

    // Test for deleteUser failure when delete throws exception
    @Test
    fun `deleteUser should return failure when delete fails`() = runTest {
        // Given
        every { firebaseAuth.currentUser } returns mockUser
        val exception = Exception("Delete failed")
        every { mockUser.delete() } returns createFailedVoidTask(exception)

        // When
        val result = firebaseAuthService.deleteUser()

        // Then
        verify { firebaseAuth.currentUser }
        verify(exactly = 1) { mockUser.delete() }

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}