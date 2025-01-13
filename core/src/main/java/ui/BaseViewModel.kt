package ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface UIIntent
interface UIState
interface UIEvent
interface IBaseViewModel<I: UIIntent, S: UIState, E: UIEvent> {
    suspend fun onEvent(state: S, intent: I)
    suspend fun onEffect(effect: E)
}
abstract class BaseViewModel <I: UIIntent, S: UIState, E: UIEvent>(state: S): ViewModel(),
    IBaseViewModel<I, S, E> {
    fun<T> Flow<T>.flowOnIO() = flowOn(Dispatchers.IO)
    fun<T> Flow<T>.stateInScope(initValue: T) = stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = initValue
    )
    private val _uiState = MutableStateFlow(state)
    val uiState: StateFlow<S> = _uiState.asStateFlow()
    suspend fun withMainContext(block: suspend CoroutineScope.() -> Unit) = withContext(
        Dispatchers.IO,block
    )
    suspend fun emitState(state: S) = withMainContext {
        _uiState.value = state
    }
    suspend fun emitIntent(intent: I) = withMainContext {
        onEvent(_uiState.value, intent)
    }
    fun emitIntentOnIO(intent: I) = launchOnIO { emitIntent(intent) }
    fun launchOnIO(block: suspend CoroutineScope.() -> Unit) = viewModelScope.launch(context = Dispatchers.IO,block = block)
    override suspend fun onEvent(state: S, intent: I) {}
    override suspend fun onEffect(effect: E) {}
}