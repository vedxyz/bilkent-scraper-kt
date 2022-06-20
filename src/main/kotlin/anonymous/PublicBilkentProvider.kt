package anonymous

class PublicBilkentProvider {
    companion object {
        @JvmStatic
        suspend fun getAcademicCalendar() = anonymous.data.getAcademicCalendar()
    }
}
