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
@Tag(name = "AI Assistant", description = "Assistente contextual com RAG + Tool Calling (Spring AI)")
@SecurityRequirement(name = "BearerAuth")
public class AssistantController {
    private final AssistantPort assistantPort;
    @PostMapping("/chat")
    @Operation(
            summary = "Conversar com o Orbit X AI",
            description = """
                    Envia uma mensagem ao assistente de IA do Orbit X.
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
