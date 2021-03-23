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
    private static IocContainer iocContainer;

    public static BsCoreReport get() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        AutoUpdater.updateConfig(this);
        
        iocContainer = new IocContainer();
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

## @IocBean
THe IocBean annotation is used to tell the IocContainer to instantiate this bean. Only constructor injection is supported.
For example. Let's say I have a command and service which is triggered by that command.

### SimpleCommand
```
@IocBean
public class SimpleCommand implements CommandExecutor {

    private final SimpleService simpleService;
    
    public SimpleCommand(SimpleService simpleService) {
      this.simpleService = simpleService;
      // Doing this is not entirely clean but in the concept of mc plugins I find this acceptable
      // By doing this I also only have registration and creation of the command in one spot.
      ExamplePlugin.get().getCommand("simple-command")).setExecutor(this)
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
       this.simpleService.doSomething(sender);
    }
}
```

### SimpleService

@IocBean
public class SimpleService {

    private final SimpleRepository simpleRepository;
    
    public SimpleCommand(SimpleRepository simpleRepository) {
      this.simpleRepository = simpleRepository;  
    }
    
    public boolean doSomething(CommandSender sender) {
       //do something
    }
}

As you can see I only need to specify the annotation and I can be certain that the SimpleService will be provided.
You can see the SimpleService inject a SimpleRepository. This class is an interface wich I will explain below.

## Interface injection
In Mc Ioc there is no need to do anything special for interface injection.
If the interface has a class implementing it which is also an IocBean, it will just inject that instance.
Should the be no instance for that class an exception will be thrown at startup. If there are multiple implementations for that interface instantiated you should use `@IocMultiProvider`

For example:

```
public interface SimpleRepository {
  Simple getSimple(int id);
}

@IocBean
public class MysqlSimpleRepository implement SimpleRepository {
  public Simple getSimple(int id) {
    return null;
  }
}
```

## Conditional Bean instantiation
Mc-Ioc allows you to instantiate beans conditionally dependening on configuration properties.
Let's say you support mysql and sqlite database in your plugin. Maybe you also have 2 different repository implementations to support that.

Within the configuration file users of the plugin can set a storage type:

```
storage:
   type: mysql
```

Inside the code we have 2 different repositories.

```
public interface SimpleRepository {
  Simple getSimple(int id);
}

@IocBean(conditionalOnProperty = "storage.type=mysql")
public class MysqlSimpleRepository implement SimpleRepository {
  public Simple getSimple(int id) {
    return null;
  }
}

@IocBean(conditionalOnProperty = "storage.type=sqlite")
public class SqliteSimpleRepository implement SimpleRepository {
  public Simple getSimple(int id) {
    return null;
  }
}
```

The IocContainer will only instantiate the right one based on the property file.
By doing this there is no need to write if else statements in the code.

## List Injection
Mc-Ioc supports injection a List of type interface.

```
public interface ExampleMultiInterface {}

@IocBean
public class EasyExampleMultiInterface implements ExampleMultiInterface {}

public class MediumExampleMultiInterface implements ExampleMultiInterface {}

public class DifficultExampleMultiInterface implements ExampleMultiInterface {}
```


