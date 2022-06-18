package data

import OkHttpSingleton

internal suspend fun getLetterGradeStatistics(cookie: String, semester: Semester, course: Course) =
    OkHttpSingleton.guardedFetchImage(
        "https://stars.bilkent.edu.tr/srs/ajax/stats/letter-grade-bar.php?params=${semester.year}${semester.season.value}_${course.department}_${course.number}",
        cookie
    )
