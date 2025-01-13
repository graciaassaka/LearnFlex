package org.example.composeApp.presentation.state

import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.*

/**
 * Represents the state of the study UI.
 *
 * @property session The user's session.
 * @property profile The user's profile information.
 * @property curricula The user's curricula.
 * @property curriculum The user's curriculum.
 * @property modules The user's modules.
 * @property module The user's module.
 * @property lessons The user's lessons.
 * @property lesson The user's lesson.
 * @property sections The user's sections.
 * @property quiz The user's quiz.
 * @property quizCollection The quiz collection.
 * @property showQuizPane Indicates if the quiz pane should be shown.
 * @property showQuizResultDialog Indicates if the quiz result dialog should be shown.
 * @property isUploading Indicates if the quiz is currently uploading.
 * @property isGenerating Indicates if the quiz is currently generating.
 * @property isRefreshing Indicates if the study UI is currently refreshing.
 */
data class StudyUIState(
    val session: Session? = null,
    val profile: Profile? = null,
    val curricula: List<Curriculum> = emptyList(),
    val curriculum: Curriculum? = null,
    val modules: List<Module> = emptyList(),
    val module: Module? = null,
    val lessons: List<Lesson> = emptyList(),
    val lesson: Lesson? = null,
    val sections: List<Section> = emptyList(),
    val quiz: Quiz = Quiz(),
    val quizCollection: Collection? = null,
    val showQuizPane: Boolean = false,
    val showQuizResultDialog: Boolean = false,
    val isUploading: Boolean = false,
    val isGenerating: Boolean = false,
    val isRefreshing: Boolean = false
)