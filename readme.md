## Introduction

Tubing is a framework to aid loose coupling spigot plugins.
The framework sets up an IOC container and gives you annotation based dependency injection with minimal setup
Full explanation can be found on the wiki: https://staffplusplus-minecraft.gitbook.io/tubing/

Some of it core features are:

    - Dependency Injection
        - Interface injection
        - List injection
        - Conditional bean registration
    - Configuration
        - Support for multiple configuration files
        - Automatically update configuration files
        - Inject configuration properties anytime anywhere with a simple annotation

## Setup

### pom.xml

```
<repository>
    <id>staffplusplus-repo</id>
    <url>https://nexus.staffplusplus.org/repository/staffplusplus/</url>
</repository>
```

```
<dependency>
    <groupId>be.garagepoort.mcioc</groupId>
    <artifactId>tubing</artifactId>
    <version>6.0.0</version>
    <exclusions>
        <exclusion>
            <groupId>*</groupId>
            <artifactId>*</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

Make sure to relocate the tubing package using the maven shade plugin

```
<relocation>
    <pattern>be.garagepoort.mcioc.</pattern>
    <shadedPattern>my.package.here.be.garagepoort.mcioc.</shadedPattern>
</relocation>
```

### TubingPlugin main class

Instead of extending the default JavaPlugin bukkit class, we now need to create a class that is extending TubingPlugin.

```
import be.garagepoort.mcioc.TubingPlugin;

public class TubingExample extends TubingPlugin {

    @Override
    protected void enable() {
        getLogger().info("Plugin enabled");
    }

    @Override
    protected void disable() {
        getLogger().info("Plugin disabled");
    }

}
```

That all the setup that is needed. 
Checkout the wiki to learn where to go from here. 
https://staffplusplus-minecraft.gitbook.io/tubing
