package repository


data class LoginCredentials(val ct0: String, val authToken: String)
interface CredentialRepository {
    suspend fun getCredentials(): LoginCredentials?
}