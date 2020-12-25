import java.io.File
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class Finder(
    private val dir: File, private val substr: String,
    private val mt: Boolean = false
) {

    private var service: ExecutorService? = null

    init {
        if (mt) service = Executors.newCachedThreadPool()
    }

    fun search(): List<File> {
        val res = searchFiles(dir)
        if (mt)
            service!!.shutdown()
        return res
    }

    private fun searchFiles(dir: File): List<File> {
        val list = dir.listFiles()
        val result = mutableListOf<File>()
        val listEx = mutableListOf<Future<List<File>>>()
        if (list != null) {
            for (file in list) {
                if (file.isDirectory) {
                    if (mt) {
                        listEx.add(service!!.submit(Callable { searchFiles(file) }))
                    } else {
                        result.addAll(searchFiles(file))
                    }
                } else {
                    if (file.extension == "txt")
                        if (searchSubstring(file)) {
                            result.add(file.absoluteFile)
//                            println(file.absolutePath)
                        }
                }
            }
        }

        if (mt)
            for (ex in listEx)
                result.addAll(ex.get())

        return result.toList()
    }

    private fun searchSubstring(file: File): Boolean {
        file.bufferedReader().use {
            for (line in it.readLines()) {
                if (line.contains(substr)) {
                    return true
                }
            }
        }
        return false
    }
}