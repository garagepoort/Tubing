## Introduction

The MC-IOC library is a very small library I created to aid lose coupling spigot plugins.
The library gives you annotation based dependency injection with minimum setup
It has limited features, but in my opinion contains everything you need for building mc plugins.

### Setup

Add maven dependency
```
  <repositories>
      <repository>
          <id>staffplusplus-repo</id>
          <name>staffplusplus-repo</name>
          <url>https://repo.repsy.io/mvn/garagepoort/staffplusplus</url>
      </repository>
    </repositories>
    ...
    <dependencies>
      <dependency>
          <groupId>be.garagepoort.mcioc</groupId>
          <artifactId>mcioc</artifactId>
          <version>1.0.4</version>
      </dependency>
    <dependencies>
```

Add relocation to the maven shade plugin. This is due to a bug in the reflections library.
Relocating the classes makes sure you won't get interference when other plugins are also using mc-ioc.
Make sure to change "my.package.here" in the below example!

```
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.2.4</version>
    <executions>
        <execution>
           ...
            <configuration>
                ...
                <relocations>
                    <relocation>
                        <pattern>be.garagepoort.mcioc.</pattern>
                        <shadedPattern>my.package.here.be.garagepoort.mcioc.</shadedPattern>
                    </relocation>
                </relocations>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Instantiating the container.
In your main plugin class instantiate the IocContainer.

```
public class ExamplePlugin extends JavaPlugin {

    private static ExamplePlugin plugin;
    public final IocContainer iocContainer = new IocContainer();

    public static BsCoreReport get() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        AutoUpdater.updateConfig(this);
        iocContainer.init("my.package.here", getConfig());
    }

    @Override
    public void onDisable() {
    }

}
```
This is an example project which illustrate the usage of the IocContainer.
When calling the init method you must pass your package name. This must be the root package of your plugin. The IocContainer will check for dependencies inside this package only.
The second parameter is the configuration file.
