package killua.dev.setup
sealed class EnvState{
    data object Idle: EnvState()
    data object Processing: EnvState()
    data object Success: EnvState()
    data object Error: EnvState()
}