package de.brittytino.android.launcher.leetcode.data

import de.brittytino.android.launcher.leetcode.api.GraphQLQuery
import de.brittytino.android.launcher.leetcode.api.LeetCodeApi

class LeetCodeRepository(
    private val api: LeetCodeApi,
    private val dao: LeetCodeDao
) {
    val myProfile = dao.getMyProfile()
    val friends = dao.getFriends()
    val dailyProblem = dao.getDailyProblem()

    suspend fun syncUser(username: String, isMe: Boolean = false): Result<Unit> {
        return try {
            val queryStr = """
                query getUserProfile(${'$'}username: String!) {
                  matchedUser(username: ${'$'}username) {
                    username
                    profile {
                      realName
                      userAvatar
                      ranking
                    }
                    submitStats {
                      acSubmissionNum {
                        difficulty
                        count
                        submissions
                      }
                    }
                    submissionCalendar
                  }
                }
            """.trimIndent()

            val query = GraphQLQuery(
                query = queryStr,
                variables = mapOf("username" to username)
            )
            val response = api.getUserProfile(query)
            val user = response.data?.matchedUser
            
            if (user != null) {
                val entity = LeetCodeUserEntity(
                    username = user.username,
                    realName = user.profile.realName,
                    avatarUrl = user.profile.userAvatar,
                    totalSolved = user.submitStats.acSubmissionNum.find { it.difficulty == "All" }?.count ?: 0,
                    easySolved = user.submitStats.acSubmissionNum.find { it.difficulty == "Easy" }?.count ?: 0,
                    mediumSolved = user.submitStats.acSubmissionNum.find { it.difficulty == "Medium" }?.count ?: 0,
                    hardSolved = user.submitStats.acSubmissionNum.find { it.difficulty == "Hard" }?.count ?: 0,
                    ranking = user.profile.ranking,
                    submissionCalendarJson = user.submissionCalendar,
                    isMe = isMe,
                    lastUpdated = System.currentTimeMillis()
                )
                dao.insertUser(entity)
                Result.success(Unit)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    suspend fun syncDailyProblem() {
        try {
            val queryStr = """
                query questionOfToday {
                  activeDailyCodingChallengeQuestion {
                    date
                    link
                    question {
                      questionId
                      frontendQuestionId
                      title
                      titleSlug
                      difficulty
                    }
                  }
                }
            """.trimIndent()
            
            val query = GraphQLQuery(
                query = queryStr,
                variables = emptyMap()
            )
            val response = api.getUserProfile(query) // Reusing same endpoint w/ different query
            val active = response.data?.activeDailyCodingChallengeQuestion
            
            if (active != null) {
                val entity = DailyProblemEntity(
                    date = active.date,
                    title = active.question.title,
                    difficulty = active.question.difficulty,
                    titleSlug = active.question.titleSlug,
                    frontendId = active.question.frontendQuestionId,
                    link = "https://leetcode.com" + active.link
                )
                dao.insertDailyProblem(entity)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun removeFriend(username: String) {
        dao.deleteUser(username)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}
