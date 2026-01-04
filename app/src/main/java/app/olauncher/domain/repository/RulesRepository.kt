package app.olauncher.domain.repository

import app.olauncher.domain.model.UsageRule

interface RulesRepository {
    suspend fun getRulesForPackage(packageName: String): List<UsageRule>
    suspend fun addRule(packageName: String, rule: UsageRule)
    suspend fun removeRule(packageName: String, rule: UsageRule)
    fun getAllRules(): kotlinx.coroutines.flow.Flow<List<app.olauncher.data.local.RuleEntity>>
    suspend fun clearRules(packageName: String)
}
