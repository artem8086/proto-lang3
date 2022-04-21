package art.soft.protolang.ast

class NoneValue {
    override fun toString() = "none"
}

class ThisValue {
    override fun toString() = "this"
}

class FieldValue {
    override fun toString() = "field"
}

class UndefinedValue {
    override fun toString() = "undefined"
}
