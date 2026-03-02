package com.demo.service;

import com.demo.model.Workspace;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Random;

@Service
public class WorkspaceService {
    private final Map<Long, Workspace> workspaces = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Random random = new Random();
    private final Counter workspaceCreatedCounter;
    private final Timer workspaceListTimer;

    public WorkspaceService(MeterRegistry meterRegistry) {
        this.workspaceCreatedCounter = Counter.builder("workspace.created")
                .description("Количество созданных рабочих пространств")
                .register(meterRegistry);
        this.workspaceListTimer = Timer.builder("workspace.list.time")
                .description("Время получения списка рабочих пространств")
                .register(meterRegistry);
    }

    public List<Workspace> getAllWorkspaces() {
        try {
            return workspaceListTimer.recordCallable(() -> {
                // Демонстрация: задержка увеличивается с количеством workspace
                int delay = 50 + (workspaces.size() * 10) + random.nextInt(100);
                try {
                    Thread.sleep(Math.min(delay, 500)); // Максимум 500ms
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return new ArrayList<>(workspaces.values());
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Workspace createWorkspace(String name, String description) {
        // Демонстрация: небольшая случайная задержка
        try {
            Thread.sleep(50 + random.nextInt(150));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Long id = idGenerator.getAndIncrement();
        Workspace workspace = new Workspace(id, name, description);
        workspaces.put(id, workspace);
        workspaceCreatedCounter.increment();
        return workspace;
    }

    public Optional<Workspace> getWorkspaceById(Long id) {
        return Optional.ofNullable(workspaces.get(id));
    }
}

