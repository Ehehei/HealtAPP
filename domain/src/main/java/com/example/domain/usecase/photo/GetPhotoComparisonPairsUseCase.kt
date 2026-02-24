package com.example.domain.usecase.photo

import com.example.domain.model.PhotoComparisonPair
import com.example.domain.model.PhotoType
import com.example.domain.repository.BodyPhotoRepository
import java.time.temporal.ChronoUnit

class GetPhotoComparisonPairsUseCase(
    private val repository: BodyPhotoRepository
) {
    suspend operator fun invoke(
        userId: String,
        type: PhotoType,
        intervalWeeks: Int = 2
    ): List<PhotoComparisonPair> {
        val photos = repository.getByType(userId, type).sortedBy { it.date }
        if (photos.size < 2) return emptyList()

        val minDays = intervalWeeks * 7L
        val pairs = mutableListOf<PhotoComparisonPair>()
        var beforeIndex = 0

        for (i in 1 until photos.size) {
            val daysBetween = ChronoUnit.DAYS.between(
                photos[beforeIndex].date,
                photos[i].date
            ).toInt()

            if (daysBetween >= minDays) {
                pairs.add(
                    PhotoComparisonPair(
                        before = photos[beforeIndex],
                        after = photos[i],
                        daysBetween = daysBetween
                    )
                )
                beforeIndex = i
            }
        }

        return pairs
    }
}
