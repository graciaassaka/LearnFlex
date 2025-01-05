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
import org.example.shared.data.local.dao.util.TimestampManager
import org.example.shared.data.local.dao.util.TimestampUpdater
import org.example.shared.data.local.database.LearnFlexDatabase
import org.example.shared.data.local.entity.*
import org.example.shared.data.remote.assistant.OpenAIAssistantClient
import org.example.shared.data.remote.assistant.generator.ContentGeneratorClientImpl
import org.example.shared.data.remote.assistant.generator.StyleQuizGeneratorClientImpl
import org.example.shared.data.remote.assistant.summarizer.SyllabusSummarizerClientImpl
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
import org.example.shared.data.util.OpenAIConstants
import org.example.shared.domain.client.*
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.dao.Dao
import org.example.shared.domain.dao.ExtendedDao
import org.example.shared.domain.model.*
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.repository.*
import org.example.shared.domain.repository.util.ModelMapper
import org.example.shared.domain.storage_operations.*
import org.example.shared.domain.storage_operations.util.PathBuilder
import org.example.shared.domain.sync.SyncHandler
import org.example.shared.domain.sync.SyncManager
import org.example.shared.domain.use_case.activity.GetWeeklyActivityUseCase
import org.example.shared.domain.use_case.auth.*
import org.example.shared.domain.use_case.curriculum.*
import org.example.shared.domain.use_case.lesson.*
import org.example.shared.domain.use_case.module.*
import org.example.shared.domain.use_case.path.*
import org.example.shared.domain.use_case.profile.*
import org.example.shared.domain.use_case.section.*
import org.example.shared.domain.use_case.session.GetAllSessionsUseCase
import org.example.shared.domain.use_case.session.GetSessionsByDateRangeUseCase
import org.example.shared.domain.use_case.session.UpdateSessionUseCase
import org.example.shared.domain.use_case.session.UploadSessionUseCase
import org.example.shared.domain.use_case.syllabus.SummarizeSyllabusUseCase
import org.example.shared.domain.use_case.validation.ValidateEmailUseCase
import org.example.shared.domain.use_case.validation.ValidatePasswordConfirmationUseCase
import org.example.shared.domain.use_case.validation.ValidatePasswordUseCase
import org.example.shared.domain.use_case.validation.ValidateUsernameUseCase
import org.example.shared.presentation.viewModel.*
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
        getDispatcherModule(), getDatabaseModule(), getFirebaseAuthServiceModule(), getFirebaseStorageServiceModule()
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
            httpClient = get(), baseUrl = URLBuilder(
                protocol = URLProtocol.HTTPS, host = "api.openai.com"
            ).build(), apiKey = OpenAIConstants.API_KEY
        )
    }

    // Style Quiz Client
    single<StyleQuizGeneratorClient> { StyleQuizGeneratorClientImpl(assistantClient = get(), assistantId = OpenAIConstants.STYLE_ASSISTANT_ID) }

    // Syllabus Summarizer Client
    single<SyllabusSummarizerClient> { SyllabusSummarizerClientImpl(assistantClient = get(), assistantId = OpenAIConstants.SYLLABUS_SUMMARIZER_ID) }

    // Content Generator Clients
    single<ContentGeneratorClient>(named(CURRICULUM_SCOPE)) {
        ContentGeneratorClientImpl(
            assistantClient = get(), assistantId = OpenAIConstants.CURRICULUM_GENERATOR_ID
        )
    }

    single<ContentGeneratorClient>(named(MODULE_SCOPE)) {
        ContentGeneratorClientImpl(
            assistantClient = get(), assistantId = OpenAIConstants.MODULE_GENERATOR_ID
        )
    }

    single<ContentGeneratorClient>(named(LESSON_SCOPE)) {
        ContentGeneratorClientImpl(
            assistantClient = get(), assistantId = OpenAIConstants.LESSON_GENERATOR_ID
        )
    }

    single<ContentGeneratorClient>(named(SECTION_SCOPE)) {
        ContentGeneratorClientImpl(
            assistantClient = get(), assistantId = OpenAIConstants.SECTION_GENERATOR_ID
        )
    }

    // Path Builder
    single<PathBuilder> { FirestorePathBuilder() }

    // Timestamp Updaters
    single<TimestampUpdater.ProfileTimestampUpdater>(named(USER_PROFILE_SCOPE)) {
        get<LearnFlexDatabase>().profileTimestampUpdater()
    }

    single<TimestampUpdater.CurriculumTimestampUpdater>(named(CURRICULUM_SCOPE)) {
        get<LearnFlexDatabase>().curriculumTimestampUpdater()
    }

    single<TimestampUpdater.ModuleTimestampUpdater>(named(MODULE_SCOPE)) {
        get<LearnFlexDatabase>().moduleTimestampUpdater()
    }

    single<TimestampUpdater.LessonTimestampUpdater>(named(LESSON_SCOPE)) {
        get<LearnFlexDatabase>().lessonTimestampUpdater()
    }

    single<TimestampUpdater.SectionTimestampUpdater>(named(SECTION_SCOPE)) {
        get<LearnFlexDatabase>().sectionTimestampUpdater()
    }

    single<TimestampUpdater.SessionTimestampUpdater>(named(SESSION_SCOPE)) {
        get<LearnFlexDatabase>().sessionTimestampUpdater()
    }

    // Updaters map
    single<Map<Collection, TimestampUpdater>> {
        mapOf(
            Collection.PROFILES to get<TimestampUpdater.ProfileTimestampUpdater>(named(USER_PROFILE_SCOPE)),
            Collection.CURRICULA to get<TimestampUpdater.CurriculumTimestampUpdater>(named(CURRICULUM_SCOPE)),
            Collection.MODULES to get<TimestampUpdater.ModuleTimestampUpdater>(named(MODULE_SCOPE)),
            Collection.LESSONS to get<TimestampUpdater.LessonTimestampUpdater>(named(LESSON_SCOPE)),
            Collection.SECTIONS to get<TimestampUpdater.SectionTimestampUpdater>(named(SECTION_SCOPE)),
            Collection.SESSIONS to get<TimestampUpdater.SessionTimestampUpdater>(named(SESSION_SCOPE))
        )
    }

    // Timestamp Manager
    single { TimestampManager(entityTimestampUpdaters = get()) }

    // Database DAOs
    single<ProfileDao>(named(USER_PROFILE_SCOPE)) {
        get<LearnFlexDatabase>().profileDao()
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
    single<Dao<Profile>>(named(USER_PROFILE_SCOPE)) {
        object : FirestoreBaseDao<Profile>(
            firestore = get(), serializer = Profile.serializer()
        ) {}
    }

    single<Dao<Curriculum>>(named(CURRICULUM_SCOPE)) {
        get<ExtendedDao<Curriculum>>(named(CURRICULUM_SCOPE))
    }

    single<ExtendedDao<Curriculum>>(named(CURRICULUM_SCOPE)) {
        object : FirestoreExtendedDao<Curriculum>(
            firestore = get(), serializer = Curriculum.serializer()
        ) {}
    }

    single<Dao<ModuleModel>>(named(MODULE_SCOPE)) {
        get<ExtendedDao<ModuleModel>>(named(MODULE_SCOPE))
    }

    single<ExtendedDao<ModuleModel>>(named(MODULE_SCOPE)) {
        object : FirestoreExtendedDao<ModuleModel>(
            firestore = get(), serializer = ModuleModel.serializer()
        ) {}
    }

    single<Dao<Lesson>>(named(LESSON_SCOPE)) {
        get<ExtendedDao<Lesson>>(named(LESSON_SCOPE))
    }

    single<ExtendedDao<Lesson>>(named(LESSON_SCOPE)) {
        object : FirestoreExtendedDao<Lesson>(
            firestore = get(), serializer = Lesson.serializer()
        ) {}
    }

    single<Dao<Section>>(named(SECTION_SCOPE)) {
        get<ExtendedDao<Section>>(named(SECTION_SCOPE))
    }

    single<ExtendedDao<Section>>(named(SECTION_SCOPE)) {
        object : FirestoreExtendedDao<Section>(
            firestore = get(), serializer = Section.serializer()
        ) {}
    }

    single<Dao<Session>>(named(SESSION_SCOPE)) {
        get<ExtendedDao<Session>>(named(SESSION_SCOPE))
    }

    single<ExtendedDao<Session>>(named(SESSION_SCOPE)) {
        object : FirestoreExtendedDao<Session>(
            firestore = get(), serializer = Session.serializer()
        ) {}
    }

    // Model Mappers
    single<ModelMapper<Profile, ProfileEntity>>(named(USER_PROFILE_SCOPE)) {
        object : ModelMapper<Profile, ProfileEntity> {
            override fun toModel(entity: ProfileEntity) = with(entity) {
                Profile(id, username, email, photoUrl, preferences, learningStyle, createdAt, lastUpdated)
            }

            override fun toEntity(model: Profile, parentId: String?) = with(model) {
                ProfileEntity(id, username, email, photoUrl, preferences, learningStyle, createdAt, lastUpdated)
            }
        }
    }

    single<ModelMapper<Curriculum, CurriculumEntity>>(named(CURRICULUM_SCOPE)) {
        object : ModelMapper<Curriculum, CurriculumEntity> {
            override fun toModel(entity: CurriculumEntity) = with(entity) {
                Curriculum(id, title, description, content, status, createdAt, lastUpdated)
            }

            override fun toEntity(model: Curriculum, parentId: String?) = with(model) {
                require(parentId != null) { "Parent ID must not be null for CurriculumEntity" }
                CurriculumEntity(id, parentId, title, description, content, status, createdAt, lastUpdated)
            }
        }
    }

    single<ModelMapper<ModuleModel, ModuleEntity>>(named(MODULE_SCOPE)) {
        object : ModelMapper<ModuleModel, ModuleEntity> {
            override fun toModel(entity: ModuleEntity) = with(entity) {
                ModuleModel(
                    id, title, description, index, content, quizScore, quizScoreMax, createdAt, lastUpdated
                )
            }

            override fun toEntity(model: ModuleModel, parentId: String?) = with(model) {
                require(parentId != null) { "Parent ID must not be null for ModuleEntity" }
                ModuleEntity(
                    id, parentId, title, description, index, content, quizScore, quizScoreMax, createdAt, lastUpdated
                )
            }
        }
    }

    single<ModelMapper<Lesson, LessonEntity>>(named(LESSON_SCOPE)) {
        object : ModelMapper<Lesson, LessonEntity> {
            override fun toModel(entity: LessonEntity) = with(entity) {
                Lesson(id, title, description, index, content, quizScore, quizScoreMax, createdAt, lastUpdated)
            }

            override fun toEntity(model: Lesson, parentId: String?) = with(model) {
                require(parentId != null) { "Parent ID must not be null for LessonEntity" }
                LessonEntity(
                    id, parentId, title, description, index, content, quizScore, quizScoreMax, createdAt, lastUpdated
                )
            }
        }
    }

    single<ModelMapper<Section, SectionEntity>>(named(SECTION_SCOPE)) {
        object : ModelMapper<Section, SectionEntity> {
            override fun toModel(entity: SectionEntity) = with(entity) {
                Section(
                    id, index, title, description, content, quizScore, quizScoreMax, createdAt, lastUpdated
                )
            }

            override fun toEntity(model: Section, parentId: String?) = with(model) {
                require(parentId != null) { "Parent ID must not be null for SectionEntity" }
                SectionEntity(
                    id, parentId, index, title, description, content, quizScore, quizScoreMax, createdAt, lastUpdated
                )
            }
        }
    }

    single<ModelMapper<Session, SessionEntity>>(named(SESSION_SCOPE)) {
        object : ModelMapper<Session, SessionEntity> {
            override fun toModel(entity: SessionEntity) = with(entity) {
                Session(id, endTime, createdAt, lastUpdated)
            }

            override fun toEntity(model: Session, parentId: String?) = with(model) {
                require(parentId != null) { "Parent ID must not be null for SessionEntity" }
                SessionEntity(id, parentId, endTime, createdAt, lastUpdated)
            }
        }
    }

    // Sync Handlers
    single<SyncHandler<Profile>>(named(USER_PROFILE_SCOPE)) {
        object : SyncHandler<Profile> by SyncHandlerDelegate(
            remoteDao = get(named(USER_PROFILE_SCOPE)),
            localDao = get<ProfileDao>(named(USER_PROFILE_SCOPE)),
            getStrategy = QueryStrategies.SingleEntityStrategyHolder { id -> get<ProfileDao>(named(USER_PROFILE_SCOPE)).get(id) },
            modelMapper = get(named(USER_PROFILE_SCOPE))
        ) {}
    }

    single<SyncHandler<Curriculum>>(named(CURRICULUM_SCOPE)) {
        object : SyncHandler<Curriculum> by SyncHandlerDelegate(
            remoteDao = get(named(CURRICULUM_SCOPE)),
            localDao = get<CurriculumLocalDao>(named(CURRICULUM_SCOPE)),
            getStrategy = QueryStrategies.SingleEntityStrategyHolder { id -> get<CurriculumLocalDao>(named(CURRICULUM_SCOPE)).get(id) },
            modelMapper = get(named(CURRICULUM_SCOPE))
        ) {}
    }

    single<SyncHandler<ModuleModel>>(named(MODULE_SCOPE)) {
        object : SyncHandler<ModuleModel> by SyncHandlerDelegate(
            remoteDao = get(named(MODULE_SCOPE)),
            localDao = get<ModuleLocalDao>(named(MODULE_SCOPE)),
            getStrategy = QueryStrategies.SingleEntityStrategyHolder { id -> get<ModuleLocalDao>(named(MODULE_SCOPE)).get(id) },
            modelMapper = get(named(MODULE_SCOPE))
        ) {}
    }

    single<SyncHandler<Lesson>>(named(LESSON_SCOPE)) {
        object : SyncHandler<Lesson> by SyncHandlerDelegate(
            remoteDao = get(named(LESSON_SCOPE)),
            localDao = get<LessonLocalDao>(named(LESSON_SCOPE)),
            getStrategy = QueryStrategies.SingleEntityStrategyHolder { id -> get<LessonLocalDao>(named(LESSON_SCOPE)).get(id) },
            modelMapper = get(named(LESSON_SCOPE))
        ) {}
    }

    single<SyncHandler<Section>>(named(SECTION_SCOPE)) {
        object : SyncHandler<Section> by SyncHandlerDelegate(
            remoteDao = get(named(SECTION_SCOPE)),
            localDao = get<SectionLocalDao>(named(SECTION_SCOPE)),
            getStrategy = QueryStrategies.SingleEntityStrategyHolder { id -> get<SectionLocalDao>(named(SECTION_SCOPE)).get(id) },
            modelMapper = get(named(SECTION_SCOPE))
        ) {}
    }

    single<SyncHandler<Session>>(named(SESSION_SCOPE)) {
        object : SyncHandler<Session> by SyncHandlerDelegate(
            remoteDao = get(named(SESSION_SCOPE)),
            localDao = get<SessionLocalDao>(named(SESSION_SCOPE)),
            getStrategy = QueryStrategies.SingleEntityStrategyHolder { id -> get<SessionLocalDao>(named(SESSION_SCOPE)).get(id) },
            modelMapper = get(named(SESSION_SCOPE))
        ) {}
    }

    // Sync Managers
    single<SyncManager<Profile>>(named(USER_PROFILE_SCOPE)) {
        SyncManagerImpl(
            syncScope = get(), syncHandler = get<SyncHandler<Profile>>(named(USER_PROFILE_SCOPE)), maxRetries = 3
        ).also { syncManager ->
            Runtime.getRuntime().addShutdownHook(Thread { syncManager.close() })
        }
    }

    single<SyncManager<Curriculum>>(named(CURRICULUM_SCOPE)) {
        SyncManagerImpl(
            syncScope = get(), syncHandler = get<SyncHandler<Curriculum>>(named(CURRICULUM_SCOPE)), maxRetries = 3
        ).also { syncManager ->
            Runtime.getRuntime().addShutdownHook(Thread { syncManager.close() })
        }
    }

    single<SyncManager<ModuleModel>>(named(MODULE_SCOPE)) {
        SyncManagerImpl(
            syncScope = get(), syncHandler = get<SyncHandler<ModuleModel>>(named(MODULE_SCOPE)), maxRetries = 3
        ).also { syncManager ->
            Runtime.getRuntime().addShutdownHook(Thread { syncManager.close() })
        }
    }

    single<SyncManager<Lesson>>(named(LESSON_SCOPE)) {
        SyncManagerImpl(
            syncScope = get(), syncHandler = get<SyncHandler<Lesson>>(named(LESSON_SCOPE)), maxRetries = 3
        ).also { syncManager ->
            Runtime.getRuntime().addShutdownHook(Thread { syncManager.close() })
        }
    }

    single<SyncManager<Section>>(named(SECTION_SCOPE)) {
        SyncManagerImpl(
            syncScope = get(), syncHandler = get<SyncHandler<Section>>(named(SECTION_SCOPE)), maxRetries = 3
        ).also { syncManager ->
            Runtime.getRuntime().addShutdownHook(Thread { syncManager.close() })
        }
    }

    single<SyncManager<Session>>(named(SESSION_SCOPE)) {
        SyncManagerImpl(
            syncScope = get(), syncHandler = get<SyncHandler<Session>>(named(SESSION_SCOPE)), maxRetries = 3
        ).also { syncManager ->
            Runtime.getRuntime().addShutdownHook(Thread { syncManager.close() })
        }
    }

    single<List<SyncManager<DatabaseRecord>>> {
        listOf(
            get(named(USER_PROFILE_SCOPE)),
            get(named(CURRICULUM_SCOPE)),
            get(named(MODULE_SCOPE)),
            get(named(LESSON_SCOPE)),
            get(named(SESSION_SCOPE))
        )
    }

    // Repository Components Configuration
    single<RepositoryConfig<Profile, ProfileEntity>>(named(USER_PROFILE_SCOPE)) {
        RepositoryConfig(
            collection = Collection.PROFILES,
            remoteDao = get<Dao<Profile>>(named(USER_PROFILE_SCOPE)),
            localDao = get<ProfileDao>(named(USER_PROFILE_SCOPE)),
            modelMapper = get(named(USER_PROFILE_SCOPE)),
            syncManager = get(named(USER_PROFILE_SCOPE)),
            queryStrategies = QueryStrategies<ProfileEntity>().apply {
                withGetById { id -> get<ProfileDao>(named(USER_PROFILE_SCOPE)).get(id) }
            })
    }

    single<RepositoryConfig<Curriculum, CurriculumEntity>>(named(CURRICULUM_SCOPE)) {
        RepositoryConfig(
            collection = Collection.CURRICULA,
            remoteDao = get<ExtendedDao<Curriculum>>(named(CURRICULUM_SCOPE)),
            localDao = get<CurriculumLocalDao>(named(CURRICULUM_SCOPE)),
            modelMapper = get<ModelMapper<Curriculum, CurriculumEntity>>(named(CURRICULUM_SCOPE)),
            syncManager = get<SyncManager<Curriculum>>(named(CURRICULUM_SCOPE)),
            queryStrategies = QueryStrategies<CurriculumEntity>().apply {
                withGetById { id ->
                    get<CurriculumLocalDao>(named(CURRICULUM_SCOPE)).get(id)
                }
                withGetByParent { userId ->
                    get<CurriculumLocalDao>(named(CURRICULUM_SCOPE)).getByUserId(userId)
                }
                withCustomQuery(
                    QueryByStatusRepositoryComponent.STATUS_STRATEGY_KEY,
                    QueryByStatusRepositoryComponent.StatusQueryStrategy { status ->
                        get<CurriculumLocalDao>(named(CURRICULUM_SCOPE)).getByStatus(status)
                    })
            })
    }

    single<RepositoryConfig<ModuleModel, ModuleEntity>>(named(MODULE_SCOPE)) {
        RepositoryConfig(
            collection = Collection.MODULES,
            remoteDao = get<ExtendedDao<ModuleModel>>(named(MODULE_SCOPE)),
            localDao = get<ModuleLocalDao>(named(MODULE_SCOPE)),
            modelMapper = get<ModelMapper<ModuleModel, ModuleEntity>>(named(MODULE_SCOPE)),
            syncManager = get<SyncManager<ModuleModel>>(named(MODULE_SCOPE)),
            queryStrategies = QueryStrategies<ModuleEntity>().apply {
                withGetById { id ->
                    get<ModuleLocalDao>(named(MODULE_SCOPE)).get(id)
                }
                withGetByParent { curriculumId ->
                    get<ModuleLocalDao>(named(MODULE_SCOPE)).getByCurriculumId(curriculumId)
                }
                withCustomQuery(
                    QueryByScoreRepositoryComponent.SCORE_STRATEGY_KEY,
                    QueryByScoreRepositoryComponent.ScoreQueryStrategy { curriculumId, score ->
                        get<ModuleLocalDao>(named(MODULE_SCOPE)).getByMinQuizScore(curriculumId, score)
                    }
                )
                withCustomQuery(
                    QueryByCurriculumIdRepositoryComponent.CURRICULUM_STRATEGY_KEY,
                    QueryByCurriculumIdRepositoryComponent.CurriculumQueryStrategy { curriculumId ->
                        get<ModuleLocalDao>(named(MODULE_SCOPE)).getByCurriculumId(curriculumId)
                    }
                )
            })
    }

    single<RepositoryConfig<Lesson, LessonEntity>>(named(LESSON_SCOPE)) {
        RepositoryConfig(
            collection = Collection.LESSONS,
            remoteDao = get<ExtendedDao<Lesson>>(named(LESSON_SCOPE)),
            localDao = get<LessonLocalDao>(named(LESSON_SCOPE)),
            modelMapper = get<ModelMapper<Lesson, LessonEntity>>(named(LESSON_SCOPE)),
            syncManager = get<SyncManager<Lesson>>(named(LESSON_SCOPE)),
            queryStrategies = QueryStrategies<LessonEntity>().apply {
                withGetById { id ->
                    get<LessonLocalDao>(named(LESSON_SCOPE)).get(id)
                }
                withGetByParent { moduleId ->
                    get<LessonLocalDao>(named(LESSON_SCOPE)).getByModuleId(moduleId)
                }
                withCustomQuery(
                    QueryByScoreRepositoryComponent.SCORE_STRATEGY_KEY,
                    QueryByScoreRepositoryComponent.ScoreQueryStrategy { moduleId, score ->
                        get<LessonLocalDao>(named(LESSON_SCOPE)).getByMinQuizScore(moduleId, score)
                    }
                )
                withCustomQuery(
                    QueryByCurriculumIdRepositoryComponent.CURRICULUM_STRATEGY_KEY,
                    QueryByCurriculumIdRepositoryComponent.CurriculumQueryStrategy { curriculumId ->
                        get<LessonLocalDao>(named(LESSON_SCOPE)).getByCurriculumId(curriculumId)
                    }
                )
            })
    }

    single<RepositoryConfig<Section, SectionEntity>>(named(SECTION_SCOPE)) {
        RepositoryConfig(
            collection = Collection.SECTIONS,
            remoteDao = get<ExtendedDao<Section>>(named(SECTION_SCOPE)),
            localDao = get<SectionLocalDao>(named(SECTION_SCOPE)),
            modelMapper = get<ModelMapper<Section, SectionEntity>>(named(SECTION_SCOPE)),
            syncManager = get<SyncManager<Section>>(named(SECTION_SCOPE)),
            queryStrategies = QueryStrategies<SectionEntity>().apply {
                withGetById { id ->
                    get<SectionLocalDao>(named(SECTION_SCOPE)).get(id)
                }
                withGetByParent { lessonId ->
                    get<SectionLocalDao>(named(SECTION_SCOPE)).getByLessonId(lessonId)
                }
                withCustomQuery(
                    QueryByScoreRepositoryComponent.SCORE_STRATEGY_KEY,
                    QueryByScoreRepositoryComponent.ScoreQueryStrategy { lessonId, score ->
                        get<SectionLocalDao>(named(SECTION_SCOPE)).getByMinQuizScore(lessonId, score)
                    }
                )
                withCustomQuery(
                    QueryByCurriculumIdRepositoryComponent.CURRICULUM_STRATEGY_KEY,
                    QueryByCurriculumIdRepositoryComponent.CurriculumQueryStrategy { curriculumId ->
                        get<SectionLocalDao>(named(SECTION_SCOPE)).getByCurriculumId(curriculumId)
                    }
                )
            })
    }

    single<RepositoryConfig<Session, SessionEntity>>(named(SESSION_SCOPE)) {
        RepositoryConfig(
            collection = Collection.SESSIONS,
            remoteDao = get<ExtendedDao<Session>>(named(SESSION_SCOPE)),
            localDao = get<SessionLocalDao>(named(SESSION_SCOPE)),
            modelMapper = get<ModelMapper<Session, SessionEntity>>(named(SESSION_SCOPE)),
            syncManager = get<SyncManager<Session>>(named(SESSION_SCOPE)),
            queryStrategies = QueryStrategies<SessionEntity>().apply {
                withGetById { id ->
                    get<SessionLocalDao>(named(SESSION_SCOPE)).get(id)
                }
                withGetByParent { userId ->
                    get<SessionLocalDao>(named(SESSION_SCOPE)).getByUserId(userId)
                }
                withCustomQuery(
                    QueryByDateRangeRepositoryComponent.DATE_RANGE_QUERY_STRATEGY_KEY,
                    QueryByDateRangeRepositoryComponent.DateRangeQueryStrategy { startTime, endTime ->
                        get<SessionLocalDao>(named(SESSION_SCOPE)).getByDateRange(startTime, endTime)
                    })
            })
    }

    // Repositories
    single<ProfileRepository>(named(USER_PROFILE_SCOPE)) {
        object : ProfileRepository, CrudOperations<Profile> by CrudRepositoryComponent(
            get<RepositoryConfig<Profile, ProfileEntity>>((named(USER_PROFILE_SCOPE)))
        ) {}
    }

    single<CurriculumRepository>(named(CURRICULUM_SCOPE)) {
        object : CurriculumRepository, CrudOperations<Curriculum> by CrudRepositoryComponent(
            get<RepositoryConfig<Curriculum, CurriculumEntity>>((named(CURRICULUM_SCOPE)))
        ), QueryByStatusOperation<Curriculum> by QueryByStatusRepositoryComponent(
            get<RepositoryConfig<Curriculum, CurriculumEntity>>((named(CURRICULUM_SCOPE)))
        ), BatchOperations<Curriculum> by BatchRepositoryComponent(
            get<RepositoryConfig<Curriculum, CurriculumEntity>>((named(CURRICULUM_SCOPE)))
        ) {}
    }

    single<ModuleRepository>(named(MODULE_SCOPE)) {
        object : ModuleRepository, CrudOperations<ModuleModel> by CrudRepositoryComponent(
            get<RepositoryConfig<ModuleModel, ModuleEntity>>((named(MODULE_SCOPE)))
        ), BatchOperations<ModuleModel> by BatchRepositoryComponent(
            get<RepositoryConfig<ModuleModel, ModuleEntity>>((named(MODULE_SCOPE)))
        ), QueryByScoreOperation<ModuleModel> by QueryByScoreRepositoryComponent(
            get<RepositoryConfig<ModuleModel, ModuleEntity>>((named(MODULE_SCOPE)))
        ), QueryByCurriculumIdOperation<ModuleModel> by QueryByCurriculumIdRepositoryComponent(
            get<RepositoryConfig<ModuleModel, ModuleEntity>>((named(MODULE_SCOPE)))
        ) {}
    }

    single<LessonRepository>(named(LESSON_SCOPE)) {
        object : LessonRepository, CrudOperations<Lesson> by CrudRepositoryComponent(
            get<RepositoryConfig<Lesson, LessonEntity>>((named(LESSON_SCOPE)))
        ), BatchOperations<Lesson> by BatchRepositoryComponent(
            get<RepositoryConfig<Lesson, LessonEntity>>((named(LESSON_SCOPE)))
        ), QueryByScoreOperation<Lesson> by QueryByScoreRepositoryComponent(
            get<RepositoryConfig<Lesson, LessonEntity>>((named(LESSON_SCOPE)))
        ), QueryByCurriculumIdOperation<Lesson> by QueryByCurriculumIdRepositoryComponent(
            get<RepositoryConfig<Lesson, LessonEntity>>((named(LESSON_SCOPE)))
        ) {}
    }

    single<SectionRepository>(named(SECTION_SCOPE)) {
        object : SectionRepository, CrudOperations<Section> by CrudRepositoryComponent(
            get<RepositoryConfig<Section, SectionEntity>>((named(SECTION_SCOPE)))
        ), BatchOperations<Section> by BatchRepositoryComponent(
            get<RepositoryConfig<Section, SectionEntity>>((named(SECTION_SCOPE)))
        ), QueryByScoreOperation<Section> by QueryByScoreRepositoryComponent(
            get<RepositoryConfig<Section, SectionEntity>>((named(SECTION_SCOPE)))
        ), QueryByCurriculumIdOperation<Section> by QueryByCurriculumIdRepositoryComponent(
            get<RepositoryConfig<Section, SectionEntity>>((named(SECTION_SCOPE)))
        ) {}
    }

    single<SessionRepository>(named(SESSION_SCOPE)) {
        object : SessionRepository, CrudOperations<Session> by CrudRepositoryComponent(
            get<RepositoryConfig<Session, SessionEntity>>((named(SESSION_SCOPE)))
        ), BatchOperations<Session> by BatchRepositoryComponent(
            get<RepositoryConfig<Session, SessionEntity>>((named(SESSION_SCOPE)))
        ), QueryByDateRangeOperation<Session> by QueryByDateRangeRepositoryComponent(
            get<RepositoryConfig<Session, SessionEntity>>((named(SESSION_SCOPE)))
        ) {}
    }

    // Use Cases - Path Builder
    singleOf(::BuildProfilePathUseCase)
    singleOf(::BuildCurriculumPathUseCase)
    singleOf(::BuildModulePathUseCase)
    singleOf(::BuildLessonPathUseCase)
    singleOf(::BuildSectionPathUseCase)
    singleOf(::BuildSessionPathUseCase)

    // Use Cases - Auth
    singleOf(::SignUpUseCase)
    singleOf(::SignInUseCase)
    singleOf(::GetUserDataUseCase)
    singleOf(::SendVerificationEmailUseCase)
    singleOf(::VerifyEmailUseCase)
    singleOf(::DeleteUserUseCase)
    singleOf(::SendPasswordResetEmailUseCase)

    // Use Cases - User Profile
    single { CreateProfileUseCase(get(named(USER_PROFILE_SCOPE)), get()) }
    single { UpdateProfileUseCase(get(named(USER_PROFILE_SCOPE)), get()) }
    single { GetProfileUseCase(get(), get(named(USER_PROFILE_SCOPE))) }
    singleOf(::UploadProfilePictureUseCase)
    singleOf(::DeleteProfilePictureUseCase)
    singleOf(::GetStyleQuestionnaireUseCase)
    singleOf(::GetStyleResultUseCase)

    // Use Cases - Curriculum
    single { UploadCurriculumUseCase(get(named(CURRICULUM_SCOPE))) }
    single { UpdateCurriculumUseCase(get(named(CURRICULUM_SCOPE))) }
    single { DeleteCurriculumUseCase(get(named(CURRICULUM_SCOPE))) }
    single { GetCurriculumUseCase(get(named(CURRICULUM_SCOPE))) }
    single { GetAllCurriculaUseCase(get(named(CURRICULUM_SCOPE))) }
    single { GetCurriculaByStatusUseCase(get(named(CURRICULUM_SCOPE))) }
    single { GenerateCurriculumUseCase(get(named(CURRICULUM_SCOPE))) }
    singleOf(::GetActiveCurriculumUseCase)

    // Use Cases - Module
    single { UploadModulesUseCase(get(named(MODULE_SCOPE))) }
    single { UpdateModuleUseCase(get(named(MODULE_SCOPE))) }
    single { GetModuleUseCase(get(named(MODULE_SCOPE))) }
    single { GetAllModulesUseCase(get(named(MODULE_SCOPE))) }
    single { GetModulesByCurriculumIdUseCase(get(named(MODULE_SCOPE))) }
    single { GetModulesByMinQuizScoreUseCase(get(named(MODULE_SCOPE))) }
    single { DeleteAllModulesUseCase(get(named(MODULE_SCOPE))) }
    single { GenerateModuleUseCase(get(named(MODULE_SCOPE))) }
    singleOf(::CountModulesByStatusUseCase)

    // Use Cases - Lesson
    single { UploadLessonsUseCase(get(named(LESSON_SCOPE))) }
    single { UpdateLessonUseCase(get(named(LESSON_SCOPE))) }
    single { GetLessonUseCase(get(named(LESSON_SCOPE))) }
    single { GetAllLessonsUseCase(get(named(LESSON_SCOPE))) }
    single { GetLessonsByCurriculumIdUseCase(get(named(LESSON_SCOPE))) }
    single { GetLessonsByMinQuizScoreUseCase(get(named(LESSON_SCOPE))) }
    single { DeleteAllLessonsUseCase(get(named(LESSON_SCOPE))) }
    single { GenerateLessonUseCase(get(named(LESSON_SCOPE))) }
    singleOf(::CountLessonsByStatusUseCase)

    // Use Cases - Section
    single { UploadSectionsUseCase(get(named(SECTION_SCOPE))) }
    single { UpdateSectionUseCase(get(named(SECTION_SCOPE))) }
    single { GetAllSectionsUseCase(get(named(SECTION_SCOPE))) }
    single { GetSectionsByCurriculumIdUseCase(get(named(SECTION_SCOPE))) }
    single { GetSectionByMinQuizScoreUseCase(get(named(SECTION_SCOPE))) }
    single { DeleteAllSectionsUseCase(get(named(SECTION_SCOPE))) }
    single { GenerateSectionUseCase(get(named(SECTION_SCOPE))) }
    singleOf(::CountSectionsByStatusUseCase)

    // Use Cases - Session
    single { UploadSessionUseCase(get(named(SESSION_SCOPE))) }
    single { UpdateSessionUseCase(get(named(SESSION_SCOPE))) }
    single { GetAllSessionsUseCase(get(named(SESSION_SCOPE))) }
    single { GetSessionsByDateRangeUseCase(get(named(SESSION_SCOPE))) }

    // Use Cases - Syllabus
    singleOf(::SummarizeSyllabusUseCase)

    // Use Cases - Validation
    singleOf(::ValidateEmailUseCase)
    singleOf(::ValidatePasswordUseCase)
    singleOf(::ValidateUsernameUseCase)
    singleOf(::ValidatePasswordConfirmationUseCase)

    // Use Cases - Activity
    singleOf(::GetWeeklyActivityUseCase)

    // ViewModels
    viewModel {
        BaseViewModel(
            dispatcher = get(),
            syncMangers = get()
        )
    }

    viewModel {
        AuthViewModel(
            signUpUseCase = get(),
            signInUseCase = get(),
            sendVerificationEmailUseCase = get(),
            verifyEmailUseCase = get(),
            deleteUserUseCase = get(),
            sendPasswordResetEmailUseCase = get(),
            validateEmailUseCase = get(),
            validatePasswordUseCase = get(),
            validatePasswordConfirmationUseCase = get(),
            dispatcher = get()
        )
    }

    viewModel {
        CreateUserProfileViewModel(
            getUserDataUseCase = get(),
            createProfileUseCase = get(),
            uploadProfilePictureUseCase = get(),
            deleteProfilePictureUseCase = get(),
            getStyleQuestionnaireUseCase = get(),
            getStyleResultUseCase = get(),
            updateProfileUseCase = get(),
            validateUsernameUseCase = get(),
            buildProfilePathUseCase = get(),
            dispatcher = get(),
            syncManagers = get(),
            sharingStarted = get()
        )
    }
    viewModel {
        DashboardViewModel(
            buildProfilePathUseCase = get(),
            buildSessionPathUseCase = get(),
            buildCurriculumPathUseCase = get(),
            buildModulePathUseCase = get(),
            buildLessonPathUseCase = get(),
            buildSectionPathUseCase = get(),
            getProfileUseCase = get(),
            getAllSessionsUseCase = get(),
            getActiveCurriculumUseCase = get(),
            getAllCurriculaUseCase = get(),
            getAllLessonsUseCase = get(),
            getAllSectionsUseCase = get(),
            getAllModulesUseCase = get(),
            getWeeklyActivityUseCase = get(),
            countModulesByStatusUseCase = get(),
            countLessonsByStatusUseCase = get(),
            countSectionsByStatusUseCase = get(),
            dispatcher = get(),
            syncManagers = get(),
            sharingStarted = get()
        )
    }
    viewModel {
        LibraryViewModel(
            buildProfilePathUseCase = get(),
            buildModulePathUseCase = get(),
            getProfileUseCase = get(),
            summarizeSyllabusUseCase = get(),
            generateModuleUseCase = get(),
            uploadModulesUseCase = get(),
            buildCurriculumPathUseCase = get(),
            getAllCurriculaUseCase = get(),
            generateCurriculumUseCase = get(),
            uploadCurriculumUseCase = get(),
            dispatcher = get(),
            syncManagers = get(),
            sharingStarted = get(),
        )
    }
}