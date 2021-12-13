## Introduction

The Tubing library is a very small library I created to aid loose coupling spigot plugins.
The library gives you annotation based dependency injection with minimal setup
It has limited features, but in my opinion contains everything you need for building mc plugins.

### Setup

Add maven dependency
```
    <repositories>
        <repository>
            <id>staffplusplus-repo</id>
            <url>https://nexus.staffplusplus.org/repository/staffplusplus/</url>
        </repository>
    </repositories>
    ...
    <dependencies>
      <dependency>
          <groupId>be.garagepoort.mcioc</groupId>
          <artifactId>tubing</artifactId>
          <version>4.0.4</version>
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

#### Instantiating the container.
Change your main plugin class to extends the TubingPlugin class.
Do not implement `onEnable` and `onDisable`, but implement `enable` and `disable`.
In the `enable` method put your configuration files setup.

```
public class ExamplePlugin extends TubingPlugin {

    @Override
    protected void enable() {
        saveDefaultConfig();
        AutoUpdater.updateConfig(this);
    }

    @Override
    protected void disable() {
        // no disabling logic
    }

}
```
This is an example project which illustrate the usage of the Tubing framework.You only need to extends the class and the IocContainer will be initialized.

### @IocBean
The IocBean annotation is used to tell the IocContainer to instantiate this bean. Only constructor injection is supported.
For example. Let's say I have a command and service which is triggered by that command.

### @IocCommandHandler
The IocCommandHandler annotation is an addition to the @IocBean annotation.
It tells Tubing to register your command. You pass in the command name as defined in your plugin.yml file.

### @IocListener
The IocListener annotation is an addition to the @IocBean annotation.
It tells Tubing to register your listener. It takes no arguments

### @IocMessageListener
The IocMessageListener annotation is an addition to the @IocBean annotation.
It tells Tubing to register your PluginMessageListener. It takes the channel name as argument


#### SimpleCommand

```
@IocBean
@IocCommandHandler("simple-command")
public class SimpleCommand implements CommandExecutor {

    private final SimpleService simpleService;
    
    public SimpleCommand(SimpleService simpleService) {
      this.simpleService = simpleService;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
       this.simpleService.doSomething(sender);
    }
}
```

#### SimpleListener

```
@IocBean
@IocListener
public class SimpleListener implements Listener {

    private final SimpleService simpleService;
    
    public SimpleCommand(SimpleService simpleService) {
      this.simpleService = simpleService;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        this.simpleService.doSomethingOnClick(event);
    }
}
```
#### SimpleMessageListener

```
@IocBean
@IocMessageListener(channel = "BungeeCord")
public class SimpleListener implements PluginMessageListener {

    private final SimpleService simpleService;
    
    public SimpleCommand(SimpleService simpleService) {
      this.simpleService = simpleService;
    }
    
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        this.simpleService.doSomethingOnBungeeChannel(channel, message);
    }
}
```

#### SimpleService
```
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
```
As you can see I only need to specify the annotation and I can be certain that the SimpleService will be provided.
You can see the SimpleService inject a SimpleRepository. This class is an interface wich I will explain below.

### Interface injection
In Tubing there is no need to do anything special for interface injection.
If the interface has a class implementing it which is also an IocBean, it will just inject that instance.
Should the be no instance for that class an exception will be thrown at startup. If there are multiple implementations for that interface instantiated you should use `@IocMultiProvider`

For example:

```
public interface SimpleRepository {
  Simple getSimple(int id);
}

@IocBean
public class MysqlSimpleRepository implements SimpleRepository {
  public Simple getSimple(int id) {
    return null;
  }
}
```

### Conditional Bean instantiation
Tubing allows you to instantiate beans conditionally depending on configuration properties.
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

### List Injection
Tubing supports injection a List of type interface.

```
public interface ExampleMultiInterface {}

@IocBean
@IocMultiProvider(ExampleMultiInterface.class)
public class EasyExampleMultiInterface implements ExampleMultiInterface {}

@IocBean
@IocMultiProvider(ExampleMultiInterface.class)
public class MediumExampleMultiInterface implements ExampleMultiInterface {}

@IocBean
@IocMultiProvider(ExampleMultiInterface.class)
public class DifficultExampleMultiInterface implements ExampleMultiInterface {}
```

By specifying @IocMultiProvider, the ioc container knows you want this bean to be added to the list of ExampleMultiInterface instances.
Now when we want to Inject a list of these instances we need to do the following:

```
@IocBean
public class SimpleMultiInterfaceExecutor {

  private final List<ExampleMultiInterface> exampleInterfaces;

  public SimpleMultiInterfaceExecutor(@IocMulti(ExampleMultiInterface.class) List<ExampleMultiInterface> exampleInterfaces) {
     this.exampleInterfaces = exampleInterfaces;
  }
}
```
