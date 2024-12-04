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
import org.example.shared.data.local.dao.*
import org.example.shared.data.local.database.LearnFlexDatabase
import org.example.shared.data.local.entity.*
import org.example.shared.data.remote.assistant.OpenAIAssistantClient
import org.example.shared.data.remote.assistant.StyleQuizClientImpl
import org.example.shared.data.remote.custom_search.GoogleImageSearchClient
import org.example.shared.data.remote.firebase.FirebaseAuthClient
import org.example.shared.data.remote.firebase.FirebaseStorageClient
import org.example.shared.data.remote.firestore.FirestoreBaseDao
import org.example.shared.data.remote.firestore.FirestoreExtendedDao
import org.example.shared.data.remote.firestore.FirestorePathBuilder
import org.example.shared.data.remote.util.HttpClientConfig
import org.example.shared.data.repository.component.*
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
import org.example.shared.domain.model.*
import org.example.shared.domain.repository.*
import org.example.shared.domain.repository.util.ModelMapper
import org.example.shared.domain.storage_operations.*
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
import org.example.shared.domain.model.Module as ModuleModel

// Platform-specific module declarations
expect fun initKoin(context: Any?)
expect fun getDispatcherModule(): Module
expect fun getDatabaseModule(): Module
expect fun getFirebaseAuthServiceModule(): Module
expect fun getFirebaseStorageServiceModule(): Module

