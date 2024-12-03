package org.example.shared.injection

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import org.example.shared.data.local.dao.CurriculumLocalDao
import org.example.shared.data.local.dao.LessonLocalDao
import org.example.shared.data.local.dao.UserProfileDao
import org.example.shared.data.local.database.LearnFlexDatabase
import org.example.shared.data.local.entity.CurriculumEntity
import org.example.shared.data.local.entity.LessonEntity
import org.example.shared.data.local.entity.UserProfileEntity
import org.example.shared.data.remote.assistant.OpenAIAssistantClient
import org.example.shared.data.remote.assistant.StyleQuizClientImpl
import org.example.shared.data.remote.custom_search.GoogleImageSearchClient
import org.example.shared.data.remote.firebase.FirebaseAuthClient
import org.example.shared.data.remote.firebase.FirebaseStorageClient
import org.example.shared.data.remote.firestore.FirestoreBaseDao
import org.example.shared.data.remote.firestore.FirestoreExtendedDao
import org.example.shared.data.remote.firestore.FirestorePathBuilder
import org.example.shared.data.remote.util.HttpClientConfig
import org.example.shared.data.repository.component.BatchRepositoryComponent
import org.example.shared.data.repository.component.CrudRepositoryComponent
import org.example.shared.data.repository.component.QueryByScoreRepositoryComponent
import org.example.shared.data.repository.component.QueryByStatusRepositoryComponent
import org.example.shared.data.repository.util.QueryStrategies
import org.example.shared.data.repository.util.RepositoryConfig
import org.example.shared.data.sync.handler.SyncHandlerDelegate
import org.example.shared.data.sync.manager.SyncManagerImpl
import org.example.shared.data.util.GoogleConstants
import org.example.shared.data.util.OpenAIConstants
import org.example.shared.domain.client.*
import org.example.shared.domain.dao.ExtendedRemoteDao
import org.example.shared.domain.dao.RemoteDao
import org.example.shared.domain.dao.util.PathBuilder
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.model.Lesson
import org.example.shared.domain.model.UserProfile
import org.example.shared.domain.repository.CurriculumRepository
import org.example.shared.domain.repository.LessonRepository
import org.example.shared.domain.repository.UserProfileRepository
import org.example.shared.domain.repository.util.ModelMapper
import org.example.shared.domain.storage_operations.BatchOperations
import org.example.shared.domain.storage_operations.CrudOperations
import org.example.shared.domain.storage_operations.QueryByScoreOperation
import org.example.shared.domain.storage_operations.QueryByStatusOperation
import org.example.shared.domain.sync.SyncHandler
import org.example.shared.domain.sync.SyncManager
import org.example.shared.domain.use_case.*
import org.example.shared.presentation.viewModel.AuthViewModel
import org.example.shared.presentation.viewModel.BaseViewModel
import org.example.shared.presentation.viewModel.CreateUserProfileViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

// Platform-specific module declarations
expect fun initKoin(context: Any?)
expect fun getDispatcherModule(): Module
expect fun getDatabaseModule(): Module
expect fun getFirebaseAuthServiceModule(): Module
expect fun getFirebaseStorageServiceModule(): Module

// Qualifier constants
private const val USER_PROFILE_SCOPE = "user_profile_scope"
private const val CURRICULUM_SCOPE = "curriculum_scope"
private const val LESSON_SCOPE = "lesson_scope"

