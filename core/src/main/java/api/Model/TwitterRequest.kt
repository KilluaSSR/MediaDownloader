package api.Model

import api.Constants.TwitterAPIURL

data class TwitterRequest(
    val url: String,
    val parameters: Map<String, String>,
    val bearer: String = TwitterAPIURL.Bearer
)