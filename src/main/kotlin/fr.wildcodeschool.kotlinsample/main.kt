package fr.wildcodeschool.kotlinsample


import kotlinx.coroutines.*
import java.io.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class Department(val name: String, val surface: Double)

suspend fun main() {
    val filePath = "C:\\Users\\zappy\\Downloads\\contours-des-departements-francais-issus-dopenstreetmap.csv"
    val totalSurface = calculateTotalSurface(filePath)

    val outputFile = File.createTempFile("total_surface", ".json")
    val outputJson = buildJsonObject {
        put("totalSurface", totalSurface)
    }
    outputFile.writeText(Json.encodeToString(outputJson))

    println("Total surface: $totalSurface")
    println("Output file: ${outputFile.absolutePath}")
}

suspend fun calculateTotalSurface(filePath: String): Double = withContext(Dispatchers.IO) {
    var totalSurface = 0.0
    val departments = mutableListOf<Department>()

    File(filePath).bufferedReader().useLines { lines ->
        // Ignore la première ligne qui contient les en-têtes des colonnes
        val iterator = lines.iterator()
        if (iterator.hasNext()) {
            iterator.next()
        }

        for (line in iterator) {
            val columns = line.split(";")
            if (columns.size >= 7) { // Vérifier si la liste a suffisamment d'éléments
                val departmentName = columns[3]
                val surfaceStr = columns[6]

                if (surfaceStr.isNotEmpty()) {
                    val surface = surfaceStr.toDoubleOrNull()
                    if (surface != null) {
                        totalSurface += surface
                        val department = Department(departmentName, surface)
                        departments.add(department)
                    }
                }
            }
        }
    }

    departments.sortBy { it.name }

    for (department in departments) {
        println("Department: ${department.name}, Surface: ${department.surface}")
    }

    return@withContext totalSurface
}