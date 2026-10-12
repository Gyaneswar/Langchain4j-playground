package com.gyaneswar.service;


import dev.langchain4j.service.spring.AiService;

@AiService
public interface ChatAIAgent {

    String chat(String message);
}