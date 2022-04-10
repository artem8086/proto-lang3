package art.soft.protolang.config

data class CompilerOptions(
    val indentation: String = "    ",

    val parserWarningsEnable: Boolean = true
) {
    companion object {
        val DEFAULT = CompilerOptions()
    }
}