package com.orbitx.backend.domain.assistant.controller;

import com.orbitx.backend.domain.assistant.dto.ChatRequest;
import com.orbitx.backend.domain.assistant.dto.ChatResponse;
import com.orbitx.backend.domain.assistant.port.AssistantPort;
import com.orbitx.backend.domain.dashboard.controller.DashboardController;
import com.orbitx.backend.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/assistant")
@RequiredArgsConstructor
@Tag(name = "AI Assistant", description = "Context-aware assistant with RAG + Tool Calling (Spring AI)")
@SecurityRequirement(name = "BearerAuth")
public class AssistantController {

    private final AssistantPort assistantPort;

    @PostMapping("/chat")
    @Operation(
            summary = "Chat with Orbit X AI",
            description = """
                    Sends a message to the Orbit X AI assistant.

                    **When OPENAI_API_KEY is configured:** Uses Spring AI with GPT-4o-mini,
                    RAG (vector similarity search on datacenter knowledge base), and Tool Calling
                    (live KPIs, datacenter status, active alerts, sustainability metrics).

                    **Without API key:** Falls back to the offline keyword-matching engine
                    with live telemetry injection.

                    The `history` field enables multi-turn conversations (sliding window of 20 messages).
                    """
    )
    public ResponseEntity<EntityModel<ChatResponse>> chat(
            @Valid @RequestBody ChatRequest request
    ) {
        ChatResponse response = assistantPort.chat(request);

        EntityModel<ChatResponse> model = EntityModel.of(response,
                linkTo(methodOn(AssistantController.class).chat(null)).withSelfRel(),
                linkTo(methodOn(DashboardController.class).getKpis()).withRel("live-kpis"),
                linkTo(methodOn(DashboardController.class).getAlerts()).withRel("alerts")
        );

        return ResponseEntity.ok(model);
    }
}
