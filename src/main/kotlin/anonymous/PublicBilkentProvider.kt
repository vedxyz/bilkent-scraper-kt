package anonymous

class PublicBilkentProvider {
    companion object {
        /**
         * Provides the contents of [/bilkent/academic-calendar](https://w3.bilkent.edu.tr/bilkent/academic-calendar/)
         *
         * All rows are parsed into a single array with no distinction of semester.
         * Event types ([anonymous.data.AcademicCalendarEventType]) are parsed.
         *
         * @return Array of academic calendar items
         */
        @JvmStatic
        suspend fun getAcademicCalendar() = anonymous.data.getAcademicCalendar()
    }
}
