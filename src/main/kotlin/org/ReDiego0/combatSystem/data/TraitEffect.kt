package org.ReDiego0.combatSystem.data

data class TraitEffect(
    val type: EffectType,
    val parameters: Map<String, Any>
) {
    fun getDouble(key: String, default: Double = 0.0): Double {
        return (parameters[key] as? Number)?.toDouble() ?: default
    }

    fun getInt(key: String, default: Int = 0): Int {
        return (parameters[key] as? Number)?.toInt() ?: default
    }

    fun getString(key: String, default: String = ""): String {
        return parameters[key]?.toString() ?: default
    }

    fun getBoolean(key: String, default: Boolean = false): Boolean {
        return (parameters[key] as? Boolean) ?: default
    }
}
