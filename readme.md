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
    - Tubing GUIs
        - Builds GUIs easily
        - Use XML templating for building GUIs
        - Easily handling user flows by providing a GUI history
