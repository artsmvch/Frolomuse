package com.frolo.muse.model.lib;

public class Lib {
    private final String name;
    private final String dependency;
    private final String version;
    private final String copyright;
    private final String description;
    private final String license;

    public Lib(String name, String dependency, String version, String copyright, String description, String license) {
        this.name = name;
        this.dependency = dependency;
        this.version = version;
        this.copyright = copyright;
        this.description = description;
        this.license = license;
    }

    public String getName() {
        return name;
    }

    public String getDependency() {
        return dependency;
    }

    public String getVersion() {
        return version;
    }

    public String getCopyright() {
        return copyright;
    }

    public String getDescription() {
        return description;
    }

    public String getLicense() {
        return license;
    }
}
