import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.random.Random.Default.nextInt

internal class FinderTest {
    private val expectedList = mutableListOf<File>()

    @BeforeEach
    internal fun createTestsDirectory() {
        var count = 0

        fun writeText(file: File) {
            val writer = file.bufferedWriter()
            val strBuilder = StringBuilder()
            for (i in 1..nextInt(1, 100)) {
                strBuilder.append("${nextInt(0, 9)}")
            }
            val chance = nextInt(1, 10)
            if (chance in 9..10) {
                strBuilder.append("test")
                expectedList.add(file.absoluteFile)
            }
            writer.write(strBuilder.toString())
            writer.close()
        }

        fun createFiles(depth: Int, dir: File) {
            if (depth > 0) {
                for (fileNum in 1..nextInt(1, 5)) {
                    File("${dir.absoluteFile}${File.separator}$count.txt").createNewFile()
                    writeText(File("${dir.absoluteFile}${File.separator}$count.txt"))
                    count++
                }
                for (dirNum in 1..nextInt(1, 5)) {      // можно изменять
//                for (dirNum in 1..5) {
                    File("${dir.absoluteFile}${File.separator}$count").mkdir()
                    count++
                    createFiles(depth - 1, File("${dir.absoluteFile}${File.separator}${count - 1}"))
                }
            }
        }

        val depth = 10  // можно изменять
        val testDir = File(".${File.separator}src${File.separator}test${File.separator}resources${File.separator}tests")
        testDir.mkdir()
        createFiles(depth, testDir)
    }

    @Test
    fun searchTest() {
        val startTime1 = System.currentTimeMillis()
        val finder1 = Finder(
            File(".${File.separator}src${File.separator}test${File.separator}resources${File.separator}tests"),
            "test", false
        ).search()
        println("Time for single threaded: ${System.currentTimeMillis() - startTime1}")

        val startTime2 = System.currentTimeMillis()
        val finder2 = Finder(
            File(".${File.separator}src${File.separator}test${File.separator}resources${File.separator}tests"),
            "test", true
        ).search()
        println("Time for multithreading: ${System.currentTimeMillis() - startTime2}")

        assertEquals(expectedList.sorted(), finder1.sorted())
        assertEquals(expectedList.sorted(), finder2.sorted())
    }

    @AfterEach
    fun deleteTestsDirectory() {
        fun delete(file: File) {
            if (!file.exists())
                return
            if (file.isDirectory) {
                val files = file.listFiles()
                if (files != null)
                    for (f in files)
                        delete(f)
            }
            file.delete()
        }
        delete(File(".${File.separator}src${File.separator}test${File.separator}resources${File.separator}tests"))
    }

}