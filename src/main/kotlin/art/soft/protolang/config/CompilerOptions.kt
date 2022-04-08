package art.soft.protolang.config

data class CompilerOptions(
    val indentation: String = "    "
) {
    companion object {
        val DEFAULT = CompilerOptions()
    }
}