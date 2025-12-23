package com.demo.dto;

public class VersionResponse {
    private String version;
    private String buildDate;
    private String buildCommit;
    private String serviceName;

    public VersionResponse() {
    }

    public VersionResponse(String version, String buildDate, String buildCommit, String serviceName) {
        this.version = version;
        this.buildDate = buildDate;
        this.buildCommit = buildCommit;
        this.serviceName = serviceName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBuildDate() {
        return buildDate;
    }

    public void setBuildDate(String buildDate) {
        this.buildDate = buildDate;
    }

    public String getBuildCommit() {
        return buildCommit;
    }

    public void setBuildCommit(String buildCommit) {
        this.buildCommit = buildCommit;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}

