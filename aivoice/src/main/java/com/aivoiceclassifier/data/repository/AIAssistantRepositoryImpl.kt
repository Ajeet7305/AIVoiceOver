package com.aivoiceclassifier.data.repository

import com.aivoiceclassifier.BuildConfig
import com.aivoiceclassifier.data.remote.api.OpenAIService
import com.aivoiceclassifier.data.remote.dto.Message
import com.aivoiceclassifier.data.remote.dto.OpenAIRequest
import com.aivoiceclassifier.domain.repository.AIAssistantRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIAssistantRepositoryImpl @Inject constructor(
    private val openAIService: OpenAIService
) : AIAssistantRepository {
    
    companion object {
        private const val SYSTEM_PROMPT = """
            You are an AI assistant helping with interview preparation. 
            Provide concise, professional answers to interview questions.
            Keep responses under 150 words and focus on practical, actionable advice.
            Be encouraging and supportive while providing realistic guidance.
        """
    }
    
    override suspend fun getAIResponse(question: String, context: String): Result<String> {
        return try {
            // Check if API key is configured
            if (BuildConfig.OPENAI_API_KEY == "your-openai-api-key-here" || BuildConfig.OPENAI_API_KEY.isBlank()) {
                // Use fallback response when API key is not configured
                return Result.success(generateFallbackResponse(question))
            }
            
            val messages = mutableListOf<Message>().apply {
                add(Message("system", SYSTEM_PROMPT))
                if (context.isNotEmpty()) {
                    add(Message("system", "Context: $context"))
                }
                add(Message("user", question))
            }
            
            val request = OpenAIRequest(
                messages = messages,
                maxTokens = 150,
                temperature = 0.7
            )
            
            val response = openAIService.getChatCompletion(
                authorization = "Bearer ${BuildConfig.OPENAI_API_KEY}",
                request = request
            )
            
            if (response.isSuccessful && response.body() != null) {
                val aiResponse = response.body()!!
                val answer = aiResponse.choices.firstOrNull()?.message?.content
                    ?: "I'm sorry, I couldn't generate a response."
                Result.success(answer.trim())
            } else {
                // API call failed, use fallback
                Result.success(generateFallbackResponse(question))
            }
        } catch (e: Exception) {
            // Network error or other exception, use fallback response
            Result.success(generateFallbackResponse(question))
        }
    }
    
    private fun generateFallbackResponse(question: String): String {
        return when {
            question.contains("tell me about yourself", ignoreCase = true) -> 
                "I am a dedicated professional with strong technical skills and a passion for continuous learning. I have experience working in collaborative environments and enjoy tackling challenging problems. My background includes software development, and I'm always eager to contribute to team success while growing my expertise."
            
            question.contains("strength", ignoreCase = true) -> 
                "My key strengths include strong problem-solving abilities, attention to detail, and excellent communication skills. I'm also highly adaptable and enjoy learning new technologies. I work well both independently and as part of a team, and I'm committed to delivering high-quality results."
            
            question.contains("weakness", ignoreCase = true) -> 
                "I sometimes tend to be overly thorough in my work, which can slow me down initially. However, I've been working on finding the right balance between thoroughness and efficiency by setting clear priorities and deadlines for myself."
            
            question.contains("why", ignoreCase = true) && question.contains("company", ignoreCase = true) -> 
                "I'm excited about this opportunity because your company has an excellent reputation for innovation and employee development. I believe my skills and enthusiasm would be a great fit for your team, and I'm eager to contribute to your continued success while advancing my own career."
            
            question.contains("experience", ignoreCase = true) -> 
                "In my previous roles, I've gained valuable experience in software development, project management, and team collaboration. I've worked on various projects that have helped me develop both technical and soft skills, and I'm excited to bring this experience to a new challenge."
            
            question.contains("goal", ignoreCase = true) || question.contains("future", ignoreCase = true) -> 
                "My career goals include continuing to develop my technical expertise while taking on more leadership responsibilities. I want to contribute to meaningful projects and help mentor other team members. I see this role as an excellent step toward achieving these objectives."
            
            question.contains("challenge", ignoreCase = true) -> 
                "I approach challenges by first understanding the problem thoroughly, then breaking it down into manageable parts. I research best practices, consult with team members when needed, and develop a systematic approach to find solutions. I view challenges as opportunities to learn and grow."
            
            question.contains("team", ignoreCase = true) -> 
                "I believe effective teamwork requires clear communication, mutual respect, and shared goals. I contribute by being reliable, offering help when needed, and maintaining a positive attitude. I also value diverse perspectives and believe they lead to better solutions."
            
            else -> 
                "That's an excellent question. Based on my experience and understanding of the role, I believe the key is to approach it systematically, leverage available resources, and maintain open communication with stakeholders. I'm confident in my ability to handle such situations effectively."
        }
    }
} 