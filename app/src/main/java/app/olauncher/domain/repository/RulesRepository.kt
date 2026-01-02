package app.olauncher.domain.repository

import app.olauncher.domain.model.UsageRule

interface RulesRepository {
    suspend fun getRulesForPackage(packageName: String): List<UsageRule>
    suspend fun addRule(packageName: String, rule: UsageRule)
    suspend fun clearRules(packageName: String)
}
