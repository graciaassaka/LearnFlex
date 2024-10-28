package org.example.shared.data.firebase

import dev.gitlive.firebase.auth.AuthResult
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
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

    @Before
    fun setUp() {
        // Initialize MockK objects
        firebaseAuth = mockk(relaxed = true)
        mockUser = mockk(relaxed = true)
        mockAuthResult = mockk(relaxed = true)

        // Initialize the service with mocked FirebaseAuth
        firebaseAuthService = FirebaseAuthService(firebaseAuth)
    }

    // Test for signUp success
    @Test
    fun `signUp should call createUserWithEmailAndPassword and sendEmailVerification on success`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"

        // Mock behavior
        coEvery { firebaseAuth.createUserWithEmailAndPassword(email, password) } returns mockAuthResult
        every { mockAuthResult.user } returns mockUser
        coEvery { mockUser.sendEmailVerification() } just Runs

        // When
        val result = firebaseAuthService.signUp(email, password)

        // Then
        coVerify(exactly = 1) { firebaseAuth.createUserWithEmailAndPassword(email, password) }
        coVerify(exactly = 1) { mockUser.sendEmailVerification() }

        assertTrue(result.isSuccess)
    }

    // Test for signUp failure
    @Test
    fun `signUp should return failure when createUserWithEmailAndPassword throws exception`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"
        val exception = Exception("Sign up failed")

        // Mock behavior to throw exception
        coEvery { firebaseAuth.createUserWithEmailAndPassword(email, password) } throws exception

        // When
        val result = firebaseAuthService.signUp(email, password)

        // Then
        coVerify(exactly = 1) { firebaseAuth.createUserWithEmailAndPassword(email, password) }
        // sendEmailVerification should not be called
        coVerify(exactly = 0) { mockUser.sendEmailVerification() }

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
        coEvery { firebaseAuth.signInWithEmailAndPassword(email, password) } returns mockAuthResult

        // When
        val result = firebaseAuthService.signIn(email, password)

        // Then
        coVerify(exactly = 1) { firebaseAuth.signInWithEmailAndPassword(email, password) }

        assertTrue(result.isSuccess)
    }

    // Test for signIn failure
    @Test
    fun `signIn should return failure when signInWithEmailAndPassword throws exception`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"
        val exception = Exception("Sign in failed")

        // Mock behavior to throw exception
        coEvery { firebaseAuth.signInWithEmailAndPassword(email, password) } throws exception

        // When
        val result = firebaseAuthService.signIn(email, password)

        // Then
        coVerify(exactly = 1) { firebaseAuth.signInWithEmailAndPassword(email, password) }

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    // Test for signOut
    @Test
    fun `signOut should call firebaseAuth signOut`() = runTest {
        // Given
        coEvery { firebaseAuth.signOut() } just Runs

        // When
        firebaseAuthService.signOut()

        // Then
        coVerify(exactly = 1) { firebaseAuth.signOut() }
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

        // Mock currentUser
        every { firebaseAuth.currentUser } returns mockUser
        // Mock user properties
        every { mockUser.displayName } returns displayName
        every { mockUser.email } returns email
        every { mockUser.photoURL } returns photoUrl
        every { mockUser.isEmailVerified } returns isEmailVerified
        every { mockUser.uid } returns uid

        // Mock reload
        coEvery { mockUser.reload() } just Runs

        // When
        val result = firebaseAuthService.getUserData()

        // Then
        verify { firebaseAuth.currentUser }
        coVerify { mockUser.reload() }

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
        coVerify(exactly = 0) { mockUser.reload() }

        assertTrue(result.isFailure)
        assertEquals("No signed in user", result.exceptionOrNull()?.message)
    }

    // Test for getUserData failure when reload throws exception
    @Test
    fun `getUserData should return failure when reload throws exception`() = runTest {
        // Given
        every { firebaseAuth.currentUser } returns mockUser
        // Mock reload to throw exception
        coEvery { mockUser.reload() } throws Exception("Reload failed")

        // When
        val result = firebaseAuthService.getUserData()

        // Then
        verify { firebaseAuth.currentUser }
        coVerify { mockUser.reload() }

        assertTrue(result.isFailure)
        assertEquals("Reload failed", result.exceptionOrNull()?.message)
    }

    // Test for sendEmailVerification success
    @Test
    fun `sendEmailVerification should call sendEmailVerification on current user`() = runTest {
        // Given
        every { firebaseAuth.currentUser } returns mockUser
        coEvery { mockUser.sendEmailVerification() } just Runs

        // When
        val result = firebaseAuthService.sendEmailVerification()

        // Then
        verify { firebaseAuth.currentUser }
        coVerify(exactly = 1) { mockUser.sendEmailVerification() }

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
        coVerify(exactly = 0) { mockUser.sendEmailVerification() }

        assertTrue(result.isFailure)
        assertEquals("No signed in user", result.exceptionOrNull()?.message)
    }

    // Test for sendEmailVerification failure when sendEmailVerification throws exception
    @Test
    fun `sendEmailVerification should return failure when sendEmailVerification throws exception`() = runTest {
        // Given
        every { firebaseAuth.currentUser } returns mockUser
        val exception = Exception("Verification failed")
        coEvery { mockUser.sendEmailVerification() } throws exception

        // When
        val result = firebaseAuthService.sendEmailVerification()

        // Then
        verify { firebaseAuth.currentUser }
        coVerify(exactly = 1) { mockUser.sendEmailVerification() }

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    // Test for sendPasswordResetEmail success
    @Test
    fun `sendPasswordResetEmail should call firebaseAuth sendPasswordResetEmail and return success`() = runTest {
        // Given
        val email = "test@example.com"
        coEvery { firebaseAuth.sendPasswordResetEmail(email) } just Runs

        // When
        val result = firebaseAuthService.sendPasswordResetEmail(email)

        // Then
        coVerify(exactly = 1) { firebaseAuth.sendPasswordResetEmail(email) }

        assertTrue(result.isSuccess)
    }

    // Test for sendPasswordResetEmail failure
    @Test
    fun `sendPasswordResetEmail should return failure when sendPasswordResetEmail throws exception`() = runTest {
        // Given
        val email = "test@example.com"
        val exception = Exception("Password reset failed")
        coEvery { firebaseAuth.sendPasswordResetEmail(email) } throws exception

        // When
        val result = firebaseAuthService.sendPasswordResetEmail(email)

        // Then
        coVerify(exactly = 1) { firebaseAuth.sendPasswordResetEmail(email) }

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    // Test for deleteUser success
    @Test
    fun `deleteUser should call delete on current user and return success`() = runTest {
        // Given
        every { firebaseAuth.currentUser } returns mockUser
        coEvery { mockUser.delete() } just Runs

        // When
        val result = firebaseAuthService.deleteUser()

        // Then
        verify { firebaseAuth.currentUser }
        coVerify(exactly = 1) { mockUser.delete() }

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
        coVerify(exactly = 0) { mockUser.delete() }

        assertTrue(result.isFailure)
        assertEquals("No signed in user", result.exceptionOrNull()?.message)
    }

    // Test for deleteUser failure when delete throws exception
    @Test
    fun `deleteUser should return failure when delete throws exception`() = runTest {
        // Given
        every { firebaseAuth.currentUser } returns mockUser
        val exception = Exception("Delete failed")
        coEvery { mockUser.delete() } throws exception

        // When
        val result = firebaseAuthService.deleteUser()

        // Then
        verify { firebaseAuth.currentUser }
        coVerify(exactly = 1) { mockUser.delete() }

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
