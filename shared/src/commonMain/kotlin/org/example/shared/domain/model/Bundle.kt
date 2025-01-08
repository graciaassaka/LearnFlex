package org.example.shared.domain.model

/**
 * Bundle represents a comprehensive learning package.
 *
 * @property curriculum The overarching curriculum for the bundle.
 * @property modules A list of modules included in the bundle.
 * @property lessons A list of lessons forming part of the bundle.
 * @property sections A list of sections categorized within the bundle.
 */
data class Bundle(
    val curriculum: Curriculum,
    val modules: List<Module>,
    val lessons: List<Lesson>,
    val sections: List<Section>
)