package com.example.health.ui.photos

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.analysis.BodyPhotoDiffAnalyzer
import com.example.data.storage.PhotoStorage
import com.example.domain.model.BodyPhoto
import com.example.domain.model.PhotoComparisonPair
import com.example.domain.model.PhotoType
import com.example.domain.repository.WeightRepository
import com.example.domain.usecase.photo.GetPhotoComparisonPairsUseCase
import com.example.domain.usecase.photo.ObserveBodyPhotosByTypeUseCase
import com.example.domain.usecase.photo.SaveBodyPhotoUseCase
import com.example.health.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class PhotosViewModel(
    observe: ObserveBodyPhotosByTypeUseCase,
    private val save: SaveBodyPhotoUseCase,
    private val getPairs: GetPhotoComparisonPairsUseCase,
    private val storage: PhotoStorage,
    private val analyzer: BodyPhotoDiffAnalyzer,

) : ViewModel() {

    private val _type = MutableStateFlow(PhotoType.BODY)
    val type: StateFlow<PhotoType> = _type.asStateFlow()

    val bodyPhotos: StateFlow<List<BodyPhoto>> =
        observe(Session.USER_ID, PhotoType.BODY)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val analysisPhotos: StateFlow<List<BodyPhoto>> =
        observe(Session.USER_ID, PhotoType.ANALYSIS)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _pairs = MutableStateFlow<List<PhotoComparisonPair>>(emptyList())
    val pairs: StateFlow<List<PhotoComparisonPair>> = _pairs.asStateFlow()

    private val _verdict = MutableStateFlow<BodyPhotoDiffAnalyzer.Verdict?>(null)
    val verdict: StateFlow<BodyPhotoDiffAnalyzer.Verdict?> = _verdict.asStateFlow()

    private val _showVerdictDialog = MutableStateFlow(false)
    val showVerdictDialog: StateFlow<Boolean> = _showVerdictDialog.asStateFlow()

    fun setType(type: PhotoType) { _type.value = type }

    fun dismissVerdict() { _showVerdictDialog.value = false }

    fun bytesOf(path: String): ByteArray? = storage.readBytes(path)

    fun addPhoto(uri: Uri, type: PhotoType, note: String? = null) {
        viewModelScope.launch {
            val path = storage.savePhoto(
                uri,
                subDir = if (type == PhotoType.BODY) "body" else "analysis",
            )
            save(
                BodyPhoto(
                    id = 0,
                    userId = Session.USER_ID,
                    filePath = path,
                    type = type,
                    note = note,
                    date = LocalDateTime.now(),
                )
            )
            refreshPairs()
        }
    }

    /** Тихое обновление пар (после добавления фото) — без всплывающего окна. */
    fun refreshPairs() {
        viewModelScope.launch {
            _pairs.value = getPairs(Session.USER_ID, PhotoType.BODY)
            _pairs.value.lastOrNull()?.let { pair ->
                _verdict.value = analyzer.analyze(pair.before, pair.after)
            }
        }
    }

    /**
     * Запуск сравнения по кнопке «Сравнить прогресс».
     * Всегда показывает окно с вердиктом.
     * Если «честной» пары (интервал ≥2 недель) нет, но фото хотя бы два — алгоритм
     * прогоняется на самом старом и самом новом снимке без блокировки по сроку,
     * чтобы результат был виден (демо-режим для дипломного скриншота).
     */
    fun compareProgress() {
        viewModelScope.launch {
            val realPairs = getPairs(Session.USER_ID, PhotoType.BODY)
            _pairs.value = realPairs

            val verdict = when {
                realPairs.isNotEmpty() -> {
                    val pair = realPairs.last()
                    analyzer.analyze(pair.before, pair.after)
                }
                else -> {
                    val photos = bodyPhotos.value.sortedBy { it.date }
                    if (photos.size >= 2) {
                        val before = photos.first()
                        val after = photos.last()
                        _pairs.value = listOf(
                            PhotoComparisonPair(before, after, daysBetween = 0)
                        )
                        analyzer.analyze(before, after, enforceMinInterval = false)
                    } else {
                        null
                    }
                }
            }

            _verdict.value = verdict
            _showVerdictDialog.value = true
        }
    }
}
