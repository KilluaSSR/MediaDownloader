package killua.dev.twitterdownloader.api.Lofter.BuildRequest

internal fun makeArchiveData(authorId: String, queryNum: Int): Map<String, String> = mapOf(
    "callCount" to "1",
    "scriptSessionId" to "$/{scriptSessionId}187",
    "httpSessionId" to "",
    "c0-scriptName" to "ArchiveBean",
    "c0-methodName" to "getArchivePostByTime",
    "c0-id" to "0",
    "c0-param0" to "boolean:false",
    "c0-param1" to "number:$authorId",
    "c0-param2" to "number:${System.currentTimeMillis()}",
    "c0-param3" to "number:$queryNum",
    "c0-param4" to "boolean:false",
    "batchId" to "918906",
)