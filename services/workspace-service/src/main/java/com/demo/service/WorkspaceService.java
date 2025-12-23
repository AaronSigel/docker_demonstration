package com.demo.service;

import com.demo.model.Workspace;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class WorkspaceService {
    private final Map<Long, Workspace> workspaces = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public List<Workspace> getAllWorkspaces() {
        return new ArrayList<>(workspaces.values());
    }

    public Workspace createWorkspace(String name, String description) {
        Long id = idGenerator.getAndIncrement();
        Workspace workspace = new Workspace(id, name, description);
        workspaces.put(id, workspace);
        return workspace;
    }

    public Optional<Workspace> getWorkspaceById(Long id) {
        return Optional.ofNullable(workspaces.get(id));
    }
}

