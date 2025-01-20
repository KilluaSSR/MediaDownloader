package killua.dev.base.utils

fun <T> MutableList<T>.addRange(elements: Collection<T>) {
    this.addAll(elements)
}