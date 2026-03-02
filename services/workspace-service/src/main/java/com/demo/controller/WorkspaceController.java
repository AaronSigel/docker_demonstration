package com.demo.controller;

import com.demo.dto.VersionResponse;
import com.demo.dto.WorkspaceRequest;
import com.demo.model.Workspace;
import com.demo.service.WorkspaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class WorkspaceController {

    private static final Logger logger = LoggerFactory.getLogger(WorkspaceController.class);

    @Autowired
    private WorkspaceService workspaceService;

    @Value("${app.version}")
    private String version;

    @Value("${app.build.date}")
    private String buildDate;

    @Value("${app.build.commit}")
    private String buildCommit;

    @GetMapping("/workspaces")
    public ResponseEntity<List<Workspace>> getAllWorkspaces() {
        logger.info("Запрос на получение списка workspace (GET /api/workspaces)");
        List<Workspace> workspaces = workspaceService.getAllWorkspaces();
        logger.info("Найдено workspace: {}", workspaces.size());
        return ResponseEntity.ok(workspaces);
    }

    @PostMapping("/workspaces")
    public ResponseEntity<Workspace> createWorkspace(@RequestBody WorkspaceRequest request) {
        logger.info("Создание workspace (POST /api/workspaces): name='{}', description='{}'",
                request.getName(), request.getDescription());
        Workspace workspace = workspaceService.createWorkspace(
            request.getName(),
            request.getDescription()
        );
        logger.info("Создан workspace с id={}", workspace.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(workspace);
    }

    @GetMapping("/version")
    public ResponseEntity<VersionResponse> getVersion() {
        VersionResponse response = new VersionResponse(
            version,
            buildDate,
            buildCommit,
            "workspace-service"
        );
        return ResponseEntity.ok(response);
    }
}