// Qualifier constants
private const val USER_PROFILE_SCOPE = "user_profile_scope"
private const val CURRICULUM_SCOPE = "curriculum_scope"
private const val MODULE_SCOPE = "module_scope"
private const val LESSON_SCOPE = "lesson_scope"
private const val SECTION_SCOPE = "section_scope"
private const val SESSION_SCOPE = "session_scope"

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

    single<ModuleLocalDao>(named(MODULE_SCOPE)) {
        get<LearnFlexDatabase>().moduleDao()
    }

    single<LessonLocalDao>(named(LESSON_SCOPE)) {
        get<LearnFlexDatabase>().lessonDao()
    }

    single<SectionLocalDao>(named(SECTION_SCOPE)) {
        get<LearnFlexDatabase>().sectionDao()
    }

    single<SessionLocalDao>(named(SESSION_SCOPE)) {
        get<LearnFlexDatabase>().sessionDao()
    }

    // Remote DAOs
    single<RemoteDao<UserProfile>>(named(USER_PROFILE_SCOPE)) {
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

    single<ExtendedRemoteDao<ModuleModel>>(named(MODULE_SCOPE)) {
        object : FirestoreExtendedDao<ModuleModel>(
            firestore = get(),
            serializer = ModuleModel.serializer()
        ) {}
    }

    single<ExtendedRemoteDao<Lesson>>(named(LESSON_SCOPE)) {
        object : FirestoreExtendedDao<Lesson>(
            firestore = get(),
            serializer = Lesson.serializer()
        ) {}
    }

    single<ExtendedRemoteDao<Section>>(named(SECTION_SCOPE)) {
        object : FirestoreExtendedDao<Section>(
            firestore = get(),
            serializer = Section.serializer()
        ) {}
    }

    single<ExtendedRemoteDao<Session>>(named(SESSION_SCOPE)) {
        object : FirestoreExtendedDao<Session>(
            firestore = get(),
            serializer = Session.serializer()
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

    single<ModelMapper<ModuleModel, ModuleEntity>>(named(MODULE_SCOPE)) {
        object : ModelMapper<ModuleModel, ModuleEntity> {
            override fun toModel(entity: ModuleEntity) = with(entity) {
                ModuleModel(id, imageUrl, title, description, index, quizScore, createdAt, lastUpdated)
            }

            override fun toEntity(model: ModuleModel, parentId: String?) = with(model) {
                require(parentId != null) { "Parent ID must not be null for ModuleEntity" }
                ModuleEntity(id, parentId, imageUrl, title, description, index, quizScore, createdAt, lastUpdated)
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

    single<ModelMapper<Section, SectionEntity>>(named(SECTION_SCOPE)) {
        object : ModelMapper<Section, SectionEntity> {
            override fun toModel(entity: SectionEntity) = with(entity) {
                Section(id, imageUrl, index, title, description, content, quizScore, createdAt, lastUpdated)
            }

            override fun toEntity(model: Section, parentId: String?) = with(model) {
                require(parentId != null) { "Parent ID must not be null for SectionEntity" }
                SectionEntity(
                    id,
                    parentId,
                    imageUrl,
                    index,
                    title,
                    description,
                    content,
                    quizScore,
                    createdAt,
                    lastUpdated
                )
            }
        }
    }

    single<ModelMapper<Session, SessionEntity>>(named(SESSION_SCOPE)) {
        object : ModelMapper<Session, SessionEntity> {
            override fun toModel(entity: SessionEntity) = with(entity) {
                Session(id, userId, endTime, durationMinutes, createdAt, lastUpdated)
            }

            override fun toEntity(model: Session, parentId: String?) = with(model) {
                require(parentId != null) { "Parent ID must not be null for SessionEntity" }
                SessionEntity(id, parentId, endTime, durationMinutes, createdAt, lastUpdated)
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

    single<SyncHandler<ModuleModel>>(named(MODULE_SCOPE)) {
        object : SyncHandler<ModuleModel> by SyncHandlerDelegate(
            remoteDao = get(named(MODULE_SCOPE)),
            localDao = get<ModuleLocalDao>(named(MODULE_SCOPE)),
            modelMapper = get(named(MODULE_SCOPE))
        ) {}
    }

    single<SyncHandler<Lesson>>(named(LESSON_SCOPE)) {
        object : SyncHandler<Lesson> by SyncHandlerDelegate(
            remoteDao = get(named(LESSON_SCOPE)),
            localDao = get<LessonLocalDao>(named(LESSON_SCOPE)),
            modelMapper = get(named(LESSON_SCOPE))
        ) {}
    }

    single<SyncHandler<Session>>(named(SESSION_SCOPE)) {
        object : SyncHandler<Session> by SyncHandlerDelegate(
            remoteDao = get(named(SESSION_SCOPE)),
            localDao = get<SessionLocalDao>(named(SESSION_SCOPE)),
            modelMapper = get(named(SESSION_SCOPE))
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

    single<SyncManager<ModuleModel>>(named(MODULE_SCOPE)) {
        SyncManagerImpl(
            syncScope = get(),
            syncHandler = get<SyncHandler<ModuleModel>>(named(MODULE_SCOPE)),
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

    single<SyncManager<Session>>(named(SESSION_SCOPE)) {
        SyncManagerImpl(
            syncScope = get(),
            syncHandler = get<SyncHandler<Session>>(named(SESSION_SCOPE)),
            maxRetries = 3
        ).also { syncManager ->
            Runtime.getRuntime().addShutdownHook(Thread { syncManager.close() })
        }
    }

    // Repository Components Configuration
    single<RepositoryConfig<UserProfile, UserProfileEntity>>(named(USER_PROFILE_SCOPE)) {
        RepositoryConfig(
            remoteDao = get<RemoteDao<UserProfile>>(named(USER_PROFILE_SCOPE)),
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

    single<RepositoryConfig<ModuleModel, ModuleEntity>>(named(MODULE_SCOPE)) {
        RepositoryConfig(
            remoteDao = get(named(MODULE_SCOPE)),
            localDao = get<ModuleLocalDao>(named(MODULE_SCOPE)),
            modelMapper = get(named(MODULE_SCOPE)),
            syncManager = get(named(MODULE_SCOPE)),
            queryStrategies = QueryStrategies<ModuleEntity>().apply {
                withGetById { id ->
                    get<ModuleLocalDao>(named(MODULE_SCOPE)).get(id)
                }
                withGetAll { curriculumId ->
                    requireNotNull(curriculumId) { "Curriculum ID must not be null for ModuleEntity" }
                    get<ModuleLocalDao>(named(MODULE_SCOPE)).getModulesByCurriculumId(curriculumId)
                }
                withCustomQuery(
                    QueryByScoreRepositoryComponent.SCORE_STRATEGY_KEY,
                    QueryByScoreRepositoryComponent.ScoreQueryStrategy { curriculumId, score ->
                        get<ModuleLocalDao>(named(MODULE_SCOPE)).getModuleIdsByMinQuizScore(curriculumId, score)
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

    single<RepositoryConfig<Section, SectionEntity>>(named(SECTION_SCOPE)) {
        RepositoryConfig(
            remoteDao = get(named(SECTION_SCOPE)),
            localDao = get<SectionLocalDao>(named(SECTION_SCOPE)),
            modelMapper = get(named(SECTION_SCOPE)),
            syncManager = get(named(SECTION_SCOPE)),
            queryStrategies = QueryStrategies<SectionEntity>().apply {
                withGetById { id ->
                    get<SectionLocalDao>(named(SECTION_SCOPE)).get(id)
                }
                withGetAll { lessonId ->
                    requireNotNull(lessonId) { "Lesson ID must not be null for SectionEntity" }
                    get<SectionLocalDao>(named(SECTION_SCOPE)).getSectionsByLessonId(lessonId)
                }
                withCustomQuery(
                    QueryByScoreRepositoryComponent.SCORE_STRATEGY_KEY,
                    QueryByScoreRepositoryComponent.ScoreQueryStrategy { lessonId, score ->
                        get<SectionLocalDao>(named(SECTION_SCOPE)).getSectionIdsByMinQuizScore(lessonId, score)
                    }
                )
            }
        )
    }

    single<RepositoryConfig<Session, SessionEntity>>(named(SESSION_SCOPE)) {
        RepositoryConfig(
            remoteDao = get(named(SESSION_SCOPE)),
            localDao = get<SessionLocalDao>(named(SESSION_SCOPE)),
            modelMapper = get(named(SESSION_SCOPE)),
            syncManager = get(named(SESSION_SCOPE)),
            queryStrategies = QueryStrategies<SessionEntity>().apply {
                withGetById { id ->
                    get<SessionLocalDao>(named(SESSION_SCOPE)).get(id)
                }
                withGetAll {
                    get<SessionLocalDao>(named(SESSION_SCOPE)).getAll()
                }
                withCustomQuery(
                    QueryByDateRangeRepositoryComponent.DATE_RANGE_QUERY_STRATEGY_KEY,
                    QueryByDateRangeRepositoryComponent.DateRangeQueryStrategy { startTime, endTime ->
                        get<SessionLocalDao>(named(SESSION_SCOPE)).getSessionsByDateRange(startTime, endTime)
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

    single<ModuleRepository>(named(MODULE_SCOPE)) {
        object :
            ModuleRepository,
            CrudOperations<ModuleModel> by CrudRepositoryComponent(
                get<RepositoryConfig<ModuleModel, ModuleEntity>>((named(MODULE_SCOPE)))
            ),
            BatchOperations<ModuleModel> by BatchRepositoryComponent(
                get<RepositoryConfig<ModuleModel, ModuleEntity>>((named(MODULE_SCOPE)))
            ),
            QueryByScoreOperation<ModuleModel> by QueryByScoreRepositoryComponent(
                get<RepositoryConfig<ModuleModel, ModuleEntity>>((named(MODULE_SCOPE)))
            ) {}
    }

    single<LessonRepository>(named(LESSON_SCOPE)) {
        object :
            LessonRepository,
            CrudOperations<Lesson> by CrudRepositoryComponent(
                get<RepositoryConfig<Lesson, LessonEntity>>((named(LESSON_SCOPE)))
            ),
            BatchOperations<Lesson> by BatchRepositoryComponent(
                get<RepositoryConfig<Lesson, LessonEntity>>((named(LESSON_SCOPE)))
            ),
            QueryByScoreOperation<Lesson> by QueryByScoreRepositoryComponent(
                get<RepositoryConfig<Lesson, LessonEntity>>((named(LESSON_SCOPE)))
            ) {}
    }

    single<SectionRepository>(named(SECTION_SCOPE)) {
        object :
            SectionRepository,
            CrudOperations<Section> by CrudRepositoryComponent(
                get<RepositoryConfig<Section, SectionEntity>>((named(SECTION_SCOPE)))
            ),
            BatchOperations<Section> by BatchRepositoryComponent(
                get<RepositoryConfig<Section, SectionEntity>>((named(SECTION_SCOPE)))
            ),
            QueryByScoreOperation<Section> by QueryByScoreRepositoryComponent(
                get<RepositoryConfig<Section, SectionEntity>>((named(SECTION_SCOPE)))
            ) {}
    }

    single<SessionRepository>(named(SESSION_SCOPE)) {
        object :
            SessionRepository,
            CrudOperations<Session> by CrudRepositoryComponent(
                get<RepositoryConfig<Session, SessionEntity>>((named(SESSION_SCOPE)))
            ),
            BatchOperations<Session> by BatchRepositoryComponent(
                get<RepositoryConfig<Session, SessionEntity>>((named(SESSION_SCOPE)))
            ),
            QueryByDateRangeOperation<Session> by QueryByDateRangeRepositoryComponent(
                get<RepositoryConfig<Session, SessionEntity>>((named(SESSION_SCOPE)))
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

    // Use Cases - Module
    singleOf(::UploadModuleUseCase)
    singleOf(::UpdateModuleUseCase)
    singleOf(::GetModuleUseCase)
    singleOf(::GetAllModulesUseCase)
    singleOf(::GetModuleIdsByMinQuizScoreUseCase)
    singleOf(::DeleteAllModulesUseCase)

    // Use Cases - Lesson
    singleOf(::UploadLessonUseCase)
    singleOf(::UpdateLessonUseCase)
    singleOf(::GetLessonUseCase)
    singleOf(::GetAllLessonsUseCase)
    singleOf(::GetLessonIdsByMinQuizScoreUseCase)
    singleOf(::DeleteAllLessonsUseCase)

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