import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.FileNotFoundException

internal val jsonFmt = Json { prettyPrint = true }
internal inline fun <reified T> json(obj: T) = jsonFmt.encodeToString(obj)

internal open class TestClass {
    companion object {
        @JvmStatic
        internal fun readSample(name: String) =
            this::class.java.getResource(name)?.readText() ?: throw FileNotFoundException("Couldn't find '$name'")
    }
}