val commonModule = module {
    // Core Dependencies
    single {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).also { scope ->
            Runtime.getRuntime().addShutdownHook(Thread { scope.cancel() })
        }
    }
    single { SharingStarted.WhileSubscribed(5000) }

    // Include Platform-Specific Modules
    includes(
        getDispatcherModule(),
        getDatabaseModule(),
        getFirebaseAuthServiceModule(),
        getFirebaseStorageServiceModule()
    )

    // HTTP Client Configuration
    single {
        HttpClientConfig.create().also { client ->
            Runtime.getRuntime().addShutdownHook(Thread { client.close() })
        }
    }

    // Firebase Clients
    single<FirebaseFirestore> { Firebase.firestore }
    single<AuthClient> { get<FirebaseAuthClient>() }
    single<StorageClient> { get<FirebaseStorageClient>() }

    // OpenAI Client
    single<AIAssistantClient> {
        OpenAIAssistantClient(
            httpClient = get(),
            baseUrl = URLBuilder(
                protocol = URLProtocol.HTTPS,
                host = "api.openai.com"
            ).build(),
            apiKey = OpenAIConstants.API_KEY
        )
    }

    // Google Image Search Client
    single<ImageSearchClient> {
        GoogleImageSearchClient(
            httpClient = get(),
            baseUrl = URLBuilder(
                protocol = URLProtocol.HTTPS,
                host = "www.googleapis.com",
                pathSegments = listOf("customsearch", "v1")
            ).build(),
            apiKey = GoogleConstants.CUSTOM_SEARCH_API_KEY,
            searchEngineId = GoogleConstants.CUSTOM_IMAGES_SEARCH_ENGINE_ID
        )
    }

    // Style Quiz Service
    single<StyleQuizClient> { StyleQuizClientImpl(assistant = get()) }

    // Path Builder
    single<PathBuilder> { FirestorePathBuilder() }

    // Database DAOs
    single<UserProfileDao>(named(USER_PROFILE_SCOPE)) {
        get<LearnFlexDatabase>().userProfileDao()
    }

    single<CurriculumLocalDao>(named(CURRICULUM_SCOPE)) {
        get<LearnFlexDatabase>().curriculumDao()
    }

    single<LessonLocalDao>(named(LESSON_SCOPE)) {
        get<LearnFlexDatabase>().lessonDao()
    }

    // Remote DAOs
    single<RemoteDao<UserProfile>> {
        object : FirestoreBaseDao<UserProfile>(
            firestore = get(),
            serializer = UserProfile.serializer()
        ) {}
    }

    single<ExtendedRemoteDao<Curriculum>>(named(CURRICULUM_SCOPE)) {
        object : FirestoreExtendedDao<Curriculum>(
            firestore = get(),
            serializer = Curriculum.serializer()
        ) {}
    }

    single<ExtendedRemoteDao<Lesson>>(named(LESSON_SCOPE)) {
        object : FirestoreExtendedDao<Lesson>(
            firestore = get(),
            serializer = Lesson.serializer()
        ) {}
    }

    // Model Mappers
    single<ModelMapper<UserProfile, UserProfileEntity>>(named(USER_PROFILE_SCOPE)) {
        object : ModelMapper<UserProfile, UserProfileEntity> {
            override fun toModel(entity: UserProfileEntity) = with(entity) {
                UserProfile(id, username, email, photoUrl, preferences, learningStyle, createdAt, lastUpdated)
            }

            override fun toEntity(model: UserProfile, parentId: String?) = with(model) {
                UserProfileEntity(id, username, email, photoUrl, preferences, learningStyle, createdAt, lastUpdated)
            }
        }
    }

    single<ModelMapper<Curriculum, CurriculumEntity>>(named(CURRICULUM_SCOPE)) {
        object : ModelMapper<Curriculum, CurriculumEntity> {
            override fun toModel(entity: CurriculumEntity) = with(entity) {
                Curriculum(id, imageUrl, syllabus, description, status, createdAt, lastUpdated)
            }

            override fun toEntity(model: Curriculum, parentId: String?) = with(model) {
                require(parentId != null) { "Parent ID must not be null for CurriculumEntity" }
                CurriculumEntity(id, parentId, imageUrl, syllabus, description, status, createdAt, lastUpdated)
            }
        }
    }

    single<ModelMapper<Lesson, LessonEntity>>(named(LESSON_SCOPE)) {
        object : ModelMapper<Lesson, LessonEntity> {
            override fun toModel(entity: LessonEntity) = with(entity) {
                Lesson(id, imageUrl, title, description, index, quizScore, createdAt, lastUpdated)
            }

            override fun toEntity(model: Lesson, parentId: String?) = with(model) {
                require(parentId != null) { "Parent ID must not be null for LessonEntity" }
                LessonEntity(id, parentId, imageUrl, title, description, index, quizScore, createdAt, lastUpdated)
            }
        }
    }

    // Sync Handlers
    single<SyncHandler<UserProfile>>(named(USER_PROFILE_SCOPE)) {
        object : SyncHandler<UserProfile> by SyncHandlerDelegate(
            remoteDao = get(named(USER_PROFILE_SCOPE)),
            localDao = get<UserProfileDao>(named(USER_PROFILE_SCOPE)),
            modelMapper = get(named(USER_PROFILE_SCOPE))
        ) {}
    }

    single<SyncHandler<Curriculum>>(named(CURRICULUM_SCOPE)) {
        object : SyncHandler<Curriculum> by SyncHandlerDelegate(
            remoteDao = get(named(CURRICULUM_SCOPE)),
            localDao = get<CurriculumLocalDao>(named(CURRICULUM_SCOPE)),
            modelMapper = get(named(CURRICULUM_SCOPE))
        ) {}
    }

    single<SyncHandler<Lesson>>(named(LESSON_SCOPE)) {
        object : SyncHandler<Lesson> by SyncHandlerDelegate(
            remoteDao = get(named(LESSON_SCOPE)),
            localDao = get<LessonLocalDao>(named(LESSON_SCOPE)),
            modelMapper = get(named(LESSON_SCOPE))
        ) {}
    }

    // Sync Managers
    single<SyncManager<UserProfile>>(named(USER_PROFILE_SCOPE)) {
        SyncManagerImpl(
            syncScope = get(),
            syncHandler = get<SyncHandler<UserProfile>>(named(USER_PROFILE_SCOPE)),
            maxRetries = 3
        ).also { syncManager ->
            Runtime.getRuntime().addShutdownHook(Thread { syncManager.close() })
        }
    }

    single<SyncManager<Curriculum>>(named(CURRICULUM_SCOPE)) {
        SyncManagerImpl(
            syncScope = get(),
            syncHandler = get<SyncHandler<Curriculum>>(named(CURRICULUM_SCOPE)),
            maxRetries = 3
        ).also { syncManager ->
            Runtime.getRuntime().addShutdownHook(Thread { syncManager.close() })
        }
    }

    single<SyncManager<Lesson>>(named(LESSON_SCOPE)) {
        SyncManagerImpl(
            syncScope = get(),
            syncHandler = get<SyncHandler<Lesson>>(named(LESSON_SCOPE)),
            maxRetries = 3
        ).also { syncManager ->
            Runtime.getRuntime().addShutdownHook(Thread { syncManager.close() })
        }
    }

    // Repository Components Configuration
    single<RepositoryConfig<UserProfile, UserProfileEntity>>(named(USER_PROFILE_SCOPE)) {
        RepositoryConfig(
            remoteDao = get(),
            localDao = get<UserProfileDao>(named(USER_PROFILE_SCOPE)),
            modelMapper = get(named(USER_PROFILE_SCOPE)),
            syncManager = get(named(USER_PROFILE_SCOPE)),
            queryStrategies = QueryStrategies<UserProfileEntity>().apply {
                withGetById { id -> get<UserProfileDao>(named(USER_PROFILE_SCOPE)).get(id) }
            }
        )
    }

    single<RepositoryConfig<Curriculum, CurriculumEntity>>(named(CURRICULUM_SCOPE)) {
        RepositoryConfig(
            remoteDao = get(named(CURRICULUM_SCOPE)),
            localDao = get<CurriculumLocalDao>(named(CURRICULUM_SCOPE)),
            modelMapper = get(named(CURRICULUM_SCOPE)),
            syncManager = get(named(CURRICULUM_SCOPE)),
            queryStrategies = QueryStrategies<CurriculumEntity>().apply {
                withGetById { id ->
                    get<CurriculumLocalDao>(named(CURRICULUM_SCOPE)).get(id)
                }
                withGetAll {
                    get<CurriculumLocalDao>(named(CURRICULUM_SCOPE)).getAll()
                }
                withCustomQuery(
                    QueryByStatusRepositoryComponent.STATUS_STRATEGY_KEY,
                    QueryByStatusRepositoryComponent.StatusQueryStrategy { status ->
                        get<CurriculumLocalDao>(named(CURRICULUM_SCOPE))
                            .getCurriculaByStatus(status)
                    }
                )
            }
        )
    }

    single<RepositoryConfig<Lesson, LessonEntity>>(named(LESSON_SCOPE)) {
        RepositoryConfig(
            remoteDao = get(named(LESSON_SCOPE)),
            localDao = get<LessonLocalDao>(named(LESSON_SCOPE)),
            modelMapper = get(named(LESSON_SCOPE)),
            syncManager = get(named(LESSON_SCOPE)),
            queryStrategies = QueryStrategies<LessonEntity>().apply {
                withGetById { id ->
                    get<LessonLocalDao>(named(LESSON_SCOPE)).get(id)
                }
                withGetAll { moduleId ->
                    requireNotNull(moduleId) { "Module ID must not be null for LessonEntity" }
                    get<LessonLocalDao>(named(LESSON_SCOPE)).getLessonsByModuleId(moduleId)
                }
                withCustomQuery(
                    QueryByScoreRepositoryComponent.SCORE_STRATEGY_KEY,
                    QueryByScoreRepositoryComponent.ScoreQueryStrategy { moduleId, score ->
                        get<LessonLocalDao>(named(LESSON_SCOPE)).getLessonIdsByMinQuizScore(moduleId, score)
                    }
                )
            }
        )
    }

    // Repositories
    single<UserProfileRepository>(named(USER_PROFILE_SCOPE)) {
        object :
            UserProfileRepository,
            CrudOperations<UserProfile> by CrudRepositoryComponent(
                get<RepositoryConfig<UserProfile, UserProfileEntity>>((named(USER_PROFILE_SCOPE)))
            ) {}
    }

    single<CurriculumRepository>(named(CURRICULUM_SCOPE)) {
        object :
            CurriculumRepository,
            CrudOperations<Curriculum> by CrudRepositoryComponent(
                get<RepositoryConfig<Curriculum, CurriculumEntity>>((named(CURRICULUM_SCOPE)))
            ),
            QueryByStatusOperation<Curriculum> by QueryByStatusRepositoryComponent(
                get<RepositoryConfig<Curriculum, CurriculumEntity>>((named(CURRICULUM_SCOPE)))
            ),
            BatchOperations<Curriculum> by BatchRepositoryComponent(
                get<RepositoryConfig<Curriculum, CurriculumEntity>>((named(CURRICULUM_SCOPE)))
            ) {}
    }

    single<LessonRepository>(named(LESSON_SCOPE)) {
        object :
            LessonRepository,
            CrudOperations<Lesson> by CrudRepositoryComponent(
                get<RepositoryConfig<Lesson, LessonEntity>>((named(LESSON_SCOPE)))
            ),
            QueryByScoreOperation<Lesson> by QueryByScoreRepositoryComponent(
                get<RepositoryConfig<Lesson, LessonEntity>>((named(LESSON_SCOPE)))
            ),
            BatchOperations<Lesson> by BatchRepositoryComponent(
                get<RepositoryConfig<Lesson, LessonEntity>>((named(LESSON_SCOPE)))
            ) {}
    }

    // Use Cases - Auth
    singleOf(::SignUpUseCase)
    singleOf(::SignInUseCase)
    singleOf(::GetUserDataUseCase)
    singleOf(::SendVerificationEmailUseCase)
    singleOf(::VerifyEmailUseCase)
    singleOf(::DeleteUserUseCase)
    singleOf(::SendPasswordResetEmailUseCase)

    // Use Cases - User Profile
    single {
        CreateUserProfileUseCase(
            repository = get(named(USER_PROFILE_SCOPE)),
            authClient = get()
        )
    }
    single {
        UpdateUserProfileUseCase(
            repository = get(named(USER_PROFILE_SCOPE)),
            authClient = get()
        )
    }
    singleOf(::UploadProfilePictureUseCase)
    singleOf(::DeleteProfilePictureUseCase)
    singleOf(::GetStyleQuestionnaireUseCase)
    singleOf(::GetStyleResultUseCase)

    // Use Cases - Curriculum
    singleOf(::UploadCurriculumUseCase)
    singleOf(::UpdateCurriculumUseCase)
    singleOf(::DeleteCurriculumUseCase)
    singleOf(::GetCurriculumUseCase)
    singleOf(::GetAllCurriculaUseCase)
    singleOf(::GetCurriculaByStatusUseCase)

    // ViewModels
    viewModel { BaseViewModel(dispatcher = get()) }

    viewModel {
        AuthViewModel(
            signUpUseCase = get(),
            signInUseCase = get(),
            sendVerificationEmailUseCase = get(),
            verifyEmailUseCase = get(),
            deleteUserUseCase = get(),
            sendPasswordResetEmailUseCase = get(),
            dispatcher = get()
        )
    }

    viewModel {
        CreateUserProfileViewModel(
            getUserDataUseCase = get(),
            createUserProfileUseCase = get(),
            uploadProfilePictureUseCase = get(),
            deleteProfilePictureUseCase = get(),
            getStyleQuestionnaireUseCase = get(),
            getStyleResultUseCase = get(),
            updateUserProfileUseCase = get(),
            pathBuilder = get(),
            syncManager = get(named(USER_PROFILE_SCOPE)),
            dispatcher = get(),
            sharingStarted = get()
        )
    }
}